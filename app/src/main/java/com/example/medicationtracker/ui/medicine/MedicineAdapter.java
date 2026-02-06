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

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

    private List<Medicine> medicines = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Medicine medicine);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setMedicines(List<Medicine> medicines) {
        this.medicines = medicines;
        notifyDataSetChanged();
    }

    public Medicine getMedicineAt(int position) {
        return medicines.get(position);
    }

    public void submitList(List<Medicine> newMedicines) {
        medicines = newMedicines;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        Medicine medicine = medicines.get(position);
        holder.bind(medicine);
    }

    @Override
    public int getItemCount() {
        return medicines.size();
    }

    public class MedicineViewHolder extends RecyclerView.ViewHolder {

        private final TextView name;
        private final TextView details;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.medicineName);
            details = itemView.findViewById(R.id.medicineDetails);

            // Click listener for editing
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(medicines.get(position));
                }
            });
        }

        void bind(Medicine medicine) {
            name.setText(medicine.getName());

            String detailsText =
                    medicine.getFormattedDosage()
                            + " • " + medicine.getType()
                            + " • " + medicine.getFrequency()
                            + " • " + medicine.getTime();

            details.setText(detailsText);
        }


    }
}
