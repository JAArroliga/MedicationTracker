package com.example.medicationtracker.ui.medicine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicationtracker.Medicine;
import com.example.medicationtracker.R;

import java.util.ArrayList;
import java.util.List;

public class MedicineAdapter
        extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

    private List<Medicine> medicines = new ArrayList<>();

    public void submitList(List<Medicine> newMedicines) {
        medicines = newMedicines;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull MedicineViewHolder holder,
            int position
    ) {
        Medicine medicine = medicines.get(position);
        holder.bind(medicine);
    }

    @Override
    public int getItemCount() {
        return medicines.size();
    }

    static class MedicineViewHolder extends RecyclerView.ViewHolder {

        private final TextView name;
        private final TextView details;

        MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.medicineName);
            details = itemView.findViewById(R.id.medicineDetails);
        }

        void bind(Medicine medicine) {
            name.setText(medicine.getName());
            details.setText(
                    medicine.getDosage() + " • " + medicine.getTime()
            );
        }
    }
}
