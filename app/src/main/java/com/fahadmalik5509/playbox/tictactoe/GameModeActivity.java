package com.fahadmalik5509.playbox.tictactoe;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.changeActivity;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.vibrate;

import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;

import com.fahadmalik5509.playbox.miscellaneous.BaseActivity;
import com.fahadmalik5509.playbox.miscellaneous.GamesActivity;
import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.databinding.GamemodeLayoutBinding;

public class GameModeActivity extends BaseActivity {

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

    @Override
    protected Class<?> getBackDestination() {
        return GamesActivity.class;
    }
}