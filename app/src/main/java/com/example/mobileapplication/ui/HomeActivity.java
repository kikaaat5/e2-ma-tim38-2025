package com.example.mobileapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobileapplication.R;
import com.example.mobileapplication.ui.auth.LoginActivity;
import com.example.mobileapplication.ui.profile.ProfileActivity;
import com.example.mobileapplication.ui.profile.StatisticsActivity;
import com.example.mobileapplication.ui.tasks.TaskListActivity;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView tvLogout = findViewById(R.id.tvLogout);
        Button btnTasks = findViewById(R.id.btnTasks);
        Button btnProfile = findViewById(R.id.btnProfile);
        Button btnStats = findViewById(R.id.btnStats);


        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        btnStats.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, StatisticsActivity.class))
        );


        tvLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnTasks.setOnClickListener(v ->
                startActivity(new Intent(this, TaskListActivity.class)));


    }
}
