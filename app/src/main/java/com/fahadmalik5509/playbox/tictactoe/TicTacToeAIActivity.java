package com.fahadmalik5509.playbox.tictactoe;

import static android.view.View.GONE;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.DARK_RED_COLOR;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.LIGHT_GREEN_COLOR;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.TTT_DIFFICULTY_KEY;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.YELLOW_COLOR;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.animateViewScale;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.changeBackgroundColor;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.saveToSharedPreferences;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.sharedPreferences;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.toggleVisibility;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;

import com.fahadmalik5509.playbox.databinding.NavigationLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShadowLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShopButtonLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShopLayoutBinding;
import com.fahadmalik5509.playbox.miscellaneous.BaseActivity;
import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.databinding.TictactoeaiLayoutBinding;

public class TicTacToeAIActivity extends BaseActivity {

    TictactoeaiLayoutBinding vb;
    TicTacToeLogic game;
    char playerSymbol = ' ', aiSymbol = ' ';
    private int difficulty;
    private Button[] buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = TictactoeaiLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        game = new TicTacToeLogic();
        getBindings();
        initialize();
        setupGameMode();
    }


    private void getBindings() {
        ShopButtonLayoutBinding ShopButtonBinding = ShopButtonLayoutBinding.bind(vb.ShopButton.getRoot());
        ShopLayoutBinding ShopBinding = ShopLayoutBinding.bind(vb.Shop.getRoot());
        NavigationLayoutBinding NavigationBinding = NavigationLayoutBinding.bind(vb.Navigation.getRoot());
        ShadowLayoutBinding ShadowBinding = ShadowLayoutBinding.bind(vb.Shadow.getRoot());
        setBindings(ShopButtonBinding, ShopBinding, NavigationBinding, ShadowBinding);
    }

    private void setupGameMode() {
        toggleVisibility(true, vb.Shadow.ShadowLayout, vb.symbolRL);
        difficulty = sharedPreferences.getInt(TTT_DIFFICULTY_KEY, 1);
        updateDifficultyColor();
    }

    //onClick Method
    public void handleBoardClick(View view) {
        if (game.getHasWon() || game.getHasDraw()) {
            playSoundAndVibrate(this, R.raw.sound_error, false, 0);
            return;
        }
        playSoundAndVibrate(this, R.raw.sound_click, true, 50);
        int playerMove = Integer.parseInt(view.getTag().toString());
        handlePlayerMove(playerMove, buttons[playerMove]);

        if (!game.getHasWon() && !game.getHasDraw()) {
            handleAIMove();
        }
    }

    private void handlePlayerMove(int playerMove, Button button) {

        game.updateGameBoardAndState(playerMove, playerSymbol);

        button.setText(String.valueOf(playerSymbol));
        button.setEnabled(false);

        if(game.getHasWon()) updateWinGUI();
        else if(game.getHasDraw()) updateDrawGUI();
    }

    private void handleAIMove() {

        int aiMove = game.getAIMove(difficulty, playerSymbol, aiSymbol);
        game.updateGameBoardAndState(aiMove, aiSymbol);

        buttons[aiMove].setText(String.valueOf(aiSymbol));
        buttons[aiMove].setEnabled(false);

        if(game.getHasWon()) updateWinGUI();
        else if(game.getHasDraw()) updateDrawGUI();
    }

    private void updateWinGUI() {
        playSoundAndVibrate(this, R.raw.sound_win, true, 100);
        animateWinningButtons(game.winA, game.winB, game.winC);

        updateScore();

        vb.fireworksLAV.setVisibility(View.VISIBLE);
        vb.fireworksLAV.playAnimation();
    }

    private void updateDrawGUI() {
        vb.drawIV.setVisibility(View.VISIBLE);
        playSoundAndVibrate(this, R.raw.sound_draw, true, 100);
    }

    private void animateWinningButtons(int... winBtn) {
        for (int index : winBtn) {
            animateViewScale(buttons[index], 1f, 1.1f, 100);
            changeBackgroundColor(buttons[index], LIGHT_GREEN_COLOR);
        }
    }

    //onClick Method
    public void handleResetClick(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        resetGame();
    }

    private void resetGame() {
        game.resetGameLogic();
        resetGameGUI();

        if (aiSymbol == 'X') handleAIMove();
    }

    private void resetGameGUI() {
        vb.drawIV.setVisibility(GONE);
        vb.fireworksLAV.cancelAnimation();
        vb.fireworksLAV.setVisibility(GONE);

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
    public void difficultyClicked(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        difficulty = (difficulty % 3) + 1;
        saveToSharedPreferences(TTT_DIFFICULTY_KEY, difficulty);
        vb.difficultyTooltipTV.setText(getString(R.string.difficulty_tooltip, getDifficultyText()));
        vb.difficultyTooltipTV.setVisibility(View.VISIBLE);
        vb.difficultyTooltipTV.postDelayed(() -> vb.difficultyTooltipTV.setVisibility(GONE), 2000);
        updateDifficultyColor();
        resetGame();
    }

    private void updateDifficultyColor() {
        int color = switch (difficulty) {
            case 1 -> LIGHT_GREEN_COLOR;
            case 2 -> YELLOW_COLOR;
            default -> DARK_RED_COLOR;
        };
        changeBackgroundColor(vb.difficultyIV, color);
    }

    private void updateScore() {
        vb.playerOneScoreTV.setText(getString(R.string.score, game.getPlayerOneScore()));
        vb.playerTwoScoreTV.setText(getString(R.string.score, game.getPlayerTwoScore()));
    }

    private String getDifficultyText() {
        if (difficulty == 1) return getString(R.string.easy);
        if (difficulty == 2) return getString(R.string.medium);
        if (difficulty == 3) return getString(R.string.hard);
        return "";
    }

    //onClick Method
    public void handleSymbolClick(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        if ("X".equals(view.getTag())) {
            playerSymbol = 'X';
            aiSymbol = 'O';
        } else {
            playerSymbol = 'O';
            aiSymbol = 'X';
            handleAIMove();
        }
        updateSymbol();
        resetGame();
        toggleVisibility(false, vb.symbolRL, vb.Shadow.ShadowLayout);
    }

    private void updateSymbol() {
        vb.playerOneSymbolTV.setText(String.valueOf(playerSymbol));
        vb.playerTwoSymbolTV.setText(String.valueOf(aiSymbol));
    }

    //onClick Method
    public void handleSwitchClick(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        toggleVisibility(true, vb.symbolRL, vb.Shadow.ShadowLayout);
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
    }
}