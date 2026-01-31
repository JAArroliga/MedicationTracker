package com.example.medicationtracker.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicationtracker.Medicine;
import com.example.medicationtracker.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeMedicineAdapter extends RecyclerView.Adapter<HomeMedicineAdapter.ViewHolder> {

    private List<Medicine> medicines = new ArrayList<>();
    private Map<Integer, Boolean> takenMap;

    public interface OnTakeClickListener {
        void onTakeClick(Medicine medicine);
    }

    private OnTakeClickListener takeClickListener;

    public void setOnTakeClickListener(OnTakeClickListener listener) {
        this.takeClickListener = listener;
    }

    public void submitList(List<Medicine> medicines, Map<Integer, Boolean> takenMap) {
        this.medicines = medicines != null ? medicines : new ArrayList<>();
        this.takenMap = takenMap;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_medicine, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medicine medicine = medicines.get(position);
        holder.bind(medicine, takenMap != null && Boolean.TRUE.equals(takenMap.get(medicine.getId())));
    }

    @Override
    public int getItemCount() {
        return medicines.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView name;
        private final TextView details;
        private final Button takenButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.medicineName);
            details = itemView.findViewById(R.id.medicineDetails);
            takenButton = itemView.findViewById(R.id.takenButton);
        }

        public void bind(Medicine medicine, boolean isTaken) {
            name.setText(medicine.getName());
            details.setText(medicine.getDosage() + " • " + medicine.getTime());

            takenButton.setText(isTaken ? "Taken" : "Mark Taken");
            takenButton.setEnabled(!isTaken);

            takenButton.setOnClickListener(v -> {
                if (takeClickListener != null) {
                    takeClickListener.onTakeClick(medicine);
                }
            });
        }
    }
}
