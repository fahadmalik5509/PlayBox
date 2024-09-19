package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.*;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.airbnb.lottie.LottieAnimationView;

import java.util.Random;

public class TicTacToeActivity extends AppCompatActivity {

    private final char[] board = { '0', '1', '2', '3', '4', '5', '6', '7', '8' };
    private boolean gameWon = false, gameDraw = false, isX = true;
    private int difficulty, playerOneScore = 0,playerTwoScore = 0;
    private ImageView drawIV, homeIV, settingIV, backIV, difficultyIV, profileIV;
    private TextView replayTV, difficultyTooltipTV,
            playerOneNameTV,playerOneScoreTV,
            playerTwoNameTV,playerTwoScoreTV;
    private EditText playerOneET, playerTwoET;
    private final Button[] buttons = new Button[9];
    private Button profileSaveB, profileExitB;
    private RelativeLayout difficultyRL, profileRL;
    private View shadowV;
    private CardView playerOneCV, playerTwoCV;
    LottieAnimationView fireworkAV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tictactoe_layout);

        loadPreference(this);
        initializeViews();
        if (isVsAi) {
            profileIV.setVisibility(View.GONE);
            playerOneNameTV.setText("YOU");
            playerTwoNameTV.setText("AI");
            difficultyRL.setVisibility(View.VISIBLE);
            difficulty = sharedPreferences.getInt(DIFFICULTY_KEY, 1);
        }
        animateViewsPulse();
        updateDifficultyColor();
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
            if (gameWon) return;
            checkLine(i, i + 3, i + 6); // Columns
            if (gameWon) return;
        }

        checkLine(0, 4, 8); // Diagonal
        checkLine(2, 4, 6); // Reverse Diagonal
        if (gameWon) return;

        if (isBoardFull()) {
            isDraw();
        }
    }

    private void checkLine(int a, int b, int c) {
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

        playerOneScoreTV.setText("Score: " + playerOneScore);
        playerTwoScoreTV.setText("Score: " + playerTwoScore);

        fireworkAV.setVisibility(View.VISIBLE);
        fireworkAV.playAnimation();
    }

    private void isDraw() {

        drawIV.setVisibility(View.VISIBLE);
        gameDraw = true;
        playSound(this, R.raw.draw);
    }

    private void animateWinningButtons(int...winBtn) {
        for (int index: winBtn) {
            animateViewScale(buttons[index], 1f, 1.1f, 100);
            changeBackgroundColor(buttons[index], ActivityUtils.getColorByID(this, R.color.green));
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
        drawIV.setVisibility(View.GONE);
        fireworkAV.cancelAnimation();
        fireworkAV.setVisibility(View.GONE);

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
            changeBackgroundColor(button, ActivityUtils.getColorByID(this, R.color.red));
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
        onResetGameClicked(view);
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

        difficultyTooltipTV.setText("AI Difficulty\n(" + getDifficultyText() + ")");
        difficultyTooltipTV.setVisibility(View.VISIBLE);
        difficultyTooltipTV.postDelayed(() -> difficultyTooltipTV.setVisibility(View.GONE), 2000);
        updateDifficultyColor();

        playerOneScore = 0;
        playerTwoScore = 0;
        playerOneScoreTV.setText("Score: " + playerOneScore);
        playerTwoScoreTV.setText("Score: " + playerTwoScore);
        onResetGameClicked(view);
    }

    private void updateDifficultyColor() {
        if (difficulty == 1) changeBackgroundColor(difficultyIV, ActivityUtils.getColorByID(this, R.color.green));
        if (difficulty == 2) changeBackgroundColor(difficultyIV, ActivityUtils.getColorByID(this, R.color.yellow));
        if (difficulty == 3) changeBackgroundColor(difficultyIV, ActivityUtils.getColorByID(this, R.color.red));
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
            animateViewScale(profileRL,0f,1.0f,200);
            profileRL.setVisibility(View.VISIBLE);
            shadowV.setVisibility(View.VISIBLE);
            playerOneET.setText(sharedPreferences.getString(PLAYERONE_NAME_KEY,"Player 1"));
            playerTwoET.setText(sharedPreferences.getString(PLAYERTWO_NAME_KEY,"Player 2"));
    }

    private void updateProfiles() {

        if(playerOneET.getText().toString().trim().isEmpty()) playerOneET.setText("Player⠀1");
        if(playerTwoET.getText().toString().trim().isEmpty()) playerTwoET.setText("Player⠀2");

        saveToSharedPreferences(PLAYERONE_NAME_KEY,playerOneET.getText().toString().trim().replaceAll("\\s", ""));
        saveToSharedPreferences(PLAYERTWO_NAME_KEY,playerTwoET.getText().toString().trim().replaceAll("\\s", ""));
        playerOneNameTV.setText(sharedPreferences.getString(PLAYERONE_NAME_KEY,"Player 1"));
        playerTwoNameTV.setText(sharedPreferences.getString(PLAYERTWO_NAME_KEY,"Player 2"));
    }

    private void updateCardView() {
        if(isX) {
            playerOneCV.setAlpha(1f);
            playerTwoCV.setAlpha(0.8f);
            animateViewScale(playerOneCV, 1f, 1.1f, 200);
            animateViewScale(playerTwoCV, 1.1f, 1f, 200);
        }
        else {
            playerOneCV.setAlpha(0.8f);
            playerTwoCV.setAlpha(1f);
            animateViewScale(playerOneCV,1.1f,1f,200);
            animateViewScale(playerTwoCV, 1f, 1.1f, 200);
        }
    }

    //OnClick Method
    public void handleProfileButtons(View view){
        playSound(this,R.raw.click_ui);
        if(view.getTag().equals("save")) updateProfiles();

        animateViewScale(profileRL,1.0f,0f,200);
        shadowV.setVisibility(View.GONE);
        new Handler().postDelayed(() -> profileRL.setVisibility(View.GONE) , 300);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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

        drawIV = findViewById(R.id.ivDraw);
        settingIV = findViewById(R.id.ivSettingIcon);
        homeIV = findViewById(R.id.ivHomeIcon);
        backIV = findViewById(R.id.ivBackIcon);
        difficultyIV = findViewById(R.id.ivDifficulty);
        replayTV = findViewById(R.id.tvReplay);
        difficultyTooltipTV = findViewById(R.id.tvDifficultyTooltip);
        shadowV = findViewById(R.id.vShadow);
        difficultyRL = findViewById(R.id.rlDifficulty);
        fireworkAV = findViewById(R.id.lavFireworks);


        playerOneNameTV = findViewById(R.id.tvPlayerOneName);
        playerOneScoreTV = findViewById(R.id.tvPlayerOneScore);
        playerTwoNameTV = findViewById(R.id.tvPlayerTwoName);
        playerTwoScoreTV = findViewById(R.id.tvPlayerTwoScore);

        playerOneET = findViewById(R.id.etPlayerOne);
        playerTwoET = findViewById(R.id.etPlayerTwo);

        profileIV = findViewById(R.id.ivProfile);
        profileRL = findViewById(R.id.rlProfile);

        profileSaveB = findViewById(R.id.bProfileSave);
        profileExitB = findViewById(R.id.bProfileExit);

        playerOneCV = findViewById(R.id.cvPlayerOne);
        playerTwoCV = findViewById(R.id.cvPlayerTwo);

        playerOneNameTV.setText(sharedPreferences.getString(PLAYERONE_NAME_KEY,"Player 1"));
        playerTwoNameTV.setText(sharedPreferences.getString(PLAYERTWO_NAME_KEY,"Player 2"));
    }

    private void animateViewsPulse() {
        for (Button button: buttons) animateViewPulse(this, button);
        animateViewPulse(this, homeIV);
        animateViewPulse(this, settingIV);
        animateViewPulse(this, replayTV);
        animateViewPulse(this, backIV);
        animateViewPulse(this, difficultyIV);
        animateViewPulse(this, profileIV);
        animateViewPulse(this, profileSaveB);
        animateViewPulse(this, profileExitB);
    }

}