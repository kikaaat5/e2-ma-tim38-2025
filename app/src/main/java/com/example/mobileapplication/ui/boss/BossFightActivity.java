package com.example.mobileapplication.ui.boss;

import android.content.Context;
import android.content.Intent;
import android.hardware.*;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobileapplication.R;
import com.example.mobileapplication.data.AppDatabase;
import com.example.mobileapplication.data.dao.TaskDao;
import com.example.mobileapplication.data.repository.BossRepository;
import com.example.mobileapplication.ui.viewModel.EquipmentViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textview.MaterialTextView;

import java.util.Random;

public class BossFightActivity extends AppCompatActivity implements SensorEventListener {

    public static final String EXTRA_STAGE_START = "stage_start";
    public static final String EXTRA_STAGE_END   = "stage_end";

    private LinearProgressIndicator hpBar, ppBar;
    private MaterialTextView tvHp, tvPp, tvTries, tvChance;
    private MaterialButton btnAttack;

    private int bossIndex;
    private long bossHpMax, bossHpLeft;
    private int userPP;
    private int triesLeft = 5;
    private int successPct = 0;


    private SensorManager sm;
    private Sensor accel;
    private long lastShakeTs = 0;
    private final Random rnd = new Random();

    public static void start(AppCompatActivity a, long stageStart, long stageEnd){
        Intent i = new Intent(a, BossFightActivity.class);
        i.putExtra(EXTRA_STAGE_START, stageStart);
        i.putExtra(EXTRA_STAGE_END, stageEnd);
        a.startActivity(i);
    }

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boss_fight);
        setTitle("Borba sa bosom");

        EquipmentViewModel equipmentVM = new ViewModelProvider(this).get(EquipmentViewModel.class);
        equipmentVM.consumeBattle();

        hpBar   = findViewById(R.id.hpBar);
        ppBar   = findViewById(R.id.ppBar);
        tvHp    = findViewById(R.id.tvHp);
        tvPp    = findViewById(R.id.tvPp);
        tvTries = findViewById(R.id.tvTries);
        tvChance= findViewById(R.id.tvChance);
        btnAttack = findViewById(R.id.btnAttack);

        //BossRepository.forceReset(this);

        bossIndex  = BossRepository.getBossIndex(this);
        bossHpMax  = BossMath.hpFor(bossIndex);
        bossHpLeft = BossRepository.getHpLeft(this);
        userPP     = PlayerPower.currentPP(this);

        hpBar.setMax((int) bossHpMax);
        hpBar.setProgress((int) bossHpLeft);
        tvHp.setText("HP: " + bossHpLeft + " / " + bossHpMax);

        ppBar.setMax(userPP);
        ppBar.setProgress(userPP);
        tvPp.setText("PP: " + userPP);

        long start = getIntent().getLongExtra(EXTRA_STAGE_START, 0);
        long end   = getIntent().getLongExtra(EXTRA_STAGE_END, System.currentTimeMillis());

        TaskDao dao = AppDatabase.get(this).taskDao();
        AppDatabase.exec(() -> {
            int pct = StageStats.successPercent(dao, start, end);
            runOnUiThread(() -> {
                successPct = pct;
                tvChance.setText("Šansa: " + pct + "%");
            });
        });

        tvTries.setText("Pokušaji: " + triesLeft + "/5");
        btnAttack.setOnClickListener(v -> performAttack());

        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    private void performAttack() {
        if (triesLeft <= 0 || bossHpLeft <= 0) return;
        triesLeft--;
        tvTries.setText("Pokušaji: " + triesLeft + "/5");

        boolean hit = rnd.nextInt(100) < successPct;
        if (hit) {
            bossHpLeft = Math.max(0, bossHpLeft - userPP);
            hpBar.setProgress((int) bossHpLeft);
            tvHp.setText("HP: " + bossHpLeft + " / " + bossHpMax);
            // TODO: promijeni sliku na "hit", pa vrati na "idle"
        } else {
            // TODO: feedback za promašaj
        }
        if (bossHpLeft == 0 || triesLeft == 0) finishBattle();
    }

    private void finishBattle() {
        btnAttack.setEnabled(false);
        sm.unregisterListener(this);

        boolean defeated = bossHpLeft == 0;
        long coinsBase = BossMath.coinsFor(bossIndex);
        long coinsWon = 0;
        boolean gearDrop = false;
        String gearType = null;

        if (defeated) {
            coinsWon = coinsBase;

            int oldPP = PlayerPower.currentPP(this);
            int newPP = PlayerPower.applyWinRule(this);
            BossRepository.setHpLeft(this, bossHpLeft);

            android.widget.Toast.makeText(
                    this,
                    (oldPP <= 0)
                            ? "Nivo 1 završen: PP = " + newPP
                            : "Pobeda! PP " + oldPP + " → " + newPP + " (+¾)",
                    android.widget.Toast.LENGTH_LONG
            ).show();

            if (new Random().nextInt(100) < 20) {
                gearDrop = true;
                gearType = (new Random().nextInt(100) < 95) ? "clothes" : "weapon";
            }
            BossRepository.advanceBoss(this);
        } else {
            long removed = bossHpMax - bossHpLeft;
            if (removed * 2 >= bossHpMax) {
                coinsWon = Math.round(coinsBase / 2.0);
                if (new Random().nextInt(100) < 10) {
                    gearDrop = true;
                    gearType = (new Random().nextInt(100) < 95) ? "clothes" : "weapon";
                }
            }
            BossRepository.setHpLeft(this, bossHpLeft);
        }

        int updated = PlayerPower.currentPP(this);
        ppBar.setMax(updated);
        ppBar.setProgress(updated);
        tvPp.setText("PP: " + updated);

        BossRepository.addCoins(this, coinsWon);
        ResultChestDialog dialog = ResultChestDialog.newInstance(coinsWon, gearType);
        dialog.show(getSupportFragmentManager(), "chest");
    }

    @Override public void onSensorChanged(SensorEvent e) {
        if (e.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;
        float x=e.values[0], y=e.values[1], z=e.values[2];
        double g = Math.sqrt(x*x + y*y + z*z);
        long now = System.currentTimeMillis();
        if (g > 21 && now - lastShakeTs > 700) { lastShakeTs = now; performAttack(); }
    }
    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    @Override protected void onResume(){ super.onResume(); if (accel!=null) sm.registerListener(this,accel,SensorManager.SENSOR_DELAY_UI); }
    @Override protected void onPause(){ super.onPause(); if (accel!=null) sm.unregisterListener(this); }
}
