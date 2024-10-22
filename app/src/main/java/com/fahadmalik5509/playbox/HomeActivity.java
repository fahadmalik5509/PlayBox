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
import android.view.View;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity implements SensorEventListener {

    HomeLayoutBinding vb;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float SHAKE_THRESHOLD = 25.0f;
    private static final long SHAKE_COOLDOWN_MS = 4000;  // Cooldown time after a shake detected
    private long lastShakeTime = 0;  // Track the last time a shake was detected


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

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this, "Accelerometer sensor not available", Toast.LENGTH_SHORT).show();
        }
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
                onShakeDetected();  // Trigger shake detected immediately
                lastShakeTime = currentTime;  // Update last shake time
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    private void onShakeDetected() {
        handleCoinFlip();
    }

    private boolean isCoinFlipping = false; // Flag to track coin flipping state

    private void handleCoinFlip() {
        if (isCoinFlipping) return; // Prevent multiple executions

        isCoinFlipping = true; // Set flag to true
        playSound(this, R.raw.acoinflip);
        vb.lavCoinFlip.setVisibility(View.VISIBLE);
        vb.lavCoinFlip.playAnimation();
        vibrate(this, 300);

        // Hide the coin flip animation and show the result after 1.5 seconds
        vb.lavCoinFlip.postDelayed(() -> {
            vb.lavCoinFlip.setVisibility(View.GONE);
            playSound(this, R.raw.acoinreveal);
            vb.tvCoinFlip.setText(getCoinFlipResult());
            vb.lavCoinSplash.playAnimation();
            vb.tvCoinFlip.setVisibility(View.VISIBLE);
            animateViewScale(vb.tvCoinFlip, 0f, 1.0f, 200);
        }, 1500);

        // Hide the result TextView after an additional 3 seconds (4500 total)
        vb.tvCoinFlip.postDelayed(() -> {
            vb.tvCoinFlip.setVisibility(View.GONE);
            vb.lavCoinSplash.cancelAnimation();
            vb.lavCoinSplash.setProgress(0);
            isCoinFlipping = false;
        }, 3000);
    }

    private String getCoinFlipResult() {
        return Math.random() < 0.5 ? "HEADS" : "TAILS";
    }

    private void animateViewsPulse() {
        animateViewPulse(this, vb.ticTacToeB);
        animateViewPulse(this, vb.wordleB);
        animateViewPulse(this, vb.settingsB);
        animateViewPulse(this, vb.yesExitB);
        animateViewPulse(this, vb.noExitB);
    }
}