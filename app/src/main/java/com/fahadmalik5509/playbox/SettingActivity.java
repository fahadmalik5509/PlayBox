package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.*;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Explode;
import android.transition.Fade;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {

    private Button soundButton, vibrationButton;
    private boolean isSoundEnabled, isVibrationEnabled;
    private ImageView homeImageView, backImageView;
    private String originActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);

        originActivity = getIntent().getStringExtra("origin_activity");

        initializeViews();
        loadPreferences(this);
        animateViewsPulse();

        isSoundEnabled = sharedPreferences.getBoolean(SOUND_KEY, true);
        isVibrationEnabled = sharedPreferences.getBoolean(VIBRATION_KEY, true);
        updateButtonStates();
    }

    private void updateButtonStates() {
        changeBackgroundColor(soundButton, isSoundEnabled ? GREEN_COLOR : RED_COLOR);
        changeBackgroundColor(vibrationButton, isVibrationEnabled ? GREEN_COLOR : RED_COLOR);
        updateButtonState(soundButton, isSoundEnabled, "Sounds ");
        updateButtonState(vibrationButton, isVibrationEnabled, "Vibration ");
    }

    //onclick Method
    public void handleSoundButtonClick(View view) {
        isSoundEnabled = !isSoundEnabled;
        playSound(this, R.raw.click_ui);
        int color = isSoundEnabled ? GREEN_COLOR : RED_COLOR;
        changeBackgroundColor(soundButton, color);
        updateButtonState(soundButton, isSoundEnabled, "Sounds ");

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SOUND_KEY, isSoundEnabled);
        editor.apply();

    }

    //onclick Method
    public void handleVibrationButtonClick(View view) {
        playSound(this, R.raw.click_ui);
        isVibrationEnabled = !isVibrationEnabled;
        int color = isVibrationEnabled ? GREEN_COLOR : RED_COLOR;
        changeBackgroundColor(vibrationButton, color);
        updateButtonState(vibrationButton, isVibrationEnabled, "Vibration ");

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(VIBRATION_KEY, isVibrationEnabled);
        editor.apply();
    }

    private void updateButtonState(Button button, boolean isEnabled, String text) {
        String buttonText = getString(isEnabled ? R.string.buttonStateOn : R.string.buttonStateOff, text);
        button.setText(buttonText);
    }

    //onclick Method
    public void goToHome(View view) {
        playSound(this, R.raw.click_ui);
        changeActivity(this, HomeActivity.class, true, false);
    }

    // OnClick Method
    public void goBack(View view) {
        playSound(this, R.raw.click_ui);
        if (originActivity != null) {
            try {
                // Launch the originating activity
                Class<?> clazz = Class.forName("com.yourpackage." + originActivity);
                Intent intent = new Intent(this, clazz);
                startActivity(intent);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        finish(); // Close the settings activity
    }

    @Override
    public void onBackPressed() {
        vibrate(this, 50);
        changeActivity(this, HomeActivity.class, true, false);
    }

    private void initializeViews() {
        soundButton = findViewById(R.id.bSound);
        vibrationButton = findViewById(R.id.bVibration);
        homeImageView = findViewById(R.id.ivHomeIcon);
        backImageView = findViewById(R.id.ivBackIcon);
    }

    private void animateViewsPulse() {
        animateViewPulse(this, soundButton);
        animateViewPulse(this, vibrationButton);
        animateViewPulse(this, homeImageView);
        animateViewPulse(this, backImageView);
    }
}
