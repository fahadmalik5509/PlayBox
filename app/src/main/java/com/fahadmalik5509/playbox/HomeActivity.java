package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.*;
import com.fahadmalik5509.playbox.databinding.HomeLayoutBinding;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

public class HomeActivity extends AppCompatActivity {

    HomeLayoutBinding vb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = HomeLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        loadColors(this);
        initializeSoundPool(this);
        animateViewsPulse();
    }

    public void handleTicTacToeButtonClick(View view) {navigateToActivity(GameModeActivity.class); }
    public void handleWordleButtonClick(View view) {
        navigateToActivity(WordleActivity.class);
    }
    public void handleSettingsButtonClick(View view) {
        navigateToActivity(SettingActivity.class);
    }

    private void navigateToActivity(Class < ? > targetActivity) {
        playSound(this, R.raw.click_ui);
        new Handler().postDelayed(() -> changeActivity(this, targetActivity, false, true), ACTIVITY_TRANSITION_DELAY_MS);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        vibrate(this, 50);
        toggleVisibility(vb.exitRL, vb.shadowV);
    }

    // Onclick Method
    public void handleExitButtons(View view) {

        playSound(this, R.raw.click_ui);

        if(view.getTag().equals("yes")) {
            finishAffinity();
        } else {
            toggleVisibility(vb.exitRL, vb.shadowV);
        }
    }

    public void handlePlayBox(View view) {
        fun_openURL++;

        if (fun_openURL >= 5) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://xvFZjo5PgG0"));
            this.startActivity(intent);
            fun_openURL = 0;
        }
    }

    private void animateViewsPulse() {
        animateViewPulse(this, vb.ticTacToeB);
        animateViewPulse(this, vb.wordleB);
        animateViewPulse(this, vb.settingsB);
        animateViewPulse(this, vb.yesExitB);
        animateViewPulse(this, vb.noExitB);

    }
}