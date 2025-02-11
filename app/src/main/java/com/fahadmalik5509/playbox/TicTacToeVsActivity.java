package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.*;
import com.fahadmalik5509.playbox.databinding.TictactoevsLayoutBinding;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

public class TicTacToeVsActivity extends AppCompatActivity {

    TictactoevsLayoutBinding vb;
    TicTacToeLogic game;
    private boolean isX = true;
    private Button[] buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = TictactoevsLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());
        game = new TicTacToeLogic();

        loadColors(this);
        loadPreference(this);
        initialize();
        animateViewsPulse();
    }

    // Called when a board cell is clicked.
    public void handleBoardClick(View view) {
        if (game.getHasWon() || game.getHasDraw()) {
            playSound(this, R.raw.click_error);
            return;
        }
        playSound(this, R.raw.click_board);
        int move = Integer.parseInt(view.getTag().toString());
        handlePlayerMove(move, buttons[move]);
    }

    private void handlePlayerMove(int i, Button button) {

        char currentPlayer = toggleAndGetCurrentPlayerSymbol();
        updateCardView();

        game.updateGameBoardAndState(i,currentPlayer);

        button.setText(String.valueOf(currentPlayer));
        button.setEnabled(false);

        if(game.getHasWon()) isWon();
        else if(game.getHasDraw()) isDraw();
    }

    // Toggle between 'X' and 'O' and return the current player's symbol.
    private char toggleAndGetCurrentPlayerSymbol() {
        char currentPlayer = isX ? 'X' : 'O';
        isX = !isX;
        return currentPlayer;
    }

    private void isWon() {
        playSound(this, R.raw.win);
        animateWinningButtons(game.winA, game.winB, game.winC);

        updateScore();

        vb.fireworksLAV.setVisibility(View.VISIBLE);
        vb.fireworksLAV.playAnimation();
    }

    private void isDraw() {
        playSound(this, R.raw.draw);
        vb.drawIV.setVisibility(View.VISIBLE);
    }

    private void animateWinningButtons(int... winBtn) {
        for (int index : winBtn) {
            animateViewScale(buttons[index], 1f, 1.1f, 100);
            changeBackgroundColor(buttons[index], GREEN_COLOR);
        }
    }

    private void updateScore() {
        vb.playerOneScoreTV.setText(getString(R.string.score, game.getPlayerOneScore()));
        vb.playerTwoScoreTV.setText(getString(R.string.score, game.getPlayerTwoScore()));
    }

    // Resets the game state and board.
    public void onResetGameClicked(View view) {
        playSound(this, R.raw.click_ui);
        resetGameState();
    }

    private void resetGameState() {
        game.resetGameLogic();

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

    // Navigation methods.
    public void goToSetting(View view) {
        playSound(this, R.raw.click_ui);
        Intent intent = new Intent(this, SettingActivity.class);
        intent.putExtra("origin_activity", getClass().getSimpleName());
        startActivity(intent);
    }

    public void goToHome(View view) {
        playSound(this, R.raw.click_ui);
        changeActivity(this, HomeActivity.class, true, false);
    }

    public void goBack(View view) {
        playSound(this, R.raw.click_ui);
        changeActivity(this, GameModeActivity.class, true, false);
    }

    @Override
    public void onBackPressed() {
        vibrate(this, 50);
        changeActivity(this, GameModeActivity.class, true, false);
    }

    // Displays the profile editing view.
    public void profileClicked(View view) {
        playSound(this, R.raw.click_ui);
        animateViewScale(vb.profileRL, 0f, 1.0f, 200);
        vb.profileRL.setVisibility(View.VISIBLE);
        vb.shadowV.setVisibility(View.VISIBLE);
        vb.playerOneET.setText(sharedPreferences.getString(PLAYERONE_NAME_KEY, "Player 1"));
        vb.playerTwoET.setText(sharedPreferences.getString(PLAYERTWO_NAME_KEY, "Player 2"));
    }

    // Handles the profile view's save/exit actions.
    public void handleProfileButtons(View view) {
        playSound(this, R.raw.click_ui);
        if ("save".equals(view.getTag())) updateProfiles();
        animateViewScale(vb.profileRL, 1.0f, 0f, 200);
        vb.shadowV.setVisibility(View.GONE);
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
        saveToSharedPreferences(PLAYERONE_NAME_KEY, playerOneName);
        saveToSharedPreferences(PLAYERTWO_NAME_KEY, playerTwoName);

        vb.playerOneNameTV.setText(sharedPreferences.getString(PLAYERONE_NAME_KEY, "Player 1"));
        vb.playerTwoNameTV.setText(sharedPreferences.getString(PLAYERTWO_NAME_KEY, "Player 2"));
    }

    // Updates the card views' alpha and scale based on the active player.
    private void updateCardView() {
        if (isX) {
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

    private void initialize() {
        buttons = new Button[] {
                vb.gameBoard0B, vb.gameBoard1B, vb.gameBoard2B,
                vb.gameBoard3B, vb.gameBoard4B, vb.gameBoard5B,
                vb.gameBoard6B, vb.gameBoard7B, vb.gameBoard8B
        };
        vb.playerOneNameTV.setText(sharedPreferences.getString(PLAYERONE_NAME_KEY, "Player 1"));
        vb.playerTwoNameTV.setText(sharedPreferences.getString(PLAYERTWO_NAME_KEY, "Player 2"));
    }

    // Combines animation calls for multiple UI elements.
    private void animateViewsPulse() {
        for (Button button : buttons) {
            animateViewPulse(this, button);
        }
        View[] views = {
                vb.homeIconIV, vb.settingIconIV, vb.replayTV,
                vb.backIconIV, vb.profileIV, vb.profileSaveB, vb.profileExitB
        };
        for (View view : views) {
            animateViewPulse(this, view);
        }
    }
}