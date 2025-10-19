package com.example.mobileapplication.ui.boss;

import android.content.Context;
import android.hardware.*;
import android.os.Bundle;
import android.view.*;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.mobileapplication.R;
import com.google.android.material.textview.MaterialTextView;

import org.jspecify.annotations.NonNull;

public class ResultChestDialog extends DialogFragment implements SensorEventListener {

    public static final String K_COINS = "coins";
    public static final String K_GEAR  = "gear";

    private SensorManager sm;
    private Sensor accel;
    private boolean opened = false;
    private ImageView chest;

    public static ResultChestDialog newInstance(long coins, @Nullable String gear) {
        Bundle b = new Bundle();
        b.putLong(K_COINS, coins);
        if (gear != null) b.putString(K_GEAR, gear);
        ResultChestDialog d = new ResultChestDialog();
        d.setArguments(b);
        return d;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_chest, container, false);
        chest = v.findViewById(R.id.imgChest);
        MaterialTextView tv = v.findViewById(R.id.tvReward);

        long coins = requireArguments().getLong(K_COINS, 0L);
        String gear = requireArguments().getString(K_GEAR, null);

        String txt = "Novčići: " + coins;
        if (gear != null) txt += "\nOprema: " + ("clothes".equals(gear) ? "Odeća" : "Oružje");
        tv.setText(txt);

        chest.setImageResource(R.drawable.chest_closed);
        return v;
    }

    @Override public void onResume() {
        super.onResume();
        sm = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        if (sm != null) {
            accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accel != null) sm.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override public void onPause() {
        super.onPause();
        if (sm != null) sm.unregisterListener(this);
    }

    private long lastHit = 0;
    @Override public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;
        float x = event.values[0], y = event.values[1], z = event.values[2];
        double g = Math.sqrt(x*x + y*y + z*z) / SensorManager.GRAVITY_EARTH;
        long now = System.currentTimeMillis();
        if (g > 2.0 && now - lastHit > 600) {
            lastHit = now;
            openChest();
        }
    }
    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void openChest() {
        if (opened) return;
        opened = true;
        if (chest != null) chest.setImageResource(R.drawable.chest_open);
    }
}
