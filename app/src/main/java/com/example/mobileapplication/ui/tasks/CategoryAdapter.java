// ui/tasks/CategoryAdapter.java
package com.example.mobileapplication.ui.tasks;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapplication.R;
import com.example.mobileapplication.data.models.CategoryEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.VH> {

    public interface Actions {
        void onEdit(CategoryEntity c);
        void onDelete(CategoryEntity c);
        void onPick(CategoryEntity c);
    }

    private final List<CategoryEntity> items = new ArrayList<>();
    private final Actions actions;

    private boolean pickMode = false;

    public CategoryAdapter(Actions actions) { this.actions = actions; }

    public void setPickMode(boolean pickMode) {
        this.pickMode = pickMode;
        notifyDataSetChanged();
    }

    public void submit(List<CategoryEntity> list){
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        CategoryEntity c = items.get(pos);

        h.tvName.setText(c.name);
        h.tvHex.setText(c.colorHex == null ? "" : c.colorHex);

        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        try { d.setColor(android.graphics.Color.parseColor(c.colorHex)); }
        catch (Exception e){ d.setColor(0xFF9E9E9E); }
        h.vDot.setBackground(d);

        h.btnEdit.setOnClickListener(v -> actions.onEdit(c));
        h.btnDelete.setOnClickListener(v -> actions.onDelete(c));

        if (pickMode) {
            h.itemView.setOnClickListener(v -> actions.onPick(c));
            h.itemView.setClickable(true);
        } else {
            h.itemView.setOnClickListener(null);
            h.itemView.setClickable(false);
        }
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        View vDot;
        MaterialTextView tvName, tvHex;
        MaterialButton btnEdit, btnDelete;
        VH(@NonNull View v){
            super(v);
            vDot = v.findViewById(R.id.vDot);
            tvName = v.findViewById(R.id.tvName);
            tvHex = v.findViewById(R.id.tvHex);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
