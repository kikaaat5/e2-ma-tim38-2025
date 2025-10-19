package com.example.mobileapplication.ui.profile;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mobileapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.EmailAuthProvider;

public class ChangePasswordDialog extends Dialog {

    public ChangePasswordDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_change_password);

        EditText etOldPass = findViewById(R.id.etOldPassword);
        EditText etNewPass = findViewById(R.id.etNewPassword);
        EditText etConfirmPass = findViewById(R.id.etConfirmPassword);
        Button btnChange = findViewById(R.id.btnConfirmChange);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        btnChange.setOnClickListener(v -> {
            String oldPass = etOldPass.getText().toString();
            String newPass = etNewPass.getText().toString();
            String confirm = etConfirmPass.getText().toString();

            if (!newPass.equals(confirm)) {
                Toast.makeText(getContext(), "Lozinke se ne poklapaju!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (auth.getCurrentUser() != null && auth.getCurrentUser().getEmail() != null) {
                auth.getCurrentUser().reauthenticate(
                                EmailAuthProvider.getCredential(auth.getCurrentUser().getEmail(), oldPass))
                        .addOnSuccessListener(aVoid -> auth.getCurrentUser().updatePassword(newPass)
                                .addOnSuccessListener(vv -> {
                                    Toast.makeText(getContext(), "Lozinka promenjena!", Toast.LENGTH_SHORT).show();
                                    dismiss();
                                })
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Greška: " + e.getMessage(), Toast.LENGTH_SHORT).show()))
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Stara lozinka nije tačna", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
