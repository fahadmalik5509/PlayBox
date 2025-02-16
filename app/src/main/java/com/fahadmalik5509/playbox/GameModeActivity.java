package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.animateViewPulse;
import static com.fahadmalik5509.playbox.ActivityUtils.changeActivity;
import static com.fahadmalik5509.playbox.ActivityUtils.loadColors;
import static com.fahadmalik5509.playbox.ActivityUtils.playSound;
import static com.fahadmalik5509.playbox.ActivityUtils.vibrate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.fahadmalik5509.playbox.databinding.GamemodeLayoutBinding;

public class GameModeActivity extends AppCompatActivity {

    GamemodeLayoutBinding vb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = GamemodeLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackNavigation();
            }
        });

        loadColors(this);
        animateViewsPulse();
    }

    public void handlePvPClick(View view) {

        playSound(this, R.raw.click_ui);
        changeActivity(this, TicTacToeVsActivity.class);

    }

    public void handlePvAClick(View view) {

        playSound(this, R.raw.click_ui);
        changeActivity(this, TicTacToeAIActivity.class);
    }

    private void handleBackNavigation() {
        vibrate(this, 50);
        changeActivity(this, GamesActivity.class);
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
        changeActivity(this, HomeActivity.class);
    }

    //onClick Method
    public void goBack(View view) {
        playSound(this, R.raw.click_ui);
        changeActivity(this, GamesActivity.class);
    }

    private void animateViewsPulse() {
        animateViewPulse(this, vb.playerVsPlayerB, true);
        animateViewPulse(this, vb.playerVsAIB, true);
        animateViewPulse(this, vb.homeIconIV, true);
        animateViewPulse(this, vb.settingIconIV, true);
        animateViewPulse(this, vb.backIconIV, true);
    }
}