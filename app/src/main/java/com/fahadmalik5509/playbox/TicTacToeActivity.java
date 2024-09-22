package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.*;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.fahadmalik5509.playbox.databinding.TictactoeLayoutBinding;

public class TicTacToeActivity extends AppCompatActivity {

    TictactoeLayoutBinding wb;

    private final char[] board = { '0', '1', '2', '3', '4', '5', '6', '7', '8' };
    private boolean gameWon = false, gameDraw = false, isX = true;
    private int difficulty, playerOneScore = 0, playerTwoScore = 0;

    private Button[] buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wb = TictactoeLayoutBinding.inflate(getLayoutInflater());
        setContentView(wb.getRoot());

        loadColors(this);
        loadPreference(this);
        initializeViews();
        setupGameMode();
        animateViewsPulse();
    }

    private void setupGameMode() {
        if (isVsAi) {
            wb.playerOneNameTV.setText("YOU");
            wb.playerTwoNameTV.setText("AI");
            wb.difficultyRL.setVisibility(View.VISIBLE);
            wb.profileIV.setVisibility(View.GONE);
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
        board[i] = currentPlayer;
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
        board[i] = 'X';
        button.setText("X");
        button.setEnabled(false);
        checkGameState();

        if (!gameWon && !gameDraw) {
            makeAIMove();
        }
    }

    private void makeAIMove() {
        int aiMove = getAIMoveBasedOnDifficulty();

        board[aiMove] = 'O';
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
        for (int i = 0; i < 3; i++) {
            checkLine(i * 3, i * 3 + 1, i * 3 + 2); // Rows
            checkLine(i, i + 3, i + 6); // Columns
        }

        checkLine(0, 4, 8); // Diagonal
        checkLine(2, 4, 6); // Reverse Diagonal

        if (isBoardFull()) {
            isDraw();
        }
    }

    private void checkLine(int a, int b, int c) {
        if (gameWon) return;
        if (board[a] == board[b] && board[b] == board[c]) {
            isWon(a);
            animateWinningButtons(a, b, c);
        }
    }

    private void isWon(int a) {
        gameWon = true;
        playSound(this, R.raw.win);

        if(board[a] == 'X') playerOneScore++;
        else playerTwoScore++;

        wb.playerOneScoreTV.setText("Score: " + playerOneScore);
        wb.playerTwoScoreTV.setText("Score: " + playerTwoScore);

        wb.fireworksLAV.setVisibility(View.VISIBLE);
        wb.fireworksLAV.playAnimation();
    }

    private void isDraw() {

        wb.drawIV.setVisibility(View.VISIBLE);
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
        wb.drawIV.setVisibility(View.GONE);
        wb.fireworksLAV.cancelAnimation();
        wb.fireworksLAV.setVisibility(View.GONE);

        resetBoard();
        resetButtons();
    }

    private void resetBoard() {
        for (int i = 0; i < board.length; i++) {
            board[i] = (char)('0' + i);
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
        } while (board[randomNum] == 'X' || board[randomNum] == 'O');
        return randomNum;
    }

    private int getMediumMove() {
        int move;

        // Helper function to check for a winning/blocking move in a line
        class Helper {
            int checkLineForMove(int a, int b, int c, char player) {
                if (board[a] == player && board[b] == player && board[c] != 'X' && board[c] != 'O')
                    return c;
                if (board[a] == player && board[c] == player && board[b] != 'X' && board[b] != 'O')
                    return b;
                if (board[b] == player && board[c] == player && board[a] != 'X' && board[a] != 'O')
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
            if (board[i] != 'X' && board[i] != 'O') {
                board[i] = 'O';
                int moveVal = minimax(false, Integer.MIN_VALUE, Integer.MAX_VALUE);
                board[i] = (char)('0' + i);

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
                if (board[i] != 'X' && board[i] != 'O') {
                    board[i] = 'O';
                    best = Math.max(best, minimax(false, alpha, beta));
                    board[i] = (char)('0' + i);
                    alpha = Math.max(alpha, best);
                    if (beta <= alpha) break; // Beta cut-off
                }
            }
        } else {
            best = Integer.MAX_VALUE;
            for (int i = 0; i < 9; i++) {
                if (board[i] != 'X' && board[i] != 'O') {
                    board[i] = 'X';
                    best = Math.min(best, minimax(true, alpha, beta));
                    board[i] = (char)('0' + i);
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
            if (board[i * 3] == board[i * 3 + 1] && board[i * 3 + 1] == board[i * 3 + 2]) {
                if (board[i * 3] == 'O') return 10;
                if (board[i * 3] == 'X') return -10;
            }
            if (board[i] == board[i + 3] && board[i + 3] == board[i + 6]) {
                if (board[i] == 'O') return 10;
                if (board[i] == 'X') return -10;
            }
        }
        if (board[0] == board[4] && board[4] == board[8]) {
            if (board[0] == 'O') return 10;
            if (board[0] == 'X') return -10;
        }
        if (board[2] == board[4] && board[4] == board[6]) {
            if (board[2] == 'O') return 10;
            if (board[2] == 'X') return -10;
        }
        return 0;
    }

    private boolean isBoardFull() {
        for (int i = 0; i < 9; i++) {
            if (board[i] != 'X' && board[i] != 'O') return false;
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

        wb.difficultyTooltipTV.setText("AI Difficulty\n(" + getDifficultyText() + ")");
        wb.difficultyTooltipTV.setVisibility(View.VISIBLE);
        wb.difficultyTooltipTV.postDelayed(() -> wb.difficultyTooltipTV.setVisibility(View.GONE), 2000);
        updateDifficultyColor();

        playerOneScore = 0;
        playerTwoScore = 0;
        wb.playerOneScoreTV.setText("Score: " + playerOneScore);
        wb.playerTwoScoreTV.setText("Score: " + playerTwoScore);
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
        changeBackgroundColor(wb.difficultyIV, setCurrentColor);
    }

    private String getDifficultyText() {

        if (difficulty == 1) return "Easy";
        if (difficulty == 2) return "Medium";
        if (difficulty == 3) return "Hard";

        return null;
    }

    @Override
    public void onBackPressed() {
        vibrate(this, 50);
        changeActivity(this, GameModeActivity.class, true, false);
    }

    //OnClick Method
    public void profileClicked(View view) {
            playSound(this,R.raw.click_ui);
            animateViewScale(wb.profileRL,0f,1.0f,200);
            wb.profileRL.setVisibility(View.VISIBLE);
            wb.shadowV.setVisibility(View.VISIBLE);
            wb.playerOneET.setText(sharedPreferences.getString(PLAYERONE_NAME_KEY,"Player 1"));
            wb.playerTwoET.setText(sharedPreferences.getString(PLAYERTWO_NAME_KEY,"Player 2"));
    }

    private void updateProfiles() {

        if(wb.playerOneET.getText().toString().trim().isEmpty()) wb.playerOneET.setText("Player⠀1");
        if(wb.playerTwoET.getText().toString().trim().isEmpty()) wb.playerTwoET.setText("Player⠀2");

        saveToSharedPreferences(PLAYERONE_NAME_KEY,wb.playerOneET.getText().toString().trim().replaceAll("\\s", ""));
        saveToSharedPreferences(PLAYERTWO_NAME_KEY,wb.playerTwoET.getText().toString().trim().replaceAll("\\s", ""));
        wb.playerOneNameTV.setText(sharedPreferences.getString(PLAYERONE_NAME_KEY,"Player 1"));
        wb.playerTwoNameTV.setText(sharedPreferences.getString(PLAYERTWO_NAME_KEY,"Player 2"));
    }

    private void updateCardView() {
        if(isX) {
            wb.playerOneCV.setAlpha(1f);
            wb.playerTwoCV.setAlpha(0.8f);
            animateViewScale(wb.playerOneCV, 1f, 1.1f, 200);
            animateViewScale(wb.playerTwoCV, 1.1f, 1f, 200);
        }
        else {
            wb.playerOneCV.setAlpha(0.8f);
            wb.playerTwoCV.setAlpha(1f);
            animateViewScale(wb.playerOneCV,1.1f,1f,200);
            animateViewScale(wb.playerTwoCV, 1f, 1.1f, 200);
        }
    }

    //OnClick Method
    public void handleProfileButtons(View view){
        playSound(this,R.raw.click_ui);
        if(view.getTag().equals("save")) updateProfiles();

        animateViewScale(wb.profileRL,1.0f,0f,200);
        wb.shadowV.setVisibility(View.GONE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void initializeViews() {
        buttons = new Button[] {
                wb.gameBoard0B, wb.gameBoard1B, wb.gameBoard2B,
                wb.gameBoard3B, wb.gameBoard4B, wb.gameBoard5B,
                wb.gameBoard6B, wb.gameBoard7B, wb.gameBoard8B
        };

        wb.playerOneNameTV.setText(sharedPreferences.getString(PLAYERONE_NAME_KEY,"Player 1"));
        wb.playerTwoNameTV.setText(sharedPreferences.getString(PLAYERTWO_NAME_KEY,"Player 2"));
    }

    private void animateViewsPulse() {
        for (Button button: buttons) animateViewPulse(this, button);
        animateViewPulse(this, wb.homeIconIV);
        animateViewPulse(this, wb.settingIconIV);
        animateViewPulse(this, wb.replayTV);
        animateViewPulse(this, wb.backIconIV);
        animateViewPulse(this, wb.difficultyIV);
        animateViewPulse(this, wb.profileIV);
        animateViewPulse(this, wb.profileSaveB);
        animateViewPulse(this, wb.profileExitB);
    }
}