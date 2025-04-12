package com.fahadmalik5509.playbox.miscellaneous;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.*;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.databinding.HomeLayoutBinding;

public class HomeActivity extends BaseActivity implements SensorEventListener {

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
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer == null) Toast.makeText(this, "Accelerometer not Present!", Toast.LENGTH_SHORT).show();
        }

        isSoundEnabled = sharedPreferences.getBoolean(SOUND_KEY, true);
        isVibrationEnabled = sharedPreferences.getBoolean(VIBRATION_KEY, true);

    }

    @Override
    protected Class<?> getBackDestination() {
        return null;
    }

    public void handleGamesClick(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        changeActivity(this, GamesActivity.class);
    }

    public void handleToolsClick(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        changeActivity(this, ToolsActivity.class);
    }


    public void handleSettingsClick(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        Intent intent = new Intent(this, SettingActivity.class);
        intent.putExtra("origin_activity", getClass().getSimpleName());
        startActivity(intent);
    }

    // Onclick Method
    public void handleExitButtons(View view) {

        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);

        if(view.getTag().equals("yes")) {
            finishAffinity();
        } else {
            toggleVisibility(false, vb.exitRL, vb.Shadow.ShadowLayout);
        }
    }

    public void handlePlayBoxClick(View view) {
        fun_openURL++;

        if (fun_openURL >= 5) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://xvFZjo5PgG0"));
            this.startActivity(intent);
            fun_openURL = 0;
        }
    }

    public void handleGitHubClick(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
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
                triggerCoinFlip();
                lastShakeTime = currentTime;
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    private void triggerCoinFlip() {
        if (isCoinFlipping) return;
        isCoinFlipping = true;

        playSoundAndVibrate(this, R.raw.sound_coin_flip, false, 0);
        vibrate(this, 300);
        vb.lavCoinFlip.setVisibility(View.VISIBLE);
        vb.lavCoinFlip.playAnimation();

        vb.lavCoinFlip.postDelayed(() -> {
                vb.lavCoinFlip.setVisibility(View.GONE);
            playSoundAndVibrate(this, R.raw.sound_coin_reveal, true, 100);

            vb.coinFlipTV.setText(getCoinFlipResult());
            vb.coinFlipTV.setVisibility(View.VISIBLE);
            vb.confettiLAV.setVisibility(View.VISIBLE);
            animateViewScale(vb.coinFlipTV, 0f, 1.0f, 200);
            vb.confettiLAV.playAnimation();

            vb.confettiLAV.postDelayed(() -> {
                vb.confettiLAV.setVisibility(View.GONE);
                vb.coinFlipTV.setVisibility(View.GONE);
                isCoinFlipping = false;
            }, 1500);

        }, 1500);
    }

    private String getCoinFlipResult() {
        return Math.random() < 0.5 ? "HEADS" : "TAILS";
    }
}