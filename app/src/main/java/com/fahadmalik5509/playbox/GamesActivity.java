package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.ACTIVITY_TRANSITION_DELAY_MS;
import static com.fahadmalik5509.playbox.ActivityUtils.animateViewPulse;
import static com.fahadmalik5509.playbox.ActivityUtils.changeActivity;
import static com.fahadmalik5509.playbox.ActivityUtils.loadColors;
import static com.fahadmalik5509.playbox.ActivityUtils.playSound;
import static com.fahadmalik5509.playbox.ActivityUtils.vibrate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.fahadmalik5509.playbox.databinding.GamemodeLayoutBinding;
import com.fahadmalik5509.playbox.databinding.GamesLayoutBinding;

public class GamesActivity extends AppCompatActivity {
    GamesLayoutBinding vb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = GamesLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        // Handle back button press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackNavigation();
            }
        });

        animateViewsPulse();
    }

    public void handleTicTacToeButtonClick(View view) { navigateToActivity(GameModeActivity.class); }
    public void handleWordleButtonClick(View view) {
        navigateToActivity(WordleActivity.class);
    }
    public void handleColorPuzzleButtonClick(View view) { navigateToActivity(ColorPuzzleActivity.class); }

    private void handleBackNavigation() {
        vibrate(this, 50);
        changeActivity(this, HomeActivity.class, true);
    }

    private void navigateToActivity(Class < ? > targetActivity) {
        playSound(this, R.raw.click_ui);
        new Handler().postDelayed(() -> changeActivity(this, targetActivity, false), ACTIVITY_TRANSITION_DELAY_MS);
    }

    // OnClick Method
    public void goToSetting(View view) {
        playSound(this, R.raw.click_ui);
        Intent intent = new Intent(this, SettingActivity.class);
        intent.putExtra("origin_activity", this.getClass().getSimpleName());
        this.startActivity(intent);
    }

    //onclick Method
    public void goToHome(View view) {
        playSound(this, R.raw.click_ui);
        changeActivity(this, HomeActivity.class, true);
    }

    //onClick Method
    public void goBack(View view) {
        playSound(this, R.raw.click_ui);
        changeActivity(this, HomeActivity.class, true);
    }

    private void animateViewsPulse() {
        animateViewPulse(this, vb.ticTacToeB, true);
        animateViewPulse(this, vb.wordleB, true);
        animateViewPulse(this, vb.colorPuzzleB, true);
    }
}
