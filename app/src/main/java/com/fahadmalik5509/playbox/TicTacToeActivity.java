package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.*;
import com.fahadmalik5509.playbox.databinding.TictactoeLayoutBinding;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

public class TicTacToeActivity extends AppCompatActivity {

    TictactoeLayoutBinding vb;

    private final char[] gameBoard = { '0', '1', '2', '3', '4', '5', '6', '7', '8' };
    private boolean gameWon = false, gameDraw = false, isX = true;
    private int difficulty, playerOneScore = 0, playerTwoScore = 0;

    private Button[] buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = TictactoeLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        loadColors(this);
        loadPreference(this);
        initializeViews();
        setupGameMode();
        animateViewsPulse();
    }

    private void setupGameMode() {
        if (isVsAi) {
            vb.playerOneNameTV.setText(getString(R.string.you));
            vb.playerTwoNameTV.setText(getString(R.string.ai));
            vb.difficultyRL.setVisibility(View.VISIBLE);
            vb.profileIV.setVisibility(View.GONE);
            difficulty = sharedPreferences.getInt(DIFFICULTY_KEY, 1);
            updateDifficultyColor();
        }
    }

    // OnClick Method
    public void handleBoardClick(View view) {

        if (gameWon || gameDraw) {
            playSound(this, R.raw.click_error);
            return;
        }

        playSound(this, R.raw.click_board);
        int i = Integer.parseInt(view.getTag().toString());

        if (isVsAi) {
            handleAIMove(i, buttons[i]);
        } else {
            handlePlayerMove(i, buttons[i]);
        }

    }

    private void handlePlayerMove(int i, Button button) {
        char currentPlayer = toggleAndGetCurrentPlayer();
        updateCardView();
        gameBoard[i] = currentPlayer;
        button.setText(String.valueOf(currentPlayer));
        button.setEnabled(false);
        checkGameState();
    }

    private char toggleAndGetCurrentPlayer() {
        char currentPlayer = isX ? 'X' : 'O';

        isX = !isX;

        return currentPlayer;
    }

    private void handleAIMove(int i, Button button) {
        gameBoard[i] = 'X';
        button.setText("X");
        button.setEnabled(false);
        checkGameState();

        if (!gameWon && !gameDraw) {
            makeAIMove();
        }
    }

    private void makeAIMove() {
        int aiMove = getAIMoveBasedOnDifficulty();

        gameBoard[aiMove] = 'O';
        buttons[aiMove].setText("O");
        buttons[aiMove].setEnabled(false);

        checkGameState();
    }

    private int getAIMoveBasedOnDifficulty() {
        switch (difficulty) {
            case 1:
                return getEasyMove(); // Easy AI: Random move
            case 2:
                return getMediumMove(); // Medium AI: Some strategy
            case 3:
                return getHardMove(); // Hard AI: Optimal strategy
            default:
                return getEasyMove();
        }
    }

    private void checkGameState() {
        if (checkAllLines()) return;

        if (isBoardFull()) {
            isDraw();
        }
    }

    private boolean checkAllLines() {
        for (int i = 0; i < 3; i++) {
            if (checkLine(i * 3, i * 3 + 1, i * 3 + 2)) return true; // Rows
            if (checkLine(i, i + 3, i + 6)) return true; // Columns
        }
        return checkLine(0, 4, 8) || checkLine(2, 4, 6); // Diagonals
    }

    private boolean checkLine(int a, int b, int c) {
        if (gameWon) return false;
        if (gameBoard[a] == gameBoard[b] && gameBoard[b] == gameBoard[c]) {
            isWon(a);
            animateWinningButtons(a, b, c);
            return true;
        }
        return false;
    }

    private void isWon(int a) {
        gameWon = true;
        playSound(this, R.raw.win);

        if(gameBoard[a] == 'X') playerOneScore++;
        else playerTwoScore++;

        vb.playerOneScoreTV.setText(getString(R.string.score, playerOneScore));
        vb.playerTwoScoreTV.setText(getString(R.string.score, playerTwoScore));

        vb.fireworksLAV.setVisibility(View.VISIBLE);
        vb.fireworksLAV.playAnimation();
    }

    private void isDraw() {

        vb.drawIV.setVisibility(View.VISIBLE);
        gameDraw = true;
        playSound(this, R.raw.draw);
    }

    private void animateWinningButtons(int...winBtn) {
        for (int index: winBtn) {
            animateViewScale(buttons[index], 1f, 1.1f, 100);
            changeBackgroundColor(buttons[index], GREEN_COLOR);
        }
    }

    // OnClick Method
    public void onResetGameClicked(View view) {
        playSound(this, R.raw.click_ui);
        resetGameState();
    }

    private void resetGameState() {
        gameWon = false;
        gameDraw = false;
        vb.drawIV.setVisibility(View.GONE);
        vb.fireworksLAV.cancelAnimation();
        vb.fireworksLAV.setVisibility(View.GONE);

        resetBoard();
        resetButtons();
    }

    private void resetBoard() {
        for (int i = 0; i < gameBoard.length; i++) {
            gameBoard[i] = (char)('0' + i);
        }
    }

    private void resetButtons() {
        for (Button button: buttons) {
            button.setText("");
            button.setEnabled(true);
            animateViewScale(button, 1.1f, 1f, 100);
            changeBackgroundColor(button, RED_COLOR);
        }
    }

    private int getEasyMove() {
        int randomNum;
        do {
            randomNum = getRandomNumber(0, 8);
        } while (gameBoard[randomNum] == 'X' || gameBoard[randomNum] == 'O');
        return randomNum;
    }

    private int getMediumMove() {
        int move;

        // Helper function to check for a winning/blocking move in a line
        class Helper {
            int checkLineForMove(int a, int b, int c, char player) {
                if (gameBoard[a] == player && gameBoard[b] == player && gameBoard[c] != 'X' && gameBoard[c] != 'O')
                    return c;
                if (gameBoard[a] == player && gameBoard[c] == player && gameBoard[b] != 'X' && gameBoard[b] != 'O')
                    return b;
                if (gameBoard[b] == player && gameBoard[c] == player && gameBoard[a] != 'X' && gameBoard[a] != 'O')
                    return a;
                return -1;
            }
        }
        Helper helper = new Helper();

        // Check for winning move
        for (int i = 0; i < 3; i++) {
            move = helper.checkLineForMove(i * 3, i * 3 + 1, i * 3 + 2, 'O'); // Rows
            if (move != -1) return move;
            move = helper.checkLineForMove(i, i + 3, i + 6, 'O'); // Columns
            if (move != -1) return move;
        }
        move = helper.checkLineForMove(0, 4, 8, 'O'); // Diagonal
        if (move != -1) return move;
        move = helper.checkLineForMove(2, 4, 6, 'O'); // Reverse Diagonal
        if (move != -1) return move;

        for (int i = 0; i < 3; i++) {
            move = helper.checkLineForMove(i * 3, i * 3 + 1, i * 3 + 2, 'X'); // Rows
            if (move != -1) return move; // Add this if statement
            move = helper.checkLineForMove(i, i + 3, i + 6, 'X'); // Columns
            if (move != -1) return move;
        }
        move = helper.checkLineForMove(0, 4, 8, 'X'); // Diagonal
        if (move != -1) return move;
        move = helper.checkLineForMove(2, 4, 6, 'X'); // Reverse Diagonal
        if (move != -1) return move;

        // If no immediate win or block, pick a random move
        return getEasyMove();
    }

    private int getHardMove() {
        int bestVal = Integer.MIN_VALUE;
        int bestMove = -1;

        for (int i = 0; i < 9; i++) {
            if (gameBoard[i] != 'X' && gameBoard[i] != 'O') {
                gameBoard[i] = 'O';
                int moveVal = minimax(false, Integer.MIN_VALUE, Integer.MAX_VALUE);
                gameBoard[i] = (char)('0' + i);

                if (moveVal > bestVal) {
                    bestMove = i;
                    bestVal = moveVal;
                }
            }
        }
        return bestMove;
    }

    private int minimax(boolean isMax, int alpha, int beta) {
        int score = evaluate();

        if (score == 10) return score;
        if (score == -10) return score;
        if (isBoardFull()) return 0;

        int best;
        if (isMax) {
            best = Integer.MIN_VALUE;
            for (int i = 0; i < 9; i++) {
                if (gameBoard[i] != 'X' && gameBoard[i] != 'O') {
                    gameBoard[i] = 'O';
                    best = Math.max(best, minimax(false, alpha, beta));
                    gameBoard[i] = (char)('0' + i);
                    alpha = Math.max(alpha, best);
                    if (beta <= alpha) break; // Beta cut-off
                }
            }
        } else {
            best = Integer.MAX_VALUE;
            for (int i = 0; i < 9; i++) {
                if (gameBoard[i] != 'X' && gameBoard[i] != 'O') {
                    gameBoard[i] = 'X';
                    best = Math.min(best, minimax(true, alpha, beta));
                    gameBoard[i] = (char)('0' + i);
                    beta = Math.min(beta, best);
                    if (beta <= alpha) break; // Alpha cut-off
                }
            }
        }
        return best;
    }

    private int evaluate() {
        // Check rows, columns and diagonals for win conditions
        for (int i = 0; i < 3; i++) {
            if (gameBoard[i * 3] == gameBoard[i * 3 + 1] && gameBoard[i * 3 + 1] == gameBoard[i * 3 + 2]) {
                if (gameBoard[i * 3] == 'O') return 10;
                if (gameBoard[i * 3] == 'X') return -10;
            }
            if (gameBoard[i] == gameBoard[i + 3] && gameBoard[i + 3] == gameBoard[i + 6]) {
                if (gameBoard[i] == 'O') return 10;
                if (gameBoard[i] == 'X') return -10;
            }
        }
        if (gameBoard[0] == gameBoard[4] && gameBoard[4] == gameBoard[8]) {
            if (gameBoard[0] == 'O') return 10;
            if (gameBoard[0] == 'X') return -10;
        }
        if (gameBoard[2] == gameBoard[4] && gameBoard[4] == gameBoard[6]) {
            if (gameBoard[2] == 'O') return 10;
            if (gameBoard[2] == 'X') return -10;
        }
        return 0;
    }

    private boolean isBoardFull() {
        for (int i = 0; i < 9; i++) {
            if (gameBoard[i] != 'X' && gameBoard[i] != 'O') return false;
        }
        return true;
    }

    // OnClick Method
    public void goToSetting(View view) {
        playSound(this, R.raw.click_ui);
        Intent intent = new Intent(this, SettingActivity.class);
        intent.putExtra("origin_activity", this.getClass().getSimpleName());
        this.startActivity(intent);
    }
    // OnClick Method
    public void goToHome(View view) {
        playSound(this, R.raw.click_ui);
        changeActivity(this, HomeActivity.class, true, false);
    }
    // OnClick Method
    public void goBack(View view) {
        playSound(this, R.raw.click_ui);
        changeActivity(this, GameModeActivity.class, true, false);
    }

    // OnClick Method
    public void difficultyClicked(View view) {
        playSound(this, R.raw.click_ui);

        difficulty++;
        if (difficulty > 3) difficulty = 1;

        saveToSharedPreferences(DIFFICULTY_KEY, difficulty);

        vb.difficultyTooltipTV.setText(getString(R.string.difficulty_tooltip, getDifficultyText()));
        vb.difficultyTooltipTV.setVisibility(View.VISIBLE);
        vb.difficultyTooltipTV.postDelayed(() -> vb.difficultyTooltipTV.setVisibility(View.GONE), 2000);
        updateDifficultyColor();

        playerOneScore = 0;
        playerTwoScore = 0;
        vb.playerOneScoreTV.setText(getString(R.string.score, playerOneScore));
        vb.playerTwoScoreTV.setText(getString(R.string.score, playerTwoScore));
        onResetGameClicked(view);
    }

    private void updateDifficultyColor() {
        int setCurrentColor;
        switch (difficulty) {
            case 1:
                setCurrentColor = GREEN_COLOR;
                break;
            case 2:
                setCurrentColor = YELLOW_COLOR;
                break;
            default:
                setCurrentColor = RED_COLOR;
        }
        changeBackgroundColor(vb.difficultyIV, setCurrentColor);
    }

    private String getDifficultyText() {

        if (difficulty == 1) return getString(R.string.easy);
        if (difficulty == 2) return getString(R.string.medium);
        if (difficulty == 3) return getString(R.string.hard);

        return null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        vibrate(this, 50);
        changeActivity(this, GameModeActivity.class, true, false);
    }

    //OnClick Method
    public void profileClicked(View view) {
            playSound(this,R.raw.click_ui);
            animateViewScale(vb.profileRL,0f,1.0f,200);
            vb.profileRL.setVisibility(View.VISIBLE);
            vb.shadowV.setVisibility(View.VISIBLE);
            vb.playerOneET.setText(sharedPreferences.getString(PLAYERONE_NAME_KEY,"Player 1"));
            vb.playerTwoET.setText(sharedPreferences.getString(PLAYERTWO_NAME_KEY,"Player 2"));
    }

    private void updateProfiles() {

        if(vb.playerOneET.getText().toString().trim().isEmpty()) vb.playerOneET.setText(getString(R.string.player_one));
        if(vb.playerTwoET.getText().toString().trim().isEmpty()) vb.playerTwoET.setText(getString(R.string.player_two));

        saveToSharedPreferences(PLAYERONE_NAME_KEY, vb.playerOneET.getText().toString().trim().replaceAll("\\s", ""));
        saveToSharedPreferences(PLAYERTWO_NAME_KEY, vb.playerTwoET.getText().toString().trim().replaceAll("\\s", ""));
        vb.playerOneNameTV.setText(sharedPreferences.getString(PLAYERONE_NAME_KEY,"Player 1"));
        vb.playerTwoNameTV.setText(sharedPreferences.getString(PLAYERTWO_NAME_KEY,"Player 2"));
    }

    private void updateCardView() {
        if(isX) {
            vb.playerOneCV.setAlpha(1f);
            vb.playerTwoCV.setAlpha(0.8f);
            animateViewScale(vb.playerOneCV, 1f, 1.1f, 200);
            animateViewScale(vb.playerTwoCV, 1.1f, 1f, 200);
        }
        else {
            vb.playerOneCV.setAlpha(0.8f);
            vb.playerTwoCV.setAlpha(1f);
            animateViewScale(vb.playerOneCV,1.1f,1f,200);
            animateViewScale(vb.playerTwoCV, 1f, 1.1f, 200);
        }
    }

    //OnClick Method
    public void handleProfileButtons(View view){
        playSound(this,R.raw.click_ui);
        if(view.getTag().equals("save")) updateProfiles();

        animateViewScale(vb.profileRL,1.0f,0f,200);
        vb.shadowV.setVisibility(View.GONE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void initializeViews() {
        buttons = new Button[] {
                vb.gameBoard0B, vb.gameBoard1B, vb.gameBoard2B,
                vb.gameBoard3B, vb.gameBoard4B, vb.gameBoard5B,
                vb.gameBoard6B, vb.gameBoard7B, vb.gameBoard8B
        };

        vb.playerOneNameTV.setText(sharedPreferences.getString(PLAYERONE_NAME_KEY,"Player 1"));
        vb.playerTwoNameTV.setText(sharedPreferences.getString(PLAYERTWO_NAME_KEY,"Player 2"));
    }

    private void animateViewsPulse() {
        for (Button button: buttons) animateViewPulse(this, button);
        animateViewPulse(this, vb.homeIconIV);
        animateViewPulse(this, vb.settingIconIV);
        animateViewPulse(this, vb.replayTV);
        animateViewPulse(this, vb.backIconIV);
        animateViewPulse(this, vb.difficultyIV);
        animateViewPulse(this, vb.profileIV);
        animateViewPulse(this, vb.profileSaveB);
        animateViewPulse(this, vb.profileExitB);
    }
}