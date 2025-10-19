package com.example.mobileapplication.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobileapplication.R;
import com.example.mobileapplication.ui.HomeActivity;
import com.example.mobileapplication.ui.tasks.TaskListActivity;
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

        Button btnLogin = findViewById(R.id.btnLogin);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        TextView tvRegisterLink = findViewById(R.id.tvRegisterLink);


        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            viewModel.login(email, password);
        });

        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        viewModel.getLoginSuccess().observe(this, success -> {
            if (success != null && success) {
                System.out.println("✅ Login uspešan!");

                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);                startActivity(intent);

                finish();
            } else {
                System.out.println("❌ Login neuspešan!");
            }
        });


        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                System.out.println("⚠️ Poruka: " + msg);
            }
        });
    }
}