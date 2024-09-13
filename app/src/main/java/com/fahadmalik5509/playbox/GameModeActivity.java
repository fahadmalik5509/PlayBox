package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;


public class GameModeActivity extends AppCompatActivity {

    Button playerVsPlayerBtn;
    Button playerVsAIBtn;
    private ImageView homeImageView, settingImageView, backImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gamemode_layout);

        initializeViews();
        animateViewsPulse();
    }

    //onclick Method
    public void handleGameModeClick(View view) {
        isVsAi = view.getId() == R.id.PlayerVsAI;
        playSound(this,R.raw.click_ui);
        new Handler().postDelayed(() -> changeActivity(this, TicTacToeActivity.class,false,true), ACTIVITY_TRANSITION_DELAY_MS);
    }

    @Override
    public void onBackPressed() {
        vibrate(this,50);
        changeActivity(this,HomeActivity.class,true,false);
    }

    //onclick Method
    public void goToSetting(View view)  {
        playSound(this,R.raw.click_ui);
        launchSettings(this);
    }

    public void launchSettings(Activity fromActivity) {
        Intent intent = new Intent(fromActivity, SettingActivity.class);
        intent.putExtra("origin_activity", fromActivity.getClass().getSimpleName());
        fromActivity.startActivity(intent);
    }

    //onclick Method
    public void goToHome(View view) {
        playSound(this,R.raw.click_ui);
        changeActivity(this, HomeActivity.class,true,false);
    }

    private void initializeViews() {
        playerVsPlayerBtn = findViewById(R.id.PlayerVsPlayer);
        playerVsAIBtn = findViewById(R.id.PlayerVsAI);
        settingImageView = findViewById(R.id.settingimgview);
        homeImageView = findViewById(R.id.homeimgview);
        backImageView = findViewById(R.id.backimgview);
    }

    private void animateViewsPulse() {
        animateViewPulse(this, playerVsPlayerBtn);
        animateViewPulse(this, playerVsAIBtn);
        animateViewPulse(this, homeImageView);
        animateViewPulse(this, settingImageView);
        animateViewPulse(this,backImageView);
    }
}