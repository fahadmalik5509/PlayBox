package com.fahadmalik5509.playbox.miscellaneous;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.changeActivity;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.vibrate;

import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;

import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.colorpuzzle.ColorPuzzleActivity;
import com.fahadmalik5509.playbox.databinding.GamesLayoutBinding;
import com.fahadmalik5509.playbox.tictactoe.GameModeActivity;
import com.fahadmalik5509.playbox.wordle.WordleActivity;


public class GamesActivity extends BaseActivity {
    GamesLayoutBinding vb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = GamesLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        // Handle back button press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackNavigation();
            }
        });
    }

    public void handleTicTacToeButtonClick(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        changeActivity(this, GameModeActivity.class);
    }
    public void handleWordleButtonClick(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        changeActivity(this, WordleActivity.class);
    }
    public void handleColorPuzzleButtonClick(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        changeActivity(this, ColorPuzzleActivity.class);
    }

    private void handleBackNavigation() {
        vibrate(this, 50);
        changeActivity(this, HomeActivity.class);
    }
    @Override
    protected Class<?> getBackDestination() {
        return HomeActivity.class;
    }

}
