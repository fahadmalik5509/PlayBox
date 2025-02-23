package com.fahadmalik5509.playbox.tictactoe;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.changeActivity;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.loadColors;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.vibrate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.fahadmalik5509.playbox.miscellaneous.GamesActivity;
import com.fahadmalik5509.playbox.miscellaneous.HomeActivity;
import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.miscellaneous.SettingActivity;
import com.fahadmalik5509.playbox.databinding.GamemodeLayoutBinding;

public class GameModeActivity extends AppCompatActivity {

    GamemodeLayoutBinding vb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = GamemodeLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackNavigation();
            }
        });

        loadColors(this);
    }

    public void handlePvPClick(View view) {

        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        changeActivity(this, TicTacToeVsActivity.class);

    }

    public void handlePvAClick(View view) {

        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        changeActivity(this, TicTacToeAIActivity.class);
    }

    private void handleBackNavigation() {
        vibrate(this, 50);
        changeActivity(this, GamesActivity.class);
    }

    // OnClick Method
    public void goToSetting(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        Intent intent = new Intent(this, SettingActivity.class);
        intent.putExtra("origin_activity", this.getClass().getSimpleName());
        this.startActivity(intent);
    }

    //onclick Method
    public void goToHome(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        changeActivity(this, HomeActivity.class);
    }

    //onClick Method
    public void goBack(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        changeActivity(this, GamesActivity.class);
    }
}