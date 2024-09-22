package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.*;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.fahadmalik5509.playbox.databinding.GamemodeLayoutBinding;

public class GameModeActivity extends AppCompatActivity {

    GamemodeLayoutBinding wb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wb = GamemodeLayoutBinding.inflate(getLayoutInflater());
        setContentView(wb.getRoot());

        loadColors(this);
        animateViewsPulse();
    }

    //onclick Method
    public void handleGameModeClick(View view) {
        if(view.getTag().equals("pva")) isVsAi = true;
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
        animateViewPulse(this, wb.playerVsPlayerB);
        animateViewPulse(this, wb.playerVsAIB);
        animateViewPulse(this, wb.homeIconIV);
        animateViewPulse(this, wb.settingIconIV);
        animateViewPulse(this, wb.backIconIV);
    }
}