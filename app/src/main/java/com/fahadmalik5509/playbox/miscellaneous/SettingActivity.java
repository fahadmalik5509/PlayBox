package com.fahadmalik5509.playbox.miscellaneous;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.*;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.databinding.SettingLayoutBinding;

public class SettingActivity extends AppCompatActivity {

    SettingLayoutBinding vb;

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

        updateButtonStates();
    }

    private void updateButtonStates() {
        changeBackgroundColor(vb.soundB, isSoundEnabled ? LIGHT_GREEN_COLOR : DARK_RED_COLOR);
        changeBackgroundColor(vb.vibrationB, isVibrationEnabled ? LIGHT_GREEN_COLOR : DARK_RED_COLOR);
        updateButtonState(vb.soundB, isSoundEnabled, "Sounds ");
        updateButtonState(vb.vibrationB, isVibrationEnabled, "Vibration ");
    }

    // OnClick Method
    public void handleSoundButtonClick(View view) {
        isSoundEnabled = !isSoundEnabled;
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        int color = isSoundEnabled ? LIGHT_GREEN_COLOR : DARK_RED_COLOR;
        changeBackgroundColor(vb.soundB, color);
        updateButtonState(vb.soundB, isSoundEnabled, "Sounds ");

        saveToSharedPreferences(SOUND_KEY, isSoundEnabled);
    }

    // OnClick Method
    public void handleVibrationButtonClick(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        isVibrationEnabled = !isVibrationEnabled;
        int color = isVibrationEnabled ? LIGHT_GREEN_COLOR : DARK_RED_COLOR;
        changeBackgroundColor(vb.vibrationB, color);
        updateButtonState(vb.vibrationB, isVibrationEnabled, "Vibration ");

        saveToSharedPreferences(VIBRATION_KEY, isVibrationEnabled);
    }

    private void updateButtonState(Button button, boolean isEnabled, String text) {
        String buttonText = getString(isEnabled ? R.string.button_state_on : R.string.button_state_off, text);
        button.setText(buttonText);
    }

    // OnClick Method
    public void goBack(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        handleBackNavigation();
    }

    public void handleBackNavigation() {
        vibrate(this, 50);
        finish();
    }

}