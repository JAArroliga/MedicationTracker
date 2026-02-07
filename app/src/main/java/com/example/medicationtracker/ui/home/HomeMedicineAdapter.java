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
    private OnTakeClickListener takeClickListener;
    private OnUndoClickListener undoClickListener;


    public interface OnTakeClickListener {
        void onTakeClick(Medicine medicine);
    }

    public interface OnUndoClickListener {
        void onUndoClick(Medicine medicine);
    }

    public void setOnTakeClickListener(OnTakeClickListener listener) {
        this.takeClickListener = listener;
    }

    public void setOnUndoClickListener(OnUndoClickListener listener) {
        this.undoClickListener = listener;
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
        private final Button undoButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.medicineName);
            details = itemView.findViewById(R.id.medicineDetails);
            takenButton = itemView.findViewById(R.id.takenButton);
            undoButton = itemView.findViewById(R.id.undoButton);
        }

        public void bind(Medicine medicine, boolean isTaken) {
            name.setText(medicine.getName());
            details.setText(medicine.getFormattedDosage() + " • " + medicine.getFormattedTimes());

            takenButton.setText(isTaken ? "Taken" : "Mark Taken");
            takenButton.setEnabled(!isTaken);

            undoButton.setVisibility(isTaken ? View.VISIBLE : View.GONE);

            takenButton.setOnClickListener(v -> {
                if (takeClickListener != null) {
                    takeClickListener.onTakeClick(medicine);
                }
            });

            undoButton.setOnClickListener(v -> {
                if (undoClickListener != null) {
                    undoClickListener.onUndoClick(medicine);
                }
            });
        }
    }
}
