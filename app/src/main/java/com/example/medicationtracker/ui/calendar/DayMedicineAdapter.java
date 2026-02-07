package com.example.medicationtracker.ui.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicationtracker.R;

import java.util.ArrayList;
import java.util.List;


public class DayMedicineAdapter extends RecyclerView.Adapter<DayMedicineAdapter.DayMedicineViewHolder> {

    List<DailyMedicationStatus> medicineList = new ArrayList<>();

    @NonNull
    @Override
    public DayMedicineAdapter.DayMedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_medicine, parent, false);
        return new DayMedicineAdapter.DayMedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayMedicineAdapter.DayMedicineViewHolder holder, int position) {
        DailyMedicationStatus medicine = medicineList.get(position);
        holder.medicineNameTextView.setText(medicine.getMedicine().getName());
        holder.medicineDosageTextView.setText(medicine.getMedicine().getFormattedDosage());
        holder.frequencyTextView.setText(medicine.getMedicine().getFormattedTimes());
        holder.takenTextView.setText(medicine.isTaken() ? "Taken" : "Not Taken");
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    public void setMedicines(List<DailyMedicationStatus> medicines) {
        medicineList = medicines;
        notifyDataSetChanged();
    }


    public class DayMedicineViewHolder extends RecyclerView.ViewHolder {

        TextView medicineNameTextView;
        TextView medicineDosageTextView;
        TextView frequencyTextView;
        TextView takenTextView;

        public DayMedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            medicineNameTextView = itemView.findViewById(R.id.medicineNameTextView);
            medicineDosageTextView = itemView.findViewById(R.id.medicineDosageTextView);
            frequencyTextView = itemView.findViewById(R.id.frequencyTextView);
            takenTextView = itemView.findViewById(R.id.takenTextView);
        }
    }
}
