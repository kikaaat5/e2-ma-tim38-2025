package com.example.mobileapplication.ui.auth;

import android.app.Dialog;
import android.content.Context;
import android.widget.ImageView;

import com.example.mobileapplication.R;

public class AvatarPickerDialog extends Dialog {

    public interface OnAvatarSelectedListener {
        void onAvatarSelected(String avatarName);
    }

    public AvatarPickerDialog(Context context, OnAvatarSelectedListener listener) {
        super(context);
        setContentView(R.layout.dialog_avatar_picker);
        setTitle("Izaberi avatar");

        int[] avatarIds = {
                R.id.avatar1, R.id.avatar2, R.id.avatar3, R.id.avatar4, R.id.avatar5
        };
        String[] avatarNames = {
                "avatar1", "avatar2", "avatar3", "avatar4", "avatar5"
        };

        for (int i = 0; i < avatarIds.length; i++) {
            int index = i;
            ImageView avatarView = findViewById(avatarIds[i]);
            avatarView.setOnClickListener(v -> {
                listener.onAvatarSelected(avatarNames[index]);
                dismiss();
            });
        }
    }
}
