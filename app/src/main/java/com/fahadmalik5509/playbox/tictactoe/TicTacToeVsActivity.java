package com.fahadmalik5509.playbox.tictactoe;

import static android.view.View.VISIBLE;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.DARK_RED_COLOR;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.LIGHT_GREEN_COLOR;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.TTT_PLAYER_ONE_NAME_KEY;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.TTT_PLAYER_TWO_NAME_KEY;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.animateViewScale;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.changeBackgroundColor;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.saveToSharedPreferences;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.sharedPreferences;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.toggleVisibility;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.databinding.NavigationLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShadowLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShopButtonLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShopLayoutBinding;
import com.fahadmalik5509.playbox.databinding.TictactoevsLayoutBinding;
import com.fahadmalik5509.playbox.miscellaneous.BaseActivity;

public class TicTacToeVsActivity extends BaseActivity {

    TictactoevsLayoutBinding vb;
    TicTacToeLogic game;
    private boolean switchTurn = true;
    private Button[] buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = TictactoevsLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        game = new TicTacToeLogic();

        getBindings();
        initialize();
    }

    private void getBindings() {
        ShopButtonLayoutBinding ShopButtonBinding = ShopButtonLayoutBinding.bind(vb.ShopButton.getRoot());
        ShopLayoutBinding ShopBinding = ShopLayoutBinding.bind(vb.Shop.getRoot());
        NavigationLayoutBinding NavigationBinding = NavigationLayoutBinding.bind(vb.Navigation.getRoot());
        ShadowLayoutBinding ShadowBinding = ShadowLayoutBinding.bind(vb.Shadow.getRoot());
        setBindings(ShopButtonBinding, ShopBinding, NavigationBinding, ShadowBinding);
    }

    //onClick Method
    public void handleBoardClick(View view) {
        if (game.getHasWon() || game.getHasDraw()) {
            playSoundAndVibrate(this, R.raw.sound_error, false, 0);
            return;
        }
        playSoundAndVibrate(this, R.raw.sound_click, true, 50);
        int move = Integer.parseInt(view.getTag().toString());
        handlePlayerMove(move, buttons[move]);
    }

    private void handlePlayerMove(int i, Button button) {

        char currentPlayer = toggleAndGetCurrentPlayerSymbol();
        updateCardView();

        game.updateGameBoardAndState(i,currentPlayer);

        button.setText(String.valueOf(currentPlayer));
        button.setEnabled(false);

        if(game.getHasWon()) updateWinGUI();
        else if(game.getHasDraw()) updateDrawGUI();
    }

    private char toggleAndGetCurrentPlayerSymbol() {
        char currentPlayer = switchTurn ? 'X' : 'O';
        switchTurn = !switchTurn;
        return currentPlayer;
    }

    private void updateWinGUI() {
        playSoundAndVibrate(this, R.raw.sound_win, true, 100);
        animateWinningButtons(game.winA, game.winB, game.winC);

        updateScore();

        vb.fireworksLAV.setVisibility(VISIBLE);
        vb.fireworksLAV.playAnimation();
    }

    private void updateDrawGUI() {
        playSoundAndVibrate(this, R.raw.sound_draw, true, 100);
        vb.drawIV.setVisibility(VISIBLE);
    }

    private void animateWinningButtons(int... winBtn) {
        for (int index : winBtn) {
            animateViewScale(buttons[index], 1f, 1.1f, 100);
            changeBackgroundColor(buttons[index], LIGHT_GREEN_COLOR);
        }
    }

    private void updateScore() {
        vb.playerOneScoreTV.setText(getString(R.string.score, game.getPlayerOneScore()));
        vb.playerTwoScoreTV.setText(getString(R.string.score, game.getPlayerTwoScore()));
    }

    //onClick Method
    public void handleResetClick(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        resetGame();

    }

    private void resetGame() {

        game.resetGameLogic();
        resetGameGUI();
    }

    private void resetGameGUI() {
        vb.drawIV.setVisibility(View.GONE);
        vb.fireworksLAV.cancelAnimation();
        vb.fireworksLAV.setVisibility(View.GONE);

        resetButtons();
    }

    private void resetButtons() {
        for (Button button : buttons) {
            button.setText("");
            button.setEnabled(true);
            animateViewScale(button, 1.1f, 1f, 100);
            changeBackgroundColor(button, DARK_RED_COLOR);
        }
    }

    //onClick Method
    public void handleProfileClick(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        animateViewScale(vb.profileRL, 0f, 1.0f, 200);
        vb.profileRL.setVisibility(VISIBLE);
        vb.Shadow.ShadowLayout.setVisibility(VISIBLE);
        vb.playerOneET.setText(sharedPreferences.getString(TTT_PLAYER_ONE_NAME_KEY, "Player 1"));
        vb.playerTwoET.setText(sharedPreferences.getString(TTT_PLAYER_TWO_NAME_KEY, "Player 2"));
    }

    public void handleProfileButtons(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        if ("save".equals(view.getTag())) updateProfiles();
        animateViewScale(vb.profileRL, 1.0f, 0f, 200);
        vb.Shadow.ShadowLayout.setVisibility(View.GONE);
        vb.profileRL.setVisibility(View.GONE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void updateProfiles() {
        if (vb.playerOneET.getText().toString().trim().isEmpty())
            vb.playerOneET.setText(getString(R.string.player_one));
        if (vb.playerTwoET.getText().toString().trim().isEmpty())
            vb.playerTwoET.setText(getString(R.string.player_two));

        String playerOneName = vb.playerOneET.getText().toString().trim().replaceAll("\\s", "");
        String playerTwoName = vb.playerTwoET.getText().toString().trim().replaceAll("\\s", "");
        saveToSharedPreferences(TTT_PLAYER_ONE_NAME_KEY, playerOneName);
        saveToSharedPreferences(TTT_PLAYER_TWO_NAME_KEY, playerTwoName);

        vb.playerOneNameTV.setText(sharedPreferences.getString(TTT_PLAYER_ONE_NAME_KEY, "Player 1"));
        vb.playerTwoNameTV.setText(sharedPreferences.getString(TTT_PLAYER_TWO_NAME_KEY, "Player 2"));
    }

    private void updateCardView() {
        if (switchTurn) {
            vb.playerOneCV.setAlpha(1f);
            vb.playerTwoCV.setAlpha(0.8f);
            animateViewScale(vb.playerOneCV, 1f, 1.1f, 200);
            animateViewScale(vb.playerTwoCV, 1.1f, 1f, 200);
        } else {
            vb.playerOneCV.setAlpha(0.8f);
            vb.playerTwoCV.setAlpha(1f);
            animateViewScale(vb.playerOneCV, 1.1f, 1f, 200);
            animateViewScale(vb.playerTwoCV, 1f, 1.1f, 200);
        }
    }

    @Override
    public void backLogic() {
        if(vb.profileRL.getVisibility() == VISIBLE) {
            toggleVisibility(false, vb.profileRL, vb.Shadow.ShadowLayout);
            return;
        }
        super.backLogic();
    }


    @Override
    protected Class<?> getBackDestination() {
        return GameModeActivity.class;
    }
    private void initialize() {
        buttons = new Button[] {
                vb.gameBoard0B, vb.gameBoard1B, vb.gameBoard2B,
                vb.gameBoard3B, vb.gameBoard4B, vb.gameBoard5B,
                vb.gameBoard6B, vb.gameBoard7B, vb.gameBoard8B
        };
        vb.playerOneNameTV.setText(sharedPreferences.getString(TTT_PLAYER_ONE_NAME_KEY, "Player 1"));
        vb.playerTwoNameTV.setText(sharedPreferences.getString(TTT_PLAYER_TWO_NAME_KEY, "Player 2"));
    }
}