package com.example.mobileapplication.ui.equipment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapplication.R;
import com.example.mobileapplication.data.models.EquipmentEntity;

public class EquipmentAdapter extends ListAdapter<EquipmentEntity, EquipmentAdapter.VH> {

    public interface OnActivateClick {
        void onClick(EquipmentEntity item);
    }

    private final OnActivateClick listener;

    public EquipmentAdapter(OnActivateClick listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<EquipmentEntity> DIFF =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull EquipmentEntity oldItem, @NonNull EquipmentEntity newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull EquipmentEntity oldItem, @NonNull EquipmentEntity newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_equipment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        EquipmentEntity e = getItem(position);
        h.bind(e, listener);
    }

    public void markItemAsActivated(long id) {
        EquipmentEntity e = null;
        for (int i = 0; i < getCurrentList().size(); i++) {
            if (getCurrentList().get(i).id == id) {
                e = getCurrentList().get(i);
                break;
            }
        }
        if (e != null) {
            e.isActive = true;
            notifyDataSetChanged();
        }
    }


    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvEffect, tvStatus;
        Button btnActivate;

        VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvEffect = v.findViewById(R.id.tvEffect);
            tvStatus = v.findViewById(R.id.tvStatus);
            btnActivate = v.findViewById(R.id.btnActivate);
        }

        void bind(EquipmentEntity e, OnActivateClick listener) {
            tvName.setText(e.name);
            tvEffect.setText("Bonus: " + e.value + "%");
            tvStatus.setText(e.isActive ? "🟢 Aktivna" : "⚪ Neaktivna");

            if (e.isActive) {
                btnActivate.setText("Aktivno");
                btnActivate.setEnabled(false);
                btnActivate.setAlpha(0.5f);
            } else {
                btnActivate.setText("Aktiviraj");
                btnActivate.setEnabled(true);
                btnActivate.setAlpha(1f);
                btnActivate.setOnClickListener(v -> listener.onClick(e));
            }
        }



    }
}
