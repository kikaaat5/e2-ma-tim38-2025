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

public class StoreAdapter extends ListAdapter<EquipmentEntity, StoreAdapter.VH> {

    public interface OnBuyClick {
        void onClick(EquipmentEntity item);
    }

    private final OnBuyClick listener;

    public StoreAdapter(OnBuyClick listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<EquipmentEntity> DIFF =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull EquipmentEntity oldItem, @NonNull EquipmentEntity newItem) {
                    return oldItem.name.equals(newItem.name);
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
                .inflate(R.layout.item_store, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        EquipmentEntity e = getItem(position);
        h.bind(e, listener);
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvEffect, tvPrice;
        Button btnBuy;

        VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvEffect = v.findViewById(R.id.tvEffect);
            tvPrice = v.findViewById(R.id.tvPrice);
            btnBuy = v.findViewById(R.id.btnBuy);
        }

        void bind(EquipmentEntity e, OnBuyClick listener) {
            tvName.setText(e.name);
            tvEffect.setText(e.effect);
            tvPrice.setText("💰 " + e.price + " coins");
            btnBuy.setOnClickListener(v -> listener.onClick(e));
        }
    }
}
