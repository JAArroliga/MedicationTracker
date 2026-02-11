package com.example.medicationtracker.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException;
import com.example.medicationtracker.R;
import com.example.medicationtracker.data.DailyDoseStatus;
import com.example.medicationtracker.data.DayStatus;
import com.example.medicationtracker.databinding.FragmentCalendarBinding;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class CalendarFragment extends Fragment {

    // ----- UI & ViewModel -----
    private FragmentCalendarBinding binding;
    private CalendarView calendarView;
    private CalendarViewModel calendarViewModel;
    private DayMedicineAdapter dayMedicineAdapter;

    // ----- State -----
    private YearMonth currentMonth;
    private LocalDate selectedDate;


    // ----- Lifecycle -----
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);

        // Initialize ViewModel and Adapter
        calendarViewModel = new ViewModelProvider(this).get(CalendarViewModel.class);
        dayMedicineAdapter = new DayMedicineAdapter();
        binding.medicationPerDayRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.medicationPerDayRecyclerView.setAdapter(dayMedicineAdapter);

        // Calendar setup
        calendarView = binding.calendarView;
        calendarView.setSelectionBackground(R.color.grey);

        // Observers
        setupObservers();

        // Initialize selected date & month
        selectedDate = LocalDate.now();
        calendarViewModel.setSelectedDate(selectedDate);
        currentMonth = YearMonth.now();
        calendarViewModel.setSelectedMonth(currentMonth);

        // Display selected date
        updateSelectedDateText(selectedDate);

        // Render month dots
        calendarViewModel.getMonthStatus().observe(getViewLifecycleOwner(), statusMap -> {
            if (statusMap != null) renderMonth(statusMap, currentMonth);
        });

        // Calendar listeners
        setupMonthChangeListener();
        setupDayClickListener();

        return binding.getRoot();
    }


    // ----- Observers -----
    private void setupObservers() {
        calendarViewModel.getDailyDoses().observe(getViewLifecycleOwner(), list -> dayMedicineAdapter.setMedicines(list));
        calendarViewModel.getHasNoEntries().observe(getViewLifecycleOwner(), noEntries -> updateEmptyState(calendarViewModel.getSelectedDateValue()));
    }


    // ----- Calendar Listeners -----
    private void setupDayClickListener() {
        calendarView.setOnDayClickListener(eventDay -> {
            Calendar clicked = eventDay.getCalendar();
            selectedDate = LocalDate.of(clicked.get(Calendar.YEAR),
                    clicked.get(Calendar.MONTH) + 1,
                    clicked.get(Calendar.DAY_OF_MONTH));

            calendarViewModel.setSelectedDate(selectedDate);

            updateSelectedDateText(selectedDate);
            updateDailyMedications(selectedDate);

            int currentMonth = calendarView.getCurrentPageDate().get(Calendar.MONTH);
            int clickedMonth = clicked.get(Calendar.MONTH);

            if (currentMonth == clickedMonth) {
                try { calendarView.setDate(clicked); }
                catch (OutOfDateRangeException e) { e.printStackTrace(); }
            }
        });
    }

    private void setupMonthChangeListener() {
        calendarView.setOnForwardPageChangeListener(this::reloadCurrentMonth);
        calendarView.setOnPreviousPageChangeListener(this::reloadCurrentMonth);
    }


    // ----- Calendar Helpers -----
    private void reloadCurrentMonth() {
        Calendar calendar = calendarView.getCurrentPageDate();
        YearMonth newMonth = YearMonth.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
        if (!newMonth.equals(currentMonth)) {
            currentMonth = newMonth;
            calendarViewModel.setSelectedMonth(currentMonth);
        }
    }

    private void renderMonth(Map<LocalDate, DayStatus> statusMap, YearMonth month) {
        List<EventDay> events = new ArrayList<>();
        LocalDate today = LocalDate.now();

        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        for (LocalDate current = start; !current.isAfter(end); current = current.plusDays(1)) {
            DayStatus status = (current.isAfter(today)) ? DayStatus.NO_DATA
                    : statusMap.getOrDefault(current, DayStatus.NO_DATA);

            Calendar c = Calendar.getInstance();
            c.set(current.getYear(), current.getMonthValue() - 1, current.getDayOfMonth());

            events.add((selectedDate != null && selectedDate.equals(current))
                    ? new EventDay(c, R.drawable.ic_selected_dot)
                    : new EventDay(c, getIconForStatus(status)));
        }

        calendarView.setEvents(events);

        if (selectedDate != null) {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.set(selectedDate.getYear(), selectedDate.getMonthValue() - 1, selectedDate.getDayOfMonth());
            try { calendarView.setDate(selectedCal); }
            catch (OutOfDateRangeException e) { e.printStackTrace(); }
        }
    }

    public int getIconForStatus(DayStatus status) {
        switch (status) {
            case ALL_TAKEN: return R.drawable.ic_green_dot;
            case PARTIAL:   return R.drawable.ic_yellow_dot;
            case NONE:      return R.drawable.ic_red_dot;
            case NO_DATA:   return R.drawable.ic_grey_dot;
        }
        return 0;
    }


    // ----- UI Update Helpers -----
    private void updateDailyMedications(LocalDate date) {
        calendarViewModel.setSelectedDate(date);

        calendarViewModel.getDailyDoses().observe(getViewLifecycleOwner(), dailyDoses -> {
            boolean hasNoData = dailyDoses == null || dailyDoses.isEmpty();

            binding.emptyStateTextView.setVisibility(hasNoData ? View.VISIBLE : View.GONE);
            binding.medicationPerDayRecyclerView.setVisibility(hasNoData ? View.GONE : View.VISIBLE);

            dayMedicineAdapter.setMedicines(dailyDoses);
        });
    }

    private void updateEmptyState(LocalDate date) {
        calendarViewModel.setSelectedDate(date);

        calendarViewModel.getHasNoEntries().observe(getViewLifecycleOwner(), noEntries -> {
            boolean hasNoData = noEntries != null && noEntries;

            binding.emptyStateTextView.setVisibility(hasNoData ? View.VISIBLE : View.GONE);
            binding.medicationPerDayRecyclerView.setVisibility(hasNoData ? View.GONE : View.VISIBLE);

            if (hasNoData) {
                dayMedicineAdapter.setMedicines(List.of());
            }
        });
    }

    private void updateSelectedDateText(LocalDate date) {
        String formattedDate = String.format("%02d/%02d/%04d", date.getMonthValue(), date.getDayOfMonth(), date.getYear());
        binding.selectedDateTextView.setText("Selected Date: " + formattedDate);
    }


    // ----- Lifecycle Cleanup -----
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
