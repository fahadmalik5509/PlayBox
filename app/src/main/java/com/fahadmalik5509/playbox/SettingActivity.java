package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.*;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.fahadmalik5509.playbox.databinding.SettingLayoutBinding;

public class SettingActivity extends AppCompatActivity {

    SettingLayoutBinding wb;

    private boolean isSoundEnabled, isVibrationEnabled;
    private String originActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wb = SettingLayoutBinding.inflate(getLayoutInflater());
        setContentView(wb.getRoot());

        originActivity = getIntent().getStringExtra("origin_activity");

        loadColors(this);
        loadPreference(this);
        animateViewsPulse();
        isSoundEnabled = sharedPreferences.getBoolean(SOUND_KEY, true);
        isVibrationEnabled = sharedPreferences.getBoolean(VIBRATION_KEY, true);
        updateButtonStates();
    }

    private void updateButtonStates() {
        changeBackgroundColor(wb.soundB, isSoundEnabled ? GREEN_COLOR : RED_COLOR);
        changeBackgroundColor(wb.vibrationB, isVibrationEnabled ? GREEN_COLOR : RED_COLOR);
        updateButtonState(wb.soundB, isSoundEnabled, "Sounds ");
        updateButtonState(wb.vibrationB, isVibrationEnabled, "Vibration ");
    }

    // OnClick Method
    public void handleSoundButtonClick(View view) {
        isSoundEnabled = !isSoundEnabled;
        playSound(this, R.raw.click_ui);
        int color = isSoundEnabled ? GREEN_COLOR : RED_COLOR;
        changeBackgroundColor(wb.soundB, color);
        updateButtonState(wb.soundB, isSoundEnabled, "Sounds ");

        saveToSharedPreferences(SOUND_KEY, isSoundEnabled);
    }

    // OnClick Method
    public void handleVibrationButtonClick(View view) {
        playSound(this, R.raw.click_ui);
        isVibrationEnabled = !isVibrationEnabled;
        int color = isVibrationEnabled ? GREEN_COLOR : RED_COLOR;
        changeBackgroundColor(wb.vibrationB, color);
        updateButtonState(wb.vibrationB, isVibrationEnabled, "Vibration ");

        saveToSharedPreferences(VIBRATION_KEY, isVibrationEnabled);
    }

    private void updateButtonState(Button button, boolean isEnabled, String text) {
        String buttonText = getString(isEnabled ? R.string.buttonStateOn : R.string.buttonStateOff, text);
        button.setText(buttonText);
    }

    // OnClick Method
    public void goBack(View view) {
        playSound(this, R.raw.click_ui);
        if (originActivity != null) {
            try {
                // Launch the originating activity
                Class < ? > clazz = Class.forName("com.yourpackage." + originActivity);
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
        if (originActivity != null) {
            try {
                // Launch the originating activity
                Class < ? > clazz = Class.forName("com.yourpackage." + originActivity);
                Intent intent = new Intent(this, clazz);
                startActivity(intent);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        finish(); // Close the settings activity
    }

    private void animateViewsPulse() {
        animateViewPulse(this, wb.soundB);
        animateViewPulse(this, wb.vibrationB);
        animateViewPulse(this, wb.backIconIV);
    }
}