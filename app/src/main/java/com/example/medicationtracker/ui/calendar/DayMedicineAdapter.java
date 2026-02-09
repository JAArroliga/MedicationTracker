package com.example.medicationtracker.ui.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicationtracker.R;
import com.example.medicationtracker.data.DailyDoseStatus;

import java.util.ArrayList;
import java.util.List;

public class DayMedicineAdapter extends RecyclerView.Adapter<DayMedicineAdapter.DayMedicineViewHolder> {

    private List<DailyDoseStatus> doseList = new ArrayList<>();

    @NonNull
    @Override
    public DayMedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Inflate the layout
        View view = inflater.inflate(R.layout.item_calendar_medicine, parent, false);

        // Safety check
        if (view == null) {
            throw new IllegalStateException("Failed to inflate item_calendar_medicine.xml. Check if the file exists and the name is correct.");
        }

        return new DayMedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayMedicineViewHolder holder, int position) {

        DailyDoseStatus item = doseList.get(position);
        if (item == null) return;

        if (item.getMedicine() != null) {
            holder.medicineNameTextView.setText(item.getMedicine().getName());
            holder.medicineDosageTextView.setText(item.getMedicine().getFormattedDosage());
        } else {
            holder.medicineNameTextView.setText("Unknown");
            holder.medicineDosageTextView.setText("-");
        }

        if (item.getDose() != null) {
            holder.frequencyTextView.setText(item.getDose().getTime());
        } else {
            holder.frequencyTextView.setText("-");
        }

        holder.takenTextView.setText(item.isTaken() ? "Taken" : "Not Taken");
    }

    @Override
    public int getItemCount() {
        return doseList != null ? doseList.size() : 0;
    }

    public void setMedicines(List<DailyDoseStatus> doses) {
        this.doseList = doses != null ? doses : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class DayMedicineViewHolder extends RecyclerView.ViewHolder {
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
