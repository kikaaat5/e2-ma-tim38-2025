package com.example.mobileapplication.ui.auth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.ComponentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobileapplication.R;
import com.example.mobileapplication.ui.viewModel.AuthViewModel;
import com.google.firebase.FirebaseApp;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends ComponentActivity {

    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(this);
        System.out.println("🔥 Firebase je uspešno inicijalizovan!");

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        // ✅ 2️⃣ Poveži UI elemente iz XML-a
        Button btnLogin = findViewById(R.id.btnLogin);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);

        // ✅ 3️⃣ Postavi klik-listener za dugme
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            viewModel.login(email, password);
        });

        viewModel.getLoginSuccess().observe(this, success -> {
            if (success)
                System.out.println("✅ Login uspešan!");
            else
                System.out.println("❌ Login neuspešan!");
        });


        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                System.out.println("⚠️ Poruka: " + msg);
            }
        });
    }
}