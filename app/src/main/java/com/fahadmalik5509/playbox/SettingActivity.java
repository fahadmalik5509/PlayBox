package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.GREEN_COLOR;
import static com.fahadmalik5509.playbox.ActivityUtils.RED_COLOR;
import static com.fahadmalik5509.playbox.ActivityUtils.SOUND_KEY;
import static com.fahadmalik5509.playbox.ActivityUtils.VIBRATION_KEY;
import static com.fahadmalik5509.playbox.ActivityUtils.animateViewPulse;
import static com.fahadmalik5509.playbox.ActivityUtils.changeBackgroundColor;
import static com.fahadmalik5509.playbox.ActivityUtils.loadColors;
import static com.fahadmalik5509.playbox.ActivityUtils.loadPreference;
import static com.fahadmalik5509.playbox.ActivityUtils.playSound;
import static com.fahadmalik5509.playbox.ActivityUtils.saveToSharedPreferences;
import static com.fahadmalik5509.playbox.ActivityUtils.sharedPreferences;
import static com.fahadmalik5509.playbox.ActivityUtils.vibrate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.fahadmalik5509.playbox.databinding.SettingLayoutBinding;

public class SettingActivity extends AppCompatActivity {

    SettingLayoutBinding vb;
    private boolean isSoundEnabled, isVibrationEnabled;
    private String originActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = SettingLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackNavigation();
            }
        });

        originActivity = getIntent().getStringExtra("origin_activity");

        loadColors(this);
        loadPreference(this);
        animateViewsPulse();
        isSoundEnabled = sharedPreferences.getBoolean(SOUND_KEY, true);
        isVibrationEnabled = sharedPreferences.getBoolean(VIBRATION_KEY, true);
        updateButtonStates();
    }

    private void updateButtonStates() {
        changeBackgroundColor(vb.soundB, isSoundEnabled ? GREEN_COLOR : RED_COLOR);
        changeBackgroundColor(vb.vibrationB, isVibrationEnabled ? GREEN_COLOR : RED_COLOR);
        updateButtonState(vb.soundB, isSoundEnabled, "Sounds ");
        updateButtonState(vb.vibrationB, isVibrationEnabled, "Vibration ");
    }

    // OnClick Method
    public void handleSoundButtonClick(View view) {
        isSoundEnabled = !isSoundEnabled;
        playSound(this, R.raw.click_ui);
        int color = isSoundEnabled ? GREEN_COLOR : RED_COLOR;
        changeBackgroundColor(vb.soundB, color);
        updateButtonState(vb.soundB, isSoundEnabled, "Sounds ");

        saveToSharedPreferences(SOUND_KEY, isSoundEnabled);
    }

    // OnClick Method
    public void handleVibrationButtonClick(View view) {
        playSound(this, R.raw.click_ui);
        isVibrationEnabled = !isVibrationEnabled;
        int color = isVibrationEnabled ? GREEN_COLOR : RED_COLOR;
        changeBackgroundColor(vb.vibrationB, color);
        updateButtonState(vb.vibrationB, isVibrationEnabled, "Vibration ");

        saveToSharedPreferences(VIBRATION_KEY, isVibrationEnabled);
    }

    private void updateButtonState(Button button, boolean isEnabled, String text) {
        String buttonText = getString(isEnabled ? R.string.buttonStateOn : R.string.buttonStateOff, text);
        button.setText(buttonText);
    }

    // OnClick Method
    public void goBack(View view) {
        playSound(this, R.raw.click_ui);
        handleBackNavigation();
    }

    public void handleBackNavigation() {
        vibrate(this, 50);
        if (originActivity != null) {
            try {
                // Fixed package name (remove space in "com.your package")
                Class<?> clazz = Class.forName("com.fahadmalik5509.playbox." + originActivity);
                Intent intent = new Intent(this, clazz);
                startActivity(intent);
            } catch (ClassNotFoundException e) {
                // Proper error logging
                Log.e("SettingActivity", "Failed to navigate back to: " + originActivity, e);

                // Optional: Show user feedback
                Toast.makeText(this, "Navigation error", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void animateViewsPulse() {
        animateViewPulse(this, vb.soundB, true);
        animateViewPulse(this, vb.vibrationB, true);
        animateViewPulse(this, vb.backIconIV, true);
    }
}