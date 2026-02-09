package com.example.medicationtracker.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicationtracker.R;
import com.example.medicationtracker.data.DailyDoseStatus;

import java.util.ArrayList;
import java.util.List;

public class HomeMedicineAdapter extends RecyclerView.Adapter<HomeMedicineAdapter.ViewHolder> {

    private List<DailyDoseStatus> items = new ArrayList<>();

    interface OnDoseActionListener {
        void onTake(DailyDoseStatus item);
        void onUndo(DailyDoseStatus item);
    }

    private OnDoseActionListener listener;

    public void setListener(OnDoseActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<DailyDoseStatus> list) {
        items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_medicine, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, details;
        Button take, undo;

        ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.medicineName);
            details = v.findViewById(R.id.medicineDetails);
            take = v.findViewById(R.id.takenButton);
            undo = v.findViewById(R.id.undoButton);
        }

        void bind(DailyDoseStatus item) {
            name.setText(item.getMedicine().getName());
            details.setText(item.getMedicine().getFormattedDosage() + " • " + item.getDose().getTime());

            boolean taken = item.isTaken();

            take.setEnabled(!taken);
            undo.setVisibility(taken ? View.VISIBLE : View.GONE);

            take.setOnClickListener(v -> listener.onTake(item));
            undo.setOnClickListener(v -> listener.onUndo(item));
        }
    }
}

