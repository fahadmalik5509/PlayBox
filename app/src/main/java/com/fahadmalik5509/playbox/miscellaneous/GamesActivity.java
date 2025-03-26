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
import com.fahadmalik5509.playbox.databinding.NavigationLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShadowLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShopButtonLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShopLayoutBinding;
import com.fahadmalik5509.playbox.dotandboxes.DotAndBoxesActivity;
import com.fahadmalik5509.playbox.tictactoe.GameModeActivity;
import com.fahadmalik5509.playbox.wordle.WordleActivity;

public class GamesActivity extends BaseActivity {
    GamesLayoutBinding vb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = GamesLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        getBindings();
    }

    private void getBindings() {
        ShopButtonLayoutBinding ShopButtonBinding = ShopButtonLayoutBinding.bind(vb.ShopButton.getRoot());
        ShopLayoutBinding ShopBinding = ShopLayoutBinding.bind(vb.Shop.getRoot());
        NavigationLayoutBinding NavigationBinding = NavigationLayoutBinding.bind(vb.Navigation.getRoot());
        ShadowLayoutBinding ShadowBinding = ShadowLayoutBinding.bind(vb.Shadow.getRoot());
        setBindings(ShopButtonBinding, ShopBinding, NavigationBinding, ShadowBinding);
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

    public void handleDotAndBoxesButtonClick(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        changeActivity(this, DotAndBoxesActivity.class);
    }

    @Override
    protected Class<?> getBackDestination() {
        return HomeActivity.class;
    }

}
