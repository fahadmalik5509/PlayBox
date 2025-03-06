package com.fahadmalik5509.playbox.miscellaneous;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.changeActivity;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import com.fahadmalik5509.playbox.R;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public void goBack(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        changeActivity(this, getBackDestination());
    }

    // Called via XML onClick for the home icon.
    public void goToHome(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        changeActivity(this, HomeActivity.class);
    }

    // Called via XML onClick for the settings icon.
    public void goToSetting(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        Intent intent = new Intent(this, SettingActivity.class);
        intent.putExtra("origin_activity", this.getClass().getSimpleName());
        startActivity(intent);
    }

    // Only back destination is abstract because it varies between activities.
    protected abstract Class<?> getBackDestination();
}
