package com.fahadmalik.playbox.tictactoe;

import static com.fahadmalik.playbox.miscellaneous.ActivityUtils.changeActivity;
import static com.fahadmalik.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;

import android.os.Bundle;
import android.view.View;

import com.fahadmalik.playbox.databinding.NavigationLayoutBinding;
import com.fahadmalik.playbox.databinding.ShadowLayoutBinding;
import com.fahadmalik.playbox.databinding.ShopButtonLayoutBinding;
import com.fahadmalik.playbox.databinding.ShopLayoutBinding;
import com.fahadmalik.playbox.miscellaneous.BaseActivity;
import com.fahadmalik.playbox.miscellaneous.GamesActivity;
import com.fahadmalik.playbox.R;
import com.fahadmalik.playbox.databinding.GamemodeLayoutBinding;

public class GameModeActivity extends BaseActivity {

    GamemodeLayoutBinding vb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = GamemodeLayoutBinding.inflate(getLayoutInflater());
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

    public void handlePvPClick(View view) {

        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        changeActivity(this, TicTacToeVsActivity.class);

    }

    public void handlePvAClick(View view) {

        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        changeActivity(this, TicTacToeAIActivity.class);
    }

    @Override
    protected Class<?> getBackDestination() {
        return GamesActivity.class;
    }
}