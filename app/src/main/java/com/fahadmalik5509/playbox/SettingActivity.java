package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.*;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {

    private boolean isSoundEnabled, isVibrationEnabled;
    private Button soundButton, vibrationButton, difficultyButton, easyButton, mediumButton, hardButton;
    private SharedPreferences sharedPreferences;
    private LinearLayout difficultyLayout;
    private ImageView homeImageView,backImageView;
    private String originActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);

        originActivity = getIntent().getStringExtra("origin_activity");

        initializeViews();
        loadPreferences();
        updateButtonStates();
        animateViewsPulse();
    }

    private void loadPreferences() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isSoundEnabled = sharedPreferences.getBoolean(SOUND_KEY, true);
        isVibrationEnabled = sharedPreferences.getBoolean(VIBRATION_KEY, true);
    }

    private void updateButtonStates() {
        changeButtonColor(soundButton, isSoundEnabled ? GREEN_COLOR : RED_COLOR);
        changeButtonColor(vibrationButton, isVibrationEnabled ? GREEN_COLOR : RED_COLOR);
        updateButtonState(soundButton, isSoundEnabled, "Sounds ");
        updateButtonState(vibrationButton, isVibrationEnabled, "Vibration ");
        updateDifficultyButtonColor();
    }

    //onclick Method
    public void handleSoundButtonClick(View view) {
        isSoundEnabled = !isSoundEnabled;
        playSound(this,R.raw.click_ui);
        int color = isSoundEnabled ? GREEN_COLOR : RED_COLOR;
        changeButtonColor(soundButton,color);
        updateButtonState(soundButton,isSoundEnabled,"Sounds ");

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SOUND_KEY, isSoundEnabled);
        editor.apply();

    }

    //onclick Method
    public void handleVibrationButtonClick(View view) {
        playSound(this,R.raw.click_ui);
        isVibrationEnabled = !isVibrationEnabled;
        int color = isVibrationEnabled ? GREEN_COLOR : RED_COLOR;
        changeButtonColor(vibrationButton,color);
        updateButtonState(vibrationButton,isVibrationEnabled,"Vibration ");

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(VIBRATION_KEY, isVibrationEnabled);
        editor.apply();
    }

    public void handleDifficultyButtonClick(View view) {
        playSound(this,R.raw.click_ui);
        difficultyLayout.setVisibility(View.VISIBLE);
    }

    //onclick Method
    public void handleEasyButtonClick(View view) {
        updateDifficulty(1);
    }
    //onclick Method
    public void handleMediumButtonClick(View view) { updateDifficulty(2); }
    //onclick Method
    public void handleHardButtonClick(View view) {
        updateDifficulty(3);
    }
    //onclick Method

    public void updateDifficulty(int difficulty) {
        playSound(this,R.raw.click_ui);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(DIFFICULTY_KEY, difficulty);
        editor.apply();
        updateDifficultyButtonColor();
        new Handler().postDelayed(() -> difficultyLayout.setVisibility(View.GONE), ACTIVITY_TRANSITION_DELAY_MS + 100);
    }

    private void updateButtonState(Button button, boolean isEnabled, String text) {
        String buttonText = getString(isEnabled ? R.string.buttonStateOn : R.string.buttonStateOff, text);
        button.setText(buttonText);
    }

    private void updateDifficultyButtonColor() {
        int difficulty = sharedPreferences.getInt(DIFFICULTY_KEY, 1);
        changeButtonColor(easyButton, difficulty == 1 ? GREEN_COLOR : RED_COLOR);
        changeButtonColor(mediumButton, difficulty == 2 ? GREEN_COLOR : RED_COLOR);
        changeButtonColor(hardButton, difficulty == 3 ? GREEN_COLOR : RED_COLOR);
    }

    //onclick Method
    public void goToHome(View view) {
        playSound(this,R.raw.click_ui);
        changeActivity(this, HomeActivity.class,true,false);
    }

    // OnClick Method
    public void goBack(View view) {
        playSound(this,R.raw.click_ui);
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

    private void initializeViews() {
        soundButton = findViewById(R.id.bSound);
        vibrationButton = findViewById(R.id.bVibration);
        difficultyButton = findViewById(R.id.bDifficulty);
        easyButton = findViewById(R.id.easyDifficulty);
        mediumButton = findViewById(R.id.mediumDifficulty);
        hardButton = findViewById(R.id.hardDifficulty);
        difficultyLayout = findViewById(R.id.difficultyLayout);
        homeImageView = findViewById(R.id.ivHomeIcon);
        backImageView = findViewById(R.id.ivBackIcon);
    }
    private void animateViewsPulse() {
        animateViewPulse(this, soundButton);
        animateViewPulse(this, vibrationButton);
        animateViewPulse(this, difficultyButton);
        animateViewPulse(this, easyButton);
        animateViewPulse(this, mediumButton);
        animateViewPulse(this, hardButton);
        animateViewPulse(this, homeImageView);
        animateViewPulse(this, backImageView);
    }
}
