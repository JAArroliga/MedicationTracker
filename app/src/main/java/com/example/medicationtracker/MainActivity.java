package com.example.medicationtracker;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.medicationtracker.data.MedicineRepository;
import com.example.medicationtracker.notifications.AlarmScheduler;
import com.example.medicationtracker.notifications.MedicationReminderReceiver;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medicationtracker.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        createNotificationChannel();

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_medicine, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        binding.fabDynamic.setOnClickListener(v -> {
            int currentDest = navController.getCurrentDestination().getId();

            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.mobile_navigation, true)
                    .setLaunchSingleTop(true)
                    .build();

            if (currentDest == R.id.nav_home) {
                navController.navigate(R.id.nav_medicine, null, navOptions);
            } else {
                navController.navigate(R.id.nav_home, null, navOptions);
            }
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            binding.navView.setCheckedItem(destination.getId());

            if (destination.getId() == R.id.nav_home) {
                binding.fabDynamic.setImageResource(R.drawable.ic_menu_medicine_icon);
            } else {
                binding.fabDynamic.setImageResource(R.drawable.ic_menu_home_icon);
            }
        });

        MedicineRepository repository = new MedicineRepository(getApplication());

        AlarmManager alarmManager =
                (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {

                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);

            } else {
                repository.scheduleAllAlarms(this);
            }

        } else {
            repository.scheduleAllAlarms(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
            }
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {

            NavController navController =
                    Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

            navController.navigate(R.id.navigation_settings);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    MedicationReminderReceiver.CHANNEL_ID,
                    "Medication Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("Reminders for taking medication");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }


}