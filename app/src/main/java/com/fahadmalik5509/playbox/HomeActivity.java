package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.*;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private Button playTicTacToeBtn, playWordleBtn, settingBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);
        initializeSoundPool(this);
        initializeViews();
        animateViewsPulse();
    }

    public void handleTicTacToeButtonClick(View view) { navigateToActivity(GameModeActivity.class); }
    public void handleWordleButtonClick(View view) { navigateToActivity(WordleActivity.class); }
    public void handleSettingsButtonClick(View view) {
        navigateToActivity(SettingActivity.class);
    }

    private void navigateToActivity(Class<?> targetActivity) {
        playSound(this, R.raw.click_ui);
        new Handler().postDelayed(() -> changeActivity(this, targetActivity, false,true), ACTIVITY_TRANSITION_DELAY_MS);
    }

    @Override
    public void onBackPressed() {
        vibrate(this, 50);
        showExitPopup();
    }

    private void showExitPopup() {
        View popupView = LayoutInflater.from(this).inflate(R.layout.exit_layout, null);

        // Create and show the PopupWindow
        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);

        // Set initial scale to 0
        popupView.setScaleX(0.5f);
        popupView.setScaleY(0.5f);
        popupView.setAlpha(0f);

        // Apply zoom-in animation
        popupView.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .setDuration(150)
                .start();

        Button noBtn = popupView.findViewById(R.id.no);
        Button yesBtn = popupView.findViewById(R.id.yes);

        noBtn.setOnClickListener(v -> {
            playSound(this, R.raw.click_ui);

            // Apply zoom-out animation before dismissing
            popupView.animate()
                    .scaleX(0.5f)
                    .scaleY(0.5f)
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        popupWindow.dismiss();
                    });
        });

        yesBtn.setOnClickListener(v -> {
            playSound(this, R.raw.click_ui);

            // Apply zoom-out animation before dismissing
            popupView.animate()
                    .scaleX(0.5f)
                    .scaleY(0.5f)
                    .alpha(0f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        popupWindow.dismiss();
                        finishAffinity(); // Closes the app completely
                    });
        });
    }

    public void handlePlayBox(View view) {
        fun_openURL++;

        if(fun_openURL>=5) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://dQw4w9WgXcQ"));
                this.startActivity(intent);
        }
    }

    private void initializeViews() {
        playTicTacToeBtn = findViewById(R.id.playtictactoe);
        playWordleBtn = findViewById(R.id.playwordle);
        settingBtn = findViewById(R.id.settings);
    }
    private void animateViewsPulse() {
        animateViewPulse(this, playTicTacToeBtn);
        animateViewPulse(this, playWordleBtn);
        animateViewPulse(this, settingBtn);
    }
}
