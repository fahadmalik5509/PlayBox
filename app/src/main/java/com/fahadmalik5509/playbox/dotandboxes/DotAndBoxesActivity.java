package com.fahadmalik5509.playbox.dotandboxes;

import static android.view.View.VISIBLE;
import static com.fahadmalik5509.playbox.dotandboxes.DotAndBoxesCasualAI.getValidMoves;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.databinding.DotandboxesLayoutBinding;
import com.fahadmalik5509.playbox.databinding.NavigationLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShadowLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShopButtonLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShopLayoutBinding;
import com.fahadmalik5509.playbox.miscellaneous.BaseActivity;
import com.fahadmalik5509.playbox.miscellaneous.GamesActivity;

import java.util.List;
import java.util.Random;

public class DotAndBoxesActivity extends BaseActivity {

    private DotandboxesLayoutBinding vb;
    private TextView previouslySelectedGridSizeTV = null;
    private boolean isPlayerVsAI, isCasual;

    // Timer variables using CountDownTimer
    private CountDownTimer countDownTimer;
    private int initialTime = 15;  // default timer value in seconds

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = DotandboxesLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        setupGameUI();
        initBindings();

        // Reset timer when a move is made in PvP mode.
        vb.dotAndBoxesView.setOnMoveListener(() -> {
            updateScore();
            updateTurnUI();
            if (!isPlayerVsAI) {
                resetTimer();
            }
            if (isPlayerVsAI && !vb.dotAndBoxesView.getGame().isPlayerOneTurn()) {
                if(isCasual) performCasualAIMove();
                else performTacticalAIMove();
            }
        });

        // Game touch listener to update UI, score and perform AI moves.
        vb.dotAndBoxesView.setOnTouchListener((v, event) -> {
            v.onTouchEvent(event);
            return true;
        });
    }

    // =================== UI Setup Methods ===================

    private void setupGameUI() {
        final int defaultGridSize = 6;
        final String defaultGameMode = "pvai";
        final String defaultDifficulty = "casual";

        vb.playerOneNameTV.setText(sharedPreferences.getString(DNBS_PLAYER_ONE_NAME_KEY, "Player 1"));
        vb.playerTwoNameTV.setText(sharedPreferences.getString(DNBS_PLAYER_TWO_NAME_KEY, "Player 2"));

        toggleVisibility(true, vb.DotAndBoxesMenuLayout, vb.Shadow.ShadowLayout);
        vb.dotAndBoxesView.updateGridSize(defaultGridSize);
        updateGridSizeUI(defaultGridSize);
        updateGameMode(defaultGameMode);
        updateDifficulty(defaultDifficulty);
        updateTurnUI();
    }

    private void initBindings() {
        ShopButtonLayoutBinding shopButtonBinding = ShopButtonLayoutBinding.bind(vb.ShopButton.getRoot());
        ShopLayoutBinding shopBinding = ShopLayoutBinding.bind(vb.Shop.getRoot());
        NavigationLayoutBinding navigationBinding = NavigationLayoutBinding.bind(vb.Navigation.getRoot());
        ShadowLayoutBinding shadowBinding = ShadowLayoutBinding.bind(vb.Shadow.getRoot());
        setBindings(shopButtonBinding, shopBinding, navigationBinding, shadowBinding);
    }

    private void updateScore() {
        int[] score = vb.dotAndBoxesView.getScore();
        if (vb.dotAndBoxesView.isGameOver()) {
            vb.dotAndBoxesView.gameInProgress = false;
            winnerSplash();
            stopTimer();
        }
        vb.playerOneScoreTV.setText(String.valueOf(score[0]));
        vb.playerTwoScoreTV.setText(String.valueOf(score[1]));
    }

    private void winnerSplash() {
        // Set the winner's name and win text.
        int[] score = vb.dotAndBoxesView.getScore();
        if (score[0] > score[1]) {
            vb.winnerNameTV.setText(vb.playerOneNameTV.getText());
            animateViewScale(vb.winnerNameTV, 0f, 1f, 500);
            animateViewScale(vb.winnerWonTV, 0f, 1f, 500);
            toggleVisibility(true, vb.winnerNameTV, vb.winnerWonTV);
        } else if(score[0] < score[1]) {
            vb.winnerNameTV.setText(vb.playerTwoNameTV.getText());
            animateViewScale(vb.winnerNameTV, 0f, 1f, 500);
            animateViewScale(vb.winnerWonTV, 0f, 1f, 500);
            toggleVisibility(true, vb.winnerNameTV, vb.winnerWonTV);
        } else {
            vb.winnerNameTV.setText(getString(R.string.draw));
            animateViewScale(vb.winnerNameTV, 0f, 1f, 500);
            toggleVisibility(true, vb.winnerNameTV);
        }

        playSoundAndVibrate(R.raw.sound_victory, true, 200);
        vb.celebrationLAV.setVisibility(VISIBLE);
        vb.celebrationLAV.playAnimation();
    }


    private void updateGridSizeUI(int boxesCount) {
        int dotCount = boxesCount + 1;
        if (dotCount < 5 || dotCount > 10) return;
        TextView[] gridSizeTVs = new TextView[]{
                vb.gridSize5TV, vb.gridSize6TV, vb.gridSize7TV,
                vb.gridSize8TV, vb.gridSize9TV, vb.gridSize10TV
        };
        int selectedIndex = dotCount - 5;
        for (int i = 0; i < gridSizeTVs.length; i++) {
            TextView tv = gridSizeTVs[i];
            tv.setSelected(i == selectedIndex);
            if (i == selectedIndex && previouslySelectedGridSizeTV != tv) {
                animateViewScale(tv, 1.0f, 1.1f, 200);
                if (previouslySelectedGridSizeTV != null) {
                    animateViewScale(previouslySelectedGridSizeTV, 1.1f, 1.0f, 0);
                    previouslySelectedGridSizeTV.setSelected(false);
                }
                previouslySelectedGridSizeTV = tv;
            }
        }
    }

    private void updateTurnUI() {
        if (vb.dotAndBoxesView.getGame().isPlayerOneTurn()) {
            changeBackgroundColor(vb.playerOneRL, LIGHT_GREEN_COLOR);
            changeBackgroundColor(vb.playerTwoRL, CHARCOAL_COLOR);
            animateViewScale(vb.playerOneRL, 1f, 1.1f, 200);
            animateViewScale(vb.playerTwoRL, 1.1f, 1f, 200);
        } else {
            changeBackgroundColor(vb.playerOneRL, CHARCOAL_COLOR);
            changeBackgroundColor(vb.playerTwoRL, LIGHT_RED_COLOR);
            animateViewScale(vb.playerOneRL, 1.1f, 1f, 200);
            animateViewScale(vb.playerTwoRL, 1f, 1.1f, 200);
        }
    }

    // =================== Event Handlers ===================

    public void handleDotAndBoxesMenuClick(View view) {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        toggleVisibility("open".equals(view.getTag()), vb.DotAndBoxesMenuLayout, vb.Shadow.ShadowLayout);
        animateViewScale(vb.DotAndBoxesMenuLayout, 0f, 1.0f, 200);
    }

    public void handleGridSizeButtons(View view) {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        int dotsCount = Integer.parseInt(view.getTag().toString());
        int boxesCount = dotsCount - 1;
        vb.dotAndBoxesView.updateGridSize(boxesCount);

        resetGame();
        updateGridSizeUI(boxesCount);
    }

    public void handleGameModeButtons(View view) {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        updateGameMode(view.getTag().toString());
    }

    private void updateGameMode(String mode) {
        isPlayerVsAI = "pvai".equals(mode);
        vb.pvaiTV.setSelected(isPlayerVsAI);
        vb.pvpTV.setSelected(!isPlayerVsAI);

        if (isPlayerVsAI) {
            animateViewScale(vb.pvaiTV, 1f, 1.05f, 200);
            animateViewScale(vb.pvpTV, 1.05f, 1f, 0);
            toggleVisibility(true, vb.difficultyTV, vb.difficultyLL);
            vb.playerOneNameTV.setText(R.string.you);
            vb.playerTwoNameTV.setText(R.string.ai);
            toggleVisibility(false, vb.profileIV, vb.timerTV, vb.setTimerTV, vb.timerLL);
            stopTimer();
        } else {
            animateViewScale(vb.pvaiTV, 1.05f, 1f, 0);
            animateViewScale(vb.pvpTV, 1f, 1.05f, 200);
            toggleVisibility(false, vb.difficultyTV, vb.difficultyLL);
            vb.playerOneNameTV.setText(sharedPreferences.getString(DNBS_PLAYER_ONE_NAME_KEY, "Player 1"));
            vb.playerTwoNameTV.setText(sharedPreferences.getString(DNBS_PLAYER_TWO_NAME_KEY, "Player 2"));
            toggleVisibility(true, vb.profileIV, vb.timerTV, vb.setTimerTV, vb.timerLL);
            startTimer();
        }

        resetGame();
    }

    public void handleDifficultyButtons(View view) {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        isCasual = view.getTag().equals("casual");
        updateDifficulty(view.getTag().toString());
    }

    private void updateDifficulty(String difficulty) {
        isCasual = "casual".equals(difficulty);
        vb.casualTV.setSelected(isCasual);
        vb.tacticalTV.setSelected(!isCasual);
        if (isCasual) {
            animateViewScale(vb.casualTV, 1f, 1.05f, 200);
            animateViewScale(vb.tacticalTV, 1.05f, 1f, 0);
        } else {
            animateViewScale(vb.casualTV, 1.05f, 1f, 0);
            animateViewScale(vb.tacticalTV, 1f, 1.05f, 200);
        }
    }

    public void handleResetClick(View view) {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        if (vb.resetRL.getVisibility() == VISIBLE) {
            if ("yes".equals(view.getTag())) {
                resetGame();
            }
            toggleVisibility(false, vb.resetRL, vb.Shadow.ShadowLayout);
            return;
        }
        if (vb.dotAndBoxesView.isGameOver()) {
            resetGame();
        } else if (vb.dotAndBoxesView.gameInProgress) {
            toggleVisibility(true, vb.resetRL, vb.Shadow.ShadowLayout);
        }
    }

    private void resetGame() {
        vb.dotAndBoxesView.gameInProgress = false;
        vb.dotAndBoxesView.animateGameReset();
        vb.dotAndBoxesView.restartGame();
        updateScore();
        updateTurnUI();
        resetTimer();
        vb.timerTV.setText(String.valueOf(initialTime));
        toggleVisibility(false, vb.celebrationLAV, vb.winnerWonTV, vb.winnerNameTV, vb.Shadow.ShadowLayout);
        vb.celebrationLAV.cancelAnimation();
        if (!isPlayerVsAI) startTimer();
    }

    public void handleExitButtons(View view) {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        if ("no".equals(view.getTag())) {
            toggleVisibility(false, vb.leaveRL, vb.Shadow.ShadowLayout);
        } else {
            changeActivity(this, GamesActivity.class);
        }
    }

    // =================== AI and Timeout Logic ===================

    private void performCasualAIMove() {
        vb.dotAndBoxesView.setInputEnabled(false);
        new Handler().postDelayed(() -> {
            DotAndBoxesGame game = vb.dotAndBoxesView.getGame();
            if (game.isPlayerOneTurn()) {
                vb.dotAndBoxesView.setInputEnabled(true);
                return;
            }
            DotAndBoxesCasualAI.Move chosenMove = DotAndBoxesCasualAI.chooseAIMove(game);

            if (chosenMove == null) {
                vb.dotAndBoxesView.setInputEnabled(true);
                return;
            }
            processMove(chosenMove);
            // If a box was completed, the AI may move again; otherwise, re-enable input.
            if (game.getClaimedBoxesCount() > 0) {
                performCasualAIMove();
            } else {
                vb.dotAndBoxesView.setInputEnabled(true);
            }
        }, 200);
    }

    private void performTacticalAIMove() {
        performCasualAIMove();
    }

    private void handleTimeout() {
        if (isPlayerVsAI || vb.dotAndBoxesView.isGameOver()) return;
        DotAndBoxesGame game = vb.dotAndBoxesView.getGame();
        List<DotAndBoxesCasualAI.Move> availableMoves = getValidMoves(game);
        if (!availableMoves.isEmpty()) {
            DotAndBoxesCasualAI.Move randomMove = availableMoves.get(new Random().nextInt(availableMoves.size()));
            processMove(randomMove);
            startTimer();
        }
    }

    private void processMove(DotAndBoxesCasualAI.Move move) {
        DotAndBoxesGame game = vb.dotAndBoxesView.getGame();
        int boxesBefore = game.getClaimedBoxesCount();
        boolean success = game.markLine(move.isHorizontal, move.row, move.col);
        if (!success) {
            vb.dotAndBoxesView.setInputEnabled(true);
            return;
        }
        vb.dotAndBoxesView.invalidate();

        updateScore();
        updateTurnUI();

        int boxesAfter = game.getClaimedBoxesCount();
        if (boxesAfter > boxesBefore) {
            vb.dotAndBoxesView.animateCompletedBoxes();
            playSoundAndVibrate(R.raw.sound_box_complete, true, 200);
        } else {
            playSoundAndVibrate(R.raw.sound_line_placed, true, 50);
        }
    }

    // =================== Profile Handling ===================

    public void handleProfileClick(View view) {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        animateViewScale(vb.profileRL, 0f, 1.0f, 200);
        toggleVisibility(true, vb.profileRL, vb.Shadow.ShadowLayout);
        vb.playerOneET.setText(sharedPreferences.getString(DNBS_PLAYER_ONE_NAME_KEY, "Player 1"));
        vb.playerTwoET.setText(sharedPreferences.getString(DNBS_PLAYER_TWO_NAME_KEY, "Player 2"));
    }

    public void handleProfileButtons(View view) {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        if ("save".equals(view.getTag())) {
            updateProfilesUI();
        }
        toggleVisibility(false, vb.profileRL, vb.Shadow.ShadowLayout);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void updateProfilesUI() {
        if (vb.playerOneET.getText().toString().trim().isEmpty())
            vb.playerOneET.setText(getString(R.string.player_one));
        if (vb.playerTwoET.getText().toString().trim().isEmpty())
            vb.playerTwoET.setText(getString(R.string.player_two));

        String playerOneName = vb.playerOneET.getText().toString().trim().replaceAll("\\s", "");
        String playerTwoName = vb.playerTwoET.getText().toString().trim().replaceAll("\\s", "");
        saveToSharedPreferences(DNBS_PLAYER_ONE_NAME_KEY, playerOneName);
        saveToSharedPreferences(DNBS_PLAYER_TWO_NAME_KEY, playerTwoName);
        vb.playerOneNameTV.setText(sharedPreferences.getString(DNBS_PLAYER_ONE_NAME_KEY, "Player 1"));
        vb.playerTwoNameTV.setText(sharedPreferences.getString(DNBS_PLAYER_TWO_NAME_KEY, "Player 2"));
    }

    // =================== Timer Methods using CountDownTimer ===================

    public void handleTimeButtons(View view) {
        switch(view.getTag().toString()) {
            case "+":
                if (initialTime >= 30) {
                    playSoundAndVibrate(R.raw.sound_error, true, 50);
                    return;
                }
                initialTime += 5;
                break;
            case "-":
                if (initialTime <= 0) {
                    playSoundAndVibrate(R.raw.sound_error, true, 50);
                    return;
                }
                initialTime -= 5;
                break;
        }

        vb.menuTimerTV.setText(String.valueOf(initialTime));
        toggleVisibility(initialTime != 0, vb.timerTV);
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        resetGame();
    }

    private void startTimer() {
        if (initialTime == 0 || isPlayerVsAI || !vb.dotAndBoxesView.gameInProgress) return;
        stopTimer();
        vb.timerTV.setText(String.valueOf(initialTime));
        countDownTimer = new CountDownTimer(initialTime * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Add 1 so that 15000ms -> 15, 14000ms -> 14, etc.
                int secondsRemaining = (int) (millisUntilFinished / 1000) + 1;
                vb.timerTV.setText(String.valueOf(secondsRemaining));
            }
            @Override
            public void onFinish() {
                handleTimeout();
            }
        }.start();
    }


    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private void resetTimer() {
        stopTimer();
        startTimer();
    }

    // =================== Back and Lifecycle ===================

    @Override
    public void backLogic() {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        if (vb.Shop.ShopLayout.getVisibility() == VISIBLE) {
            toggleVisibility(false, vb.Shop.ShopLayout, vb.Shadow.ShadowLayout);
        } else if (vb.DotAndBoxesMenuLayout.getVisibility() == VISIBLE) {
            toggleVisibility(false, vb.DotAndBoxesMenuLayout, vb.Shadow.ShadowLayout);
        } else if (vb.resetRL.getVisibility() == VISIBLE) {
            toggleVisibility(false, vb.resetRL, vb.Shadow.ShadowLayout);
        } else if (vb.leaveRL.getVisibility() == VISIBLE) {
            toggleVisibility(false, vb.leaveRL, vb.Shadow.ShadowLayout);
        } else if (vb.dotAndBoxesView.gameInProgress) {
            toggleVisibility(true, vb.leaveRL, vb.Shadow.ShadowLayout);
        } else {
            changeActivity(this, GamesActivity.class);
        }
    }

    @Override
    protected Class<?> getBackDestination() {
        return GamesActivity.class;
    }
}