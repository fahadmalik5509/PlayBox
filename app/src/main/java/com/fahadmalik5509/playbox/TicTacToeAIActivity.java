package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.*;

import com.fahadmalik5509.playbox.databinding.TictactoeaiLayoutBinding;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class TicTacToeAIActivity extends AppCompatActivity {

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

        loadColors(this);
        loadPreference(this);
        initialize();
        animateViewsPulse();
        setupGameMode();
    }

    private void setupGameMode() {
        toggleVisibility(true, vb.shadowV, vb.symbolRL);
        difficulty = sharedPreferences.getInt(DIFFICULTY_KEY, 1);
        updateDifficultyColor();
    }

    //onClick Method
    public void handleBoardClick(View view) {
        if (game.getHasWon() || game.getHasDraw()) {
            playSound(this, R.raw.click_error);
            return;
        }
        playSound(this, R.raw.click_board);
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
        playSound(this, R.raw.win);
        animateWinningButtons(game.winA, game.winB, game.winC);

        updateScore();

        vb.fireworksLAV.setVisibility(View.VISIBLE);
        vb.fireworksLAV.playAnimation();
    }

    private void updateDrawGUI() {
        vb.drawIV.setVisibility(View.VISIBLE);
        playSound(this, R.raw.draw);
    }

    private void animateWinningButtons(int... winBtn) {
        for (int index : winBtn) {
            animateViewScale(buttons[index], 1f, 1.1f, 100);
            changeBackgroundColor(buttons[index], GREEN_COLOR);
        }
    }

    //onClick Method
    public void handleResetClick(View view) {
        playSound(this, R.raw.click_ui);
        resetGame();
    }

    private void resetGame() {
        game.resetGameLogic();
        resetGameGUI();

        if (aiSymbol == 'X') handleAIMove();
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
            changeBackgroundColor(button, RED_COLOR);
        }
    }

    //onClick Method
    public void difficultyClicked(View view) {
        playSound(this, R.raw.click_ui);
        difficulty = (difficulty % 3) + 1;
        saveToSharedPreferences(DIFFICULTY_KEY, difficulty);
        vb.difficultyTooltipTV.setText(getString(R.string.difficulty_tooltip, getDifficultyText()));
        vb.difficultyTooltipTV.setVisibility(View.VISIBLE);
        vb.difficultyTooltipTV.postDelayed(() -> vb.difficultyTooltipTV.setVisibility(View.GONE), 2000);
        updateDifficultyColor();
        resetGame();
    }

    private void updateDifficultyColor() {
        int color = switch (difficulty) {
            case 1 -> GREEN_COLOR;
            case 2 -> YELLOW_COLOR;
            default -> RED_COLOR;
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

    @Override
    public void onBackPressed() {
        vibrate(this, 50);
        changeActivity(this, GameModeActivity.class, true, false);
    }

    //onClick Method
    public void handleSymbolClick(View view) {
        playSound(this, R.raw.click_ui);
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
        toggleVisibility(false, vb.symbolRL, vb.shadowV);
    }

    private void updateSymbol() {
        vb.playerOneSymbolTV.setText(String.valueOf(playerSymbol));
        vb.playerTwoSymbolTV.setText(String.valueOf(aiSymbol));
    }

    //onClick Method
    public void handleSwitchClick(View view) {
        playSound(this, R.raw.click_ui);
        toggleVisibility(true, vb.symbolRL, vb.shadowV);
    }

    //onClick Method
    public void goToSetting(View view) {
        playSound(this, R.raw.click_ui);
        Intent intent = new Intent(this, SettingActivity.class);
        intent.putExtra("origin_activity", getClass().getSimpleName());
        startActivity(intent);
    }

    //onClick Method
    public void goToHome(View view) {
        playSound(this, R.raw.click_ui);
        changeActivity(this, HomeActivity.class, true, false);
    }

    //onClick Method
    public void goBack(View view) {
        playSound(this, R.raw.click_ui);
        changeActivity(this, GameModeActivity.class, true, false);
    }
    private void initialize() {
        buttons = new Button[] {
                vb.gameBoard0B, vb.gameBoard1B, vb.gameBoard2B,
                vb.gameBoard3B, vb.gameBoard4B, vb.gameBoard5B,
                vb.gameBoard6B, vb.gameBoard7B, vb.gameBoard8B
        };
    }
    private void animateViewsPulse() {
        // Animate all game board buttons
        for (Button button : buttons) {
            animateViewPulse(this, button);
        }
        // Animate other UI elements in one loop
        View[] views = { vb.homeIconIV, vb.settingIconIV, vb.replayTV, vb.backIconIV,
                vb.difficultyIV, vb.aiSymbolXTV, vb.aiSymbolOTV, vb.symbolSwitchIV };
        for (View view : views) {
            animateViewPulse(this, view);
        }
    }
}