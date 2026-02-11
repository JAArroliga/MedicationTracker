package com.example.medicationtracker.ui.medicine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicationtracker.Dose;
import com.example.medicationtracker.Medicine;
import com.example.medicationtracker.R;

import java.util.ArrayList;
import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

    private List<MedicineWithDoses> items = new ArrayList<>();

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MedicineWithDoses item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<MedicineWithDoses> newItems) {
        items = newItems;
        notifyDataSetChanged();
    }

    public MedicineWithDoses getItemAt(int position) {
        return items.get(position);
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class MedicineViewHolder extends RecyclerView.ViewHolder {

        private final TextView name;
        private final TextView details;
        private final TextView times;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.medicineName);
            details = itemView.findViewById(R.id.medicineDetails);
            times = itemView.findViewById(R.id.medicineTimes);

            // Click listener for editing
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(items.get(position));
                }
            });
        }

        void bind(MedicineWithDoses item) {
            Medicine medicine = item.medicine;
            List<Dose> doses = item.doses;

            name.setText(medicine.getName());

            String detailsText =
                    medicine.getFormattedDosage()
                            + " • " + medicine.getType()
                            + " • " + medicine.getFrequencyDisplayText();

            details.setText(detailsText);

            if (doses != null && !doses.isEmpty()) {
                List<String> timesList = new ArrayList<>();
                for (Dose dose : doses) {
                    timesList.add(dose.getTime());
                }
                times.setText("⏰ " + String.join(", ", timesList));
                times.setVisibility(View.VISIBLE);
            } else {
                times.setVisibility(View.GONE);
            }
        }

    }
}
