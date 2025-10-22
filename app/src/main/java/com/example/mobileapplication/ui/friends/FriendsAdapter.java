package com.example.mobileapplication.ui.friends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapplication.R;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.VH> {
    private List<DocumentSnapshot> data;
    private final OnAddClick addListener;
    private final OnProfileClick profileListener;
    private List<String> currentFriends = new ArrayList<>();

    // 🔹 interfejsi za dve akcije
    public interface OnAddClick {
        void onAdd(DocumentSnapshot userDoc);
    }

    public interface OnProfileClick {
        void onProfile(DocumentSnapshot userDoc);
    }

    public FriendsAdapter(List<DocumentSnapshot> data,
                          OnAddClick addListener,
                          OnProfileClick profileListener) {
        this.data = data;
        this.addListener = addListener;
        this.profileListener = profileListener;
    }

    public void update(List<DocumentSnapshot> newData) {
        this.data = newData != null ? newData : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setCurrentFriends(List<String> friendIds) {
        this.currentFriends = friendIds != null ? friendIds : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        DocumentSnapshot doc = data.get(position);
        String uid = doc.getId();
        String username = doc.getString("username");
        holder.tvName.setText(username != null ? username : "(bez imena)");

        // 🔹 da li je već prijatelj
        boolean isFriend = currentFriends.contains(uid);

        if (isFriend) {
            holder.btnAdd.setText("✅ Prijatelj");
            holder.btnAdd.setEnabled(false);
        } else {
            holder.btnAdd.setText("➕ Dodaj");
            holder.btnAdd.setEnabled(true);
            holder.btnAdd.setOnClickListener(v -> addListener.onAdd(doc));
        }

        // 🔹 profil uvek aktivan
        holder.btnProfile.setOnClickListener(v -> profileListener.onProfile(doc));
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;
        Button btnAdd, btnProfile;

        VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            btnAdd = v.findViewById(R.id.btnAdd);
            btnProfile = v.findViewById(R.id.btnProfile);
        }
    }
}
