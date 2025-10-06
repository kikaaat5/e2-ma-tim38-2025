package com.example.mobileapplication.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapplication.R;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.VH> {

    public interface OnItemClick {
        void onClick(TaskListItem item);
    }

    private final List<TaskListItem> items = new ArrayList<>();
    private final OnItemClick onItemClick;
    private final OnRowAction onRowAction;

    public TaskListAdapter(OnItemClick onItemClick, OnRowAction onRowAction) {
        this.onItemClick = onItemClick;
        this.onRowAction = onRowAction;
    }

    public void setItems(List<TaskListItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged(); // << OVO je ključno

    }

    @NonNull
    @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int p) {
        TaskListItem it = items.get(p);

        h.tvTitle.setText(it.title);
        h.tvDesc.setText(it.description == null ? "" : it.description);
        h.tvKind.setText(it.kind);

        // WHEN
        h.tvWhen.setText(it.whenText == null ? "" : it.whenText);

        // XP
        h.tvXp.setText(it.valueXp + " XP");




        h.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(it);


        });

        h.btnDone.setOnClickListener(v -> onRowAction.onAction(it, "DONE"));
        h.btnPause.setOnClickListener(v -> onRowAction.onAction(it, "PAUSED"));
        h.btnCancel.setOnClickListener(v -> onRowAction.onAction(it, "CANCELED"));

    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvKind, tvWhen, tvXp;
        MaterialButton btnDone, btnPause, btnCancel;
        VH(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvDesc  = v.findViewById(R.id.tvDesc);
            tvKind  = v.findViewById(R.id.tvKind);
            tvWhen  = v.findViewById(R.id.tvWhen);
            tvXp    = v.findViewById(R.id.tvXp);
            btnDone = v.findViewById(R.id.btnDone);
            btnCancel = v.findViewById(R.id.btnCancel);
            btnPause = v.findViewById(R.id.btnPause);
        }
    }

    // Ako ti zatreba format u adapteru:
    static String formatTs(Long epochMs) {
        if (epochMs == null) return "";
        return new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(new Date(epochMs));
    }

    public interface OnRowAction { void onAction(TaskListItem item, String action); }
}
