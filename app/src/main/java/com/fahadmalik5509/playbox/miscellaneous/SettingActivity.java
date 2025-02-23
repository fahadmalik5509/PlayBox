package com.fahadmalik5509.playbox.miscellaneous;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.GREEN_COLOR;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.RED_COLOR;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.SOUND_KEY;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.VIBRATION_KEY;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.changeBackgroundColor;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.loadColors;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.loadPreference;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSound;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.saveToSharedPreferences;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.sharedPreferences;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.vibrate;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.databinding.SettingLayoutBinding;

public class SettingActivity extends AppCompatActivity {

    SettingLayoutBinding vb;
    private boolean isSoundEnabled, isVibrationEnabled;

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

        isSoundEnabled = sharedPreferences.getBoolean(SOUND_KEY, true);
        isVibrationEnabled = sharedPreferences.getBoolean(VIBRATION_KEY, true);

        loadColors(this);
        loadPreference(this);
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
        playSound(this, R.raw.sound_ui);
        int color = isSoundEnabled ? GREEN_COLOR : RED_COLOR;
        changeBackgroundColor(vb.soundB, color);
        updateButtonState(vb.soundB, isSoundEnabled, "Sounds ");

        saveToSharedPreferences(SOUND_KEY, isSoundEnabled);
    }

    // OnClick Method
    public void handleVibrationButtonClick(View view) {
        playSound(this, R.raw.sound_ui);
        isVibrationEnabled = !isVibrationEnabled;
        int color = isVibrationEnabled ? GREEN_COLOR : RED_COLOR;
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
        playSound(this, R.raw.sound_ui);
        handleBackNavigation();
    }

    public void handleBackNavigation() {
        vibrate(this, 50);
        finish();
    }

}