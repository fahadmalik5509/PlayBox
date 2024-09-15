package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.*;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private Button playTicTacToeBtn, playWordleBtn, settingBtn;
    RelativeLayout shadowRelativeLayout;

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
        shadowRelativeLayout.setVisibility(View.VISIBLE);

        // Inflate the popup view
        View popupView = LayoutInflater.from(this).inflate(R.layout.exit_layout, null);

        // Create and configure the PopupWindow
        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        // Disable outside touch to dismiss the popup
        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(true);

        // Show the popup in the center of the screen
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);

        // Set initial scale and transparency for the popup animation
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
            shadowRelativeLayout.setVisibility(View.GONE);

            // Apply zoom-out animation before dismissing
            popupView.animate()
                    .scaleX(0.5f)
                    .scaleY(0.5f)
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> popupWindow.dismiss())
                    .start();
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
                    })
                    .start();
        });

        // Dismiss the popup and shadow when the window is dismissed
        popupWindow.setOnDismissListener(() -> {
            shadowRelativeLayout.setVisibility(View.GONE);
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
        playTicTacToeBtn = findViewById(R.id.bTicTacToe);
        playWordleBtn = findViewById(R.id.bWordle);
        settingBtn = findViewById(R.id.bSettings);
        shadowRelativeLayout = findViewById(R.id.rlShadow);
    }
    private void animateViewsPulse() {
        animateViewPulse(this, playTicTacToeBtn);
        animateViewPulse(this, playWordleBtn);
        animateViewPulse(this, settingBtn);
    }
}
