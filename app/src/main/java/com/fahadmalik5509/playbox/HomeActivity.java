package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.*;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import com.fahadmalik5509.playbox.databinding.HomeLayoutBinding;

public class HomeActivity extends AppCompatActivity {

    HomeLayoutBinding wb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wb = HomeLayoutBinding.inflate(getLayoutInflater());
        setContentView(wb.getRoot());

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
        vibrate(this, 50);
        toggleVisibility(wb.exitRL, wb.shadowV);
    }

    // Onclick Method
    public void handleExitButtons(View view) {

        playSound(this, R.raw.click_ui);

        if(view.getTag().equals("yes")) {
            finishAffinity();
        } else {
            toggleVisibility(wb.exitRL, wb.shadowV);
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
        animateViewPulse(this, wb.ticTacToeB);
        animateViewPulse(this, wb.wordleB);
        animateViewPulse(this, wb.settingsB);
        animateViewPulse(this, wb.yesExitB);
        animateViewPulse(this, wb.noExitB);

    }
}