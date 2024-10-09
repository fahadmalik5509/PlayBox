package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.*;
import com.fahadmalik5509.playbox.databinding.GamemodeLayoutBinding;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

public class GameModeActivity extends AppCompatActivity {

    GamemodeLayoutBinding vb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = GamemodeLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        loadColors(this);
        animateViewsPulse();
    }

    //onclick Method
    public void handleGameModeClick(View view) {
        isVsAi = view.getTag().equals("pva");
        playSound(this, R.raw.click_ui);
        new Handler().postDelayed(() -> changeActivity(this, TicTacToeActivity.class, false, true), ACTIVITY_TRANSITION_DELAY_MS);
    }

    @Override
    public void onBackPressed() {
        vibrate(this, 50);
        changeActivity(this, HomeActivity.class, true, false);
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
        changeActivity(this, HomeActivity.class, true, false);
    }

    private void animateViewsPulse() {
        animateViewPulse(this, vb.playerVsPlayerB);
        animateViewPulse(this, vb.playerVsAIB);
        animateViewPulse(this, vb.homeIconIV);
        animateViewPulse(this, vb.settingIconIV);
        animateViewPulse(this, vb.backIconIV);
    }
}