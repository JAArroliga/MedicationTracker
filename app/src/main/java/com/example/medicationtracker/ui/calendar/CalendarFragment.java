package com.example.medicationtracker.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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
import java.util.Map;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private CalendarView calendarView;
    private CalendarViewModel calendarViewModel;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);

        calendarView = binding.calendarView;
        calendarViewModel = new ViewModelProvider(this).get(CalendarViewModel.class);

        setupCalendar();
        setupDayClickListener();

        return binding.getRoot();
    }

    private void setupCalendar() {
        calendarViewModel.getMonthMedicationStatus(YearMonth.now()).observe(getViewLifecycleOwner(), map -> {
            List<EventDay> events = new ArrayList<>();

            for (Map.Entry<LocalDate, DayStatus> entry : map.entrySet()) {
                LocalDate date = entry.getKey();
                DayStatus status = entry.getValue();

                int iconRes = getIconForStatus(status);
                Calendar calendar = Calendar.getInstance();
                calendar.set(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
                EventDay eventDay = new EventDay(calendar, iconRes);
                events.add(eventDay);
            }
            calendarView.setEvents(events);
        });
    }

    private void setupDayClickListener() {
        calendarView.setOnDayClickListener(eventDay -> {
            Calendar clicked = eventDay.getCalendar();

            LocalDate date = LocalDate.of(clicked.get(Calendar.YEAR), clicked.get(Calendar.MONTH) + 1, clicked.get(Calendar.DAY_OF_MONTH));

            List<String> meds = calendarViewModel.getMedicationsForDate(date);

            String message = meds.isEmpty() ? "No medications taken" : String.join("\n", meds);

            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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
        }
        return 0;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}