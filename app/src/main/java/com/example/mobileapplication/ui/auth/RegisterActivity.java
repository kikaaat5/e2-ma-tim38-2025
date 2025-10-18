package com.example.mobileapplication.ui.auth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobileapplication.R;
import com.example.mobileapplication.data.models.User;
import com.example.mobileapplication.ui.viewModel.AuthViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterActivity extends AppCompatActivity {

    private AuthViewModel viewModel;

    private EditText etEmail, etUsername, etPassword, etConfirm;
    private Button btnRegister;
    private String selectedAvatar = "avatar1"; // podrazumevano
    private ImageView ivAvatarPreview;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        ivAvatarPreview = findViewById(R.id.ivAvatarPreview);

        ivAvatarPreview.setOnClickListener(v -> {
            AvatarPickerDialog dialog = new AvatarPickerDialog(this, avatarName -> {
                selectedAvatar = avatarName;
                int resId = getResources().getIdentifier(avatarName, "drawable", getPackageName());
                ivAvatarPreview.setImageResource(resId);
            });
            dialog.show();
        });
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirm);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirm = etConfirm.getText().toString().trim();

            if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Sva polja su obavezna", Toast.LENGTH_SHORT).show();
                return;
            }

            User user = new User(null, email, username, null);
            user.setAvatar(selectedAvatar);


            viewModel.register(user, password, confirm);
        });


        viewModel.getRegistrationSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "✅ Registracija uspešna!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}