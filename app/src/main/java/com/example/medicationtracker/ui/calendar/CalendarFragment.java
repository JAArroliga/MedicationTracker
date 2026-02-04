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
import com.example.medicationtracker.R;
import com.example.medicationtracker.data.DayStatus;
import com.example.medicationtracker.databinding.FragmentCalendarBinding;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private CalendarView calendarView;
    private CalendarViewModel calendarViewModel;
    private DayMedicineAdapter dayMedicineAdapter;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCalendarBinding.inflate(inflater, container, false);

        calendarView = binding.calendarView;
        calendarView.setSelectionBackground(R.color.grey);
        calendarViewModel = new ViewModelProvider(this).get(CalendarViewModel.class);

        dayMedicineAdapter = new DayMedicineAdapter();
        binding.medicationPerDayRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.medicationPerDayRecyclerView.setAdapter(dayMedicineAdapter);

        observeMonth(YearMonth.now());
        setupMonthChangeListener();
        setupDayClickListener();

        calendarViewModel.getDailyMedicines().observe(getViewLifecycleOwner(),dayMedicineAdapter::setMedicines);

        return binding.getRoot();
    }

    private void setupDayClickListener() {
        calendarView.setOnDayClickListener(eventDay -> {
            Calendar clicked = eventDay.getCalendar();

            LocalDate date = LocalDate.of(clicked.get(Calendar.YEAR), clicked.get(Calendar.MONTH) + 1, clicked.get(Calendar.DAY_OF_MONTH));

            calendarViewModel.setSelectedDate(date);

            String formattedDate = String.format("%02d/%02d/%04d", clicked.get(Calendar.MONTH) + 1, clicked.get(Calendar.DAY_OF_MONTH), clicked.get(Calendar.YEAR));
            binding.selectedDateTextView.setText("Selected Date: " + formattedDate);
        });
    }

    private void setupMonthChangeListener() {
        calendarView.setOnForwardPageChangeListener(() -> {
            reloadCurrentMonth();
        });
        calendarView.setOnPreviousPageChangeListener(() -> {
            reloadCurrentMonth();
        });

    }

    private void reloadCurrentMonth() {
        Calendar calendar = calendarView.getCurrentPageDate();
        YearMonth month = YearMonth.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
        calendarViewModel.getMonthMedicationStatus(month);
        observeMonth(month);
    }

    private void observeMonth(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        calendarViewModel.getMonthMedicationStatus(month).observe(getViewLifecycleOwner(), statusMap -> {
            List<EventDay> events = new ArrayList<>();
            LocalDate today = LocalDate.now();

            for (LocalDate current = start; !current.isAfter(end); current = current.plusDays(1)) {
                DayStatus status;

                if (current.isAfter(today)) {
                    status = DayStatus.NO_DATA;
                } else if (statusMap.containsKey(current)) {
                    status = statusMap.get(current);
                } else {
                    status = DayStatus.NO_DATA;
                }

                Calendar c = Calendar.getInstance();
                c.set(current.getYear(), current.getMonthValue() - 1, current.getDayOfMonth());
                events.add(new EventDay(c, getIconForStatus(status)));
            }

            calendarView.setEvents(events);
        });
    }

    public int getIconForStatus(DayStatus status) {
        switch (status) {
            case ALL_TAKEN:
                return R.drawable.ic_green_dot;
            case PARTIAL:
                return R.drawable.ic_yellow_dot;
            case NONE:
                return R.drawable.ic_red_dot;
            case NO_DATA:
                return R.drawable.ic_grey_dot;
        }
        return 0;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}