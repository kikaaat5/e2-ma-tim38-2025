package com.example.mobileapplication.ui.boss;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.example.mobileapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import org.jspecify.annotations.NonNull;

public class ResultChestDialog extends DialogFragment {

    private static final String ARG_COINS = "coins";
    private static final String ARG_GEAR  = "gear";

    private LottieAnimationView lottieChest;
    private MaterialButton btnOpenChest;
    private MaterialTextView tvReward;

    private ImageView ivChest;

    private static final String ARG_DEFEATED = "defeated";


    public static ResultChestDialog newInstance(long coinsWon, @Nullable String gearType){
        ResultChestDialog d = new ResultChestDialog();
        Bundle b = new Bundle();
        b.putLong(ARG_COINS, coinsWon);
        b.putString(ARG_GEAR,  gearType);

        d.setArguments(b);
        return d;
    }

    @Override public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }


    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_chest, container, false);

        LottieAnimationView lottieChest = v.findViewById(R.id.lottieChest);
        MaterialButton btnOpen = v.findViewById(R.id.btnOpenChest);
        MaterialTextView tvReward = v.findViewById(R.id.tvReward);


        long coins = getArguments()!=null ? getArguments().getLong("coins",0):0;
        String gear = getArguments()!=null ? getArguments().getString("gear"):null;
        tvReward.setText("Osvojeni novčići: " + coins + (gear!=null? "\nNova oprema: " + ("weapon".equals(gear)?"Oružje":"Odeća") : ""));

        lottieChest.addLottieOnCompositionLoadedListener(comp -> {

        });
        lottieChest.setFailureListener(t -> {
            t.printStackTrace();
            lottieChest.cancelAnimation();
            lottieChest.setImageResource(R.drawable.chest_cl);        });

        btnOpen.setOnClickListener(x -> {
            btnOpen.setEnabled(false);
            try {
                lottieChest.setRepeatCount(0);
                lottieChest.setAnimation(R.raw.chest_open);
                lottieChest.playAnimation();
                lottieChest.addAnimatorListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationEnd(Animator animation) {
                        dismissAllowingStateLoss();
                    }
                });
            } catch (Exception e) {
                lottieChest.setImageResource(R.drawable.chest_op);
                lottieChest.postDelayed(this::dismissAllowingStateLoss, 800);
            }
        });

        return v;
    }

    private void playChestShake() {
        lottieChest.setAnimation(R.raw.chest_closed);
        lottieChest.setRepeatCount(LottieDrawable.INFINITE);
        lottieChest.setRepeatMode(LottieDrawable.RESTART);
        lottieChest.playAnimation();
    }

    private void openChest() {
        btnOpenChest.setEnabled(false);
        lottieChest.cancelAnimation();
        lottieChest.setRepeatCount(0);
        lottieChest.setAnimation(R.raw.chest_open);
        lottieChest.playAnimation();
        lottieChest.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                lottieChest.removeAllAnimatorListeners();
                dismissAllowingStateLoss();
            }
        });
    }
}
