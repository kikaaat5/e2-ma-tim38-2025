package com.example.mobileapplication.ui.tasks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mobileapplication.R;
import com.example.mobileapplication.data.models.TaskEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DayTaskAdapter extends RecyclerView.Adapter<DayTaskAdapter.VH> {

    public interface OnItemClick { void onClick(TaskEntity item); }
    public interface OnRowAction { void onAction(TaskEntity item, String action); }

    private final List<TaskEntity> items = new ArrayList<>();
    @Nullable private final OnItemClick onItemClick;
    @Nullable private final OnRowAction actions;

    public DayTaskAdapter(@Nullable List<TaskEntity> start,
                          @Nullable OnItemClick onItemClick,
                          @Nullable OnRowAction actions) {
        if (start != null) items.addAll(start);
        this.onItemClick = onItemClick;
        this.actions = actions;
    }

    public DayTaskAdapter(OnItemClick onItemClick, OnRowAction actions) {
        this.onItemClick = onItemClick;
        this.actions = actions;
    }



    public DayTaskAdapter(@Nullable OnItemClick onItemClick) {
        this(null, onItemClick, null);
    }

    public void submit(@Nullable List<TaskEntity> list){
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_day_task, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        TaskEntity it = items.get(pos);
        h.tvTitle.setText(it.title);
        h.tvTime.setText(formatTs(it.scheduledAt));

        h.itemView.setOnClickListener(v -> { if (onItemClick != null) onItemClick.onClick(it); });


    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvTitle, tvTime;
        VH(@NonNull View v){
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvTime  = v.findViewById(R.id.tvTime);
        }
    }

    static String formatTs(Long epochMs) {
        if (epochMs == null) return "";
        return new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(new Date(epochMs));
    }
}
