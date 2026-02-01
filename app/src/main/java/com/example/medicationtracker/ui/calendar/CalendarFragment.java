package com.example.medicationtracker.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.example.medicationtracker.R;
import com.example.medicationtracker.databinding.FragmentCalendarBinding;

import java.time.LocalDate;
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

        setupCalendarObservers();
        setupDayClickListener();

        return binding.getRoot();
    }

    private void setupCalendar() {
        calendarViewModel.getMedicationStatusMap().observe(getViewLifecycleOwner(), statusMap -> {
            List<EventDay> eventDays = new ArrayList<>();

            for (Map.Entry<LocalDate, String> entry : statusMap.entrySet()) {
                LocalDate date = entry.getKey();
                String status = entry.getValue();

                Calendar calendar = Calendar.getInstance();
                calendar.set(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());

                int iconRes;
                switch (status) {
                    case "all_taken":
                        iconRes = R.drawable.ic_green_dot;
                        break;
                    case "partial":
                        iconRes = R.drawable.ic_yellow_dot;
                        break;
                    default:
                        iconRes = R.drawable.ic_red_dot;
                }

                events.add(new EventDay(calendar, iconRes));
            }

            calendarView.setEvents(events);
        });
    }

    private void setupDayClickListener() {
        calendarView.setOnDayClickListener(eventDay -> {
            Calendar clicked = eventDay.getCalendar();

            LocalDate date = LocalDate.of(clicked.get(Calendar.YEAR), clicked.get(Calendar.MONTH) + 1, clicked.get(Calendar.DAY_OF_MONTH);

            List<String> meds = calendarViewModel.getMedicationsForDate(date);

            String message = meds.isEmpty() ? "No medications taken" : String.join("\n", meds);

            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}