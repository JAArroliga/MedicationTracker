package com.example.medicationtracker.ui.medicine;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicationtracker.Medicine;
import com.example.medicationtracker.R;
import com.example.medicationtracker.databinding.FragmentMedicineBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MedicineFragment extends Fragment {

    private FragmentMedicineBinding binding;
    private MedicineViewModel viewModel;
    private MedicineAdapter adapter;
    private Medicine editingMedicine = null;
    private String selectedTime = "";


    public MedicineFragment() {
        super(R.layout.fragment_medicine);
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding = FragmentMedicineBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(MedicineViewModel.class);
        adapter = new MedicineAdapter();

        viewModel.getAllMedicines().observe(getViewLifecycleOwner(), medicines -> adapter.submitList(medicines));

        binding.medicineRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.medicineRecyclerView.setAdapter(adapter);

        binding.timeButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), (timePickerView, hourOfDay, minute1) -> {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                c.set(Calendar.MINUTE, minute1);
                SimpleDateFormat format = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                selectedTime = format.format(c.getTime());
                binding.timeLabel.setText(selectedTime);
            }, hour, minute, false
            );
            timePickerDialog.show();
        });

        binding.addMedicineButton.setOnClickListener(v -> {
            String name = binding.medicineInput.getText().toString();
            String dosage = binding.dosageInput.getText().toString();
            String time = binding.timeLabel.getText().toString();

            if (!name.isEmpty() && !dosage.isEmpty() && !time.isEmpty()) {
                if (editingMedicine != null) {
                    Medicine updatedMedicine = new Medicine(editingMedicine.getId(), name, dosage, time);
                    viewModel.updateMedicine(updatedMedicine);
                    Toast.makeText(requireContext(), "Medicine updated", Toast.LENGTH_SHORT).show();
                    editingMedicine = null;
                } else {
                    viewModel.addMedicine(0, name, dosage, time);
                }
                clearInputs();
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerview, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Medicine medicine = adapter.getMedicineAt(viewHolder.getAdapterPosition());
                viewModel.deleteMedicine(medicine);
                Toast.makeText(requireContext(), "Medicine deleted", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(binding.medicineRecyclerView);

        adapter.setOnItemClickListener(medicine -> {
            binding.medicineInput.setText(medicine.getName());
            binding.dosageInput.setText(medicine.getDosage());
            binding.timeLabel.setText(medicine.getTime());

            editingMedicine = medicine;
        });
    }

    private void renderMedicines(List<Medicine> medicines) {
        StringBuilder builder = new StringBuilder();
        for (Medicine medicine : medicines) {
            builder.append("• ")
                    .append(medicine.toString())
                    .append("\n");
        }
    }

    private void clearInputs() {
        binding.medicineInput.setText("");
        binding.dosageInput.setText("");
        binding.timeLabel.setText("Time");

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}