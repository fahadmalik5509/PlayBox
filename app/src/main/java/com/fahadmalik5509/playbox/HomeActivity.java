package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.*;
import com.fahadmalik5509.playbox.databinding.HomeLayoutBinding;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

public class HomeActivity extends AppCompatActivity implements SensorEventListener {

    HomeLayoutBinding vb;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime = 0;
    private boolean isCoinFlipping = false;
    private static final float SHAKE_THRESHOLD = 20.0f;
    private static final long SHAKE_COOLDOWN_MS = 3500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = HomeLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        loadColors(this);
        initializeSoundPool(this);
        animateViewsPulse();
    }

    public void handleTicTacToeButtonClick(View view) { navigateToActivity(GameModeActivity.class); }
    public void handleWordleButtonClick(View view) {
        navigateToActivity(WordleActivity.class);
    }
    public void handleSettingsButtonClick(View view) { navigateToActivity(SettingActivity.class); }
    public void handleColorPuzzleButtonClick(View view) { navigateToActivity(ColorPuzzleActivity.class); }

    private void navigateToActivity(Class < ? > targetActivity) {
        playSound(this, R.raw.click_ui);
        new Handler().postDelayed(() -> changeActivity(this, targetActivity, false, true), ACTIVITY_TRANSITION_DELAY_MS);
    }

    @Override
    public void onBackPressed() {
        vibrate(this, 50);
        animateViewScale(vb.exitRL,0f,1.0f,200);
        toggleVisibility(true, vb.exitRL, vb.shadowV);
    }

    // Onclick Method
    public void handleExitButtons(View view) {

        playSound(this, R.raw.click_ui);

        if(view.getTag().equals("yes")) {
            finishAffinity();
        } else {
            toggleVisibility(false, vb.exitRL, vb.shadowV);
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

    public void handleGitHub(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/fahadmalik5509"));
        this.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }


    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            float acceleration = (float) Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;
            long currentTime = System.currentTimeMillis();


            if (acceleration > SHAKE_THRESHOLD && (currentTime - lastShakeTime > SHAKE_COOLDOWN_MS)) {
                handleCoinFlip();
                lastShakeTime = currentTime;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    private void handleCoinFlip() {
        if (isCoinFlipping) return;
        isCoinFlipping = true;

        playSound(this, R.raw.acoinflip);
        vibrate(this, 300);
        vb.lavCoinFlip.setVisibility(View.VISIBLE);
        vb.lavCoinFlip.playAnimation();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                vb.lavCoinFlip.setVisibility(View.GONE);
            playSound(this, R.raw.acoinreveal);

            vb.tvCoinFlip.setText(getCoinFlipResult());
            vb.tvCoinFlip.setVisibility(View.VISIBLE);
            vb.lavCoinSplash.setVisibility(View.VISIBLE);
            animateViewScale(vb.tvCoinFlip, 0f, 1.0f, 200);
            vb.lavCoinSplash.playAnimation();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                vb.lavCoinSplash.setVisibility(View.GONE);
                vb.tvCoinFlip.setVisibility(View.GONE);
                isCoinFlipping = false;
            }, 1500);

        }, 1500);
    }

    private String getCoinFlipResult() {
        return Math.random() < 0.5 ? "HEADS" : "TAILS";
    }

    private void animateViewsPulse() {
        animateViewPulse(this, vb.ticTacToeB, true);
        animateViewPulse(this, vb.wordleB, true);
        animateViewPulse(this, vb.settingsB, true);
        animateViewPulse(this, vb.yesExitB, true);
        animateViewPulse(this, vb.noExitB, true);
    }
}