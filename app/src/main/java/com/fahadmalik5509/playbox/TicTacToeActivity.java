package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.*;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class TicTacToeActivity extends AppCompatActivity {

    private final char[] board = {'0', '1', '2', '3', '4', '5', '6', '7', '8'};
    private boolean gameWon = false, gameDraw = false;
    private TextView gameStatusTextView, replayTextView;
    private ImageView drawImageView, homeImageView, settingImageView, backImageView;
    private int difficulty;
    private final Button[] buttons = new Button[9];
    private boolean isX = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tictactoe_layout);

        initializeViews();
        gameStatusTextView.setText(isVsAi ? "You're X" : "Turn: " + getCurrentPlayer(false));
        animateViewsPulse();
    }

    // OnClick Method
    public void handleBoardClick(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        difficulty = sharedPreferences.getInt(DIFFICULTY_KEY, 1);

        if (gameWon || gameDraw) {
            playSound(this, R.raw.click_error);
            return;
        }

        playSound(this, R.raw.click_btn);
        int i = Integer.parseInt(view.getTag().toString());

        if (isVsAi) {
            handleAIMove(i, buttons[i]);
        } else {
            handlePlayerMove(i, buttons[i]);
        }
    }

    private char getCurrentPlayer(boolean changePlayer) {
        char currentPlayer = isX ? 'X' : 'O'; // Get current player

        if (changePlayer) {
            isX = !isX; // Toggle the player after returning the value
        }

        return currentPlayer; // Return current player first, then toggle if required
    }

    private void handlePlayerMove(int i, Button button) {
        char currentPlayer = getCurrentPlayer(true);
        gameStatusTextView.setText("Turn: " + getCurrentPlayer(false));
        board[i] = currentPlayer;
        button.setText(String.valueOf(currentPlayer));
        button.setEnabled(false);
        checkGameState();
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
            checkLine(i * 3, i * 3 + 1, i * 3 + 2);// Rows
            if (gameWon) return;
            checkLine(i, i + 3, i + 6); // Columns
            if (gameWon) return;
        }

        checkLine(0, 4, 8);// Diagonal
        checkLine(2, 4, 6); // Reverse Diagonal
        if (gameWon) return;

        if (isBoardFull() && !gameWon) {
            displayDraw();
        }
    }

    private void checkLine(int a, int b, int c) {
        if (board[a] == board[b] && board[b] == board[c]) {
            gameWon = true;
            playSound(this, R.raw.win);
            animateWinningButtons(a, b, c);
        }
    }

    private void displayDraw() {
        gameStatusTextView.setText("");
        drawImageView.setVisibility(View.VISIBLE);
        gameDraw = true;
        playSound(this, R.raw.draw);
    }

    private void animateWinningButtons(int... winBtn) {
        for (int index : winBtn) {
            animateViewScale(buttons[index], 1f, 1.1f);
            changeButtonColor(buttons[index], GREEN_COLOR);
        }
    }

    // OnClick Method
    public void resetGame(View view) {
        playSound(this, R.raw.click_ui);
        resetGameState();
    }

    private void resetGameState() {
        gameWon = false;
        gameDraw = false;
        gameStatusTextView.setText(isVsAi ? "You're X" : "Turn: " + getCurrentPlayer(false));
        drawImageView.setVisibility(View.GONE);

        resetBoard();
        resetButtons();
    }

    private void resetBoard() {
        for (int i = 0; i < board.length; i++) {
            board[i] = (char) ('0' + i);
        }
    }

    private void resetButtons() {
        for (Button button : buttons) {
            button.setText("");
            button.setEnabled(true);
            animateViewScale(button, 1.1f, 1f);
            changeButtonColor(button, RED_COLOR);
        }
    }

    private int getEasyMove() {
        int randomNum;
        do {
            randomNum = new Random().nextInt(9);
        } while (board[randomNum] == 'X' || board[randomNum] == 'O');
        return randomNum;
    }

    private int getMediumMove() {
        int move;

        // Helper function to check for a winning/blocking move in a line
        class Helper {
            int checkLineForMove(int a, int b, int c, char player) {
                if (board[a]== player && board[b] == player && board[c] != 'X' && board[c] != 'O')
                    return c;
                if (board[a] == player && board[c] == player && board[b] != 'X' && board[b] != 'O')
                    return b;
                if (board[b] == player && board[c] == player && board[a] != 'X' && board[a] != 'O')
                    return a;
                return -1;
            }
        }
        Helper helper= new Helper();

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
                board[i] = (char) ('0' + i);

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
                    board[i] = (char) ('0' + i);
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
                    board[i] = (char) ('0' + i);
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
        resetGame(view);
    }
    // OnClick Method
    public void goToHome(View view) {
        playSound(this, R.raw.click_ui);
        changeActivity(this, HomeActivity.class, true,false);
    }
    // OnClick Method
    public void goBack(View view) {
        playSound(this,R.raw.click_ui);
        changeActivity(this, GameModeActivity.class,true,false);
    }

    private void initializeViews() {

        buttons[0] = findViewById(R.id.btn0);
        buttons[1] = findViewById(R.id.btn1);
        buttons[2] = findViewById(R.id.btn2);
        buttons[3] = findViewById(R.id.btn3);
        buttons[4] = findViewById(R.id.btn4);
        buttons[5] = findViewById(R.id.btn5);
        buttons[6] = findViewById(R.id.btn6);
        buttons[7] = findViewById(R.id.btn7);
        buttons[8] = findViewById(R.id.btn8);
        gameStatusTextView = findViewById(R.id.event);
        drawImageView = findViewById(R.id.drawImg);
        settingImageView = findViewById(R.id.settingimgview);
        homeImageView = findViewById(R.id.homeimgview);
        replayTextView = findViewById(R.id.replay);
        backImageView = findViewById(R.id.backimgview);
    }

    private void animateViewsPulse() {
        for (Button button : buttons) {
            animateViewPulse(this, button);
        }
        animateViewPulse(this, homeImageView);
        animateViewPulse(this, settingImageView);
        animateViewPulse(this, replayTextView);
        animateViewPulse(this, backImageView);
    }
}

