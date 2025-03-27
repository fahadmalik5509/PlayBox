package com.fahadmalik5509.playbox.dotandboxes;

import static android.view.View.VISIBLE;
import static com.fahadmalik5509.playbox.dotandboxes.DotAndBoxesAI.getValidMoves;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
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
    private boolean isPlayerVsAI, isCasual, previousTurnIsPlayerOne;

    // Timer variables
    private final Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private int timeLeft;
    private int initialTime = 15;  // default timer value

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = DotandboxesLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        initTimerRunnable();
        setupGameUI();
        initBindings();

        // Reset timer when a move is made in PvP mode.
        vb.dotAndBoxesView.setOnMoveListener(() -> {
            if (!isPlayerVsAI) {
                resetTimer();
            }
        });

        // Game touch listener to update UI, score and perform AI moves.
        vb.dotAndBoxesView.setOnTouchListener((v, event) -> {
            v.onTouchEvent(event);
            updateScore();
            checkAndUpdateTurn();
            if (isPlayerVsAI && !vb.dotAndBoxesView.getGame().isPlayerOneTurn()) {
                performCasualAIMove();
            }
            return true;
        });

        // Initialize turn state.
        previousTurnIsPlayerOne = vb.dotAndBoxesView.getGame().isPlayerOneTurn();
    }

    /**
     * Initialize the timer runnable that updates the timer each second.
     */
    private void initTimerRunnable() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (timeLeft > 0 && !isPlayerVsAI && vb.dotAndBoxesView.gameInProgress) {
                    timeLeft--;
                    vb.timerTV.setText(String.valueOf(timeLeft));
                    timerHandler.postDelayed(this, 1000);
                } else if (timeLeft <= 0) {
                    handleTimeout();
                }
            }
        };
    }

    // =================== UI Setup Methods ===================

    /**
     * Setup default game settings and UI components.
     */
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
        updateDifficultyUI(defaultDifficulty);
        updateScore();
        updateTurnUI();
    }

    /**
     * Initialize additional view bindings.
     */
    private void initBindings() {
        ShopButtonLayoutBinding shopButtonBinding = ShopButtonLayoutBinding.bind(vb.ShopButton.getRoot());
        ShopLayoutBinding shopBinding = ShopLayoutBinding.bind(vb.Shop.getRoot());
        NavigationLayoutBinding navigationBinding = NavigationLayoutBinding.bind(vb.Navigation.getRoot());
        ShadowLayoutBinding shadowBinding = ShadowLayoutBinding.bind(vb.Shadow.getRoot());
        setBindings(shopButtonBinding, shopBinding, navigationBinding, shadowBinding);
    }

    /**
     * Update the score UI and stop game if over.
     */
    private void updateScore() {
        int[] score = vb.dotAndBoxesView.getScore();
        if (vb.dotAndBoxesView.isGameOver()) {
            vb.dotAndBoxesView.gameInProgress = false;
        }
        vb.playerOneScoreTV.setText(String.valueOf(score[0]));
        vb.playerTwoScoreTV.setText(String.valueOf(score[1]));
    }

    /**
     * Update grid size UI highlighting based on selection.
     */
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

    /**
     * Update the turn UI by switching colors and animations based on current player.
     */
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

    /**
     * Check if turn has changed and update UI accordingly.
     */
    private void checkAndUpdateTurn() {
        boolean currentTurnIsPlayerOne = vb.dotAndBoxesView.getGame().isPlayerOneTurn();
        if (currentTurnIsPlayerOne != previousTurnIsPlayerOne) {
            updateTurnUI();
            previousTurnIsPlayerOne = currentTurnIsPlayerOne;
        }
    }

    // =================== Event Handlers ===================

    public void handleDotAndBoxesMenuClick(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        toggleVisibility("open".equals(view.getTag()), vb.DotAndBoxesMenuLayout, vb.Shadow.ShadowLayout);
    }

    public void handleGridSizeButtons(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        int dotsCount = Integer.parseInt(view.getTag().toString());
        int boxesCount = dotsCount - 1;
        vb.dotAndBoxesView.updateGridSize(boxesCount);
        updateScore();
        updateGridSizeUI(boxesCount);
        vb.dotAndBoxesView.gameInProgress = false;
    }

    public void handleGameModeButtons(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
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

        vb.dotAndBoxesView.restartGame();
        updateScore();
    }

    public void handleDifficultyButtons(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        isCasual = view.getTag().equals("casual");
        updateDifficultyUI(view.getTag().toString());
    }

    private void updateDifficultyUI(String difficulty) {
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
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        // If reset confirmation is visible, process confirmation
        if (vb.resetRL.getVisibility() == VISIBLE) {
            if ("yes".equals(view.getTag())) {
                resetGame();
            }
            toggleVisibility(false, vb.resetRL, vb.Shadow.ShadowLayout);
            return;
        }
        // Reset immediately if game is over, otherwise ask confirmation.
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
        vb.timerTV.setText(String.valueOf(initialTime));
        if (!isPlayerVsAI) startTimer();
    }

    public void handleExitButtons(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        if ("no".equals(view.getTag())) {
            toggleVisibility(false, vb.leaveRL, vb.Shadow.ShadowLayout);
        } else {
            changeActivity(this, GamesActivity.class);
        }
    }

    public void handleProfileClick(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        animateViewScale(vb.profileRL, 0f, 1.0f, 200);
        toggleVisibility(true, vb.profileRL, vb.Shadow.ShadowLayout);
        vb.playerOneET.setText(sharedPreferences.getString(DNBS_PLAYER_ONE_NAME_KEY, "Player 1"));
        vb.playerTwoET.setText(sharedPreferences.getString(DNBS_PLAYER_TWO_NAME_KEY, "Player 2"));
    }

    public void handleProfileButtons(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
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

    // =================== AI and Timeout Logic ===================

    /**
     * Perform an AI move in casual mode.
     */
    private void performCasualAIMove() {
        vb.dotAndBoxesView.setInputEnabled(false);
        new Handler().postDelayed(() -> {
            DotAndBoxesGame game = vb.dotAndBoxesView.getGame();
            if (game.isPlayerOneTurn()) {
                vb.dotAndBoxesView.setInputEnabled(true);
                return;
            }
            DotAndBoxesAI.Move chosenMove = DotAndBoxesAI.chooseAIMove(game);
            if (chosenMove == null) {
                vb.dotAndBoxesView.setInputEnabled(true);
                return;
            }
            int boxesBefore = game.getClaimedBoxesCount();
            game.markLine(chosenMove.isHorizontal, chosenMove.row, chosenMove.col);
            vb.dotAndBoxesView.invalidate();
            updateScore();
            updateTurnUI();
            int boxesAfter = game.getClaimedBoxesCount();
            if (boxesAfter > boxesBefore) {
                vb.dotAndBoxesView.animateCompletedBoxes();
                playSoundAndVibrate(this, R.raw.sound_box_complete, true, 200);
                performCasualAIMove(); // repeat turn if box completed
            } else {
                playSoundAndVibrate(this, R.raw.sound_dot_clicked, true, 50);
                vb.dotAndBoxesView.setInputEnabled(true);
            }
        }, 200);
    }

    /**
     * Handle a timeout by making a random valid move.
     */
    private void handleTimeout() {
        if (isPlayerVsAI || vb.dotAndBoxesView.isGameOver()) return;

        DotAndBoxesGame game = vb.dotAndBoxesView.getGame();
        int boxesBefore = game.getClaimedBoxesCount();

        List<DotAndBoxesAI.Move> availableMoves = getValidMoves(game);

        if (!availableMoves.isEmpty()) {
            DotAndBoxesAI.Move randomMove = availableMoves.get(new Random().nextInt(availableMoves.size()));
            boolean success = game.markLine(randomMove.isHorizontal, randomMove.row, randomMove.col);
            if (success) {
                int boxesAfter = game.getClaimedBoxesCount();
                vb.dotAndBoxesView.invalidate();
                updateScore();
                updateTurnUI();
                if (boxesAfter > boxesBefore) {
                    vb.dotAndBoxesView.animateCompletedBoxes();
                    playSoundAndVibrate(this, R.raw.sound_box_complete, true, 200);
                } else {
                    playSoundAndVibrate(this, R.raw.sound_dot_clicked, true, 50);
                }
                startTimer();
            }
        }
    }

    // =================== Timer Methods ===================

    private void startTimer() {
        if (initialTime == 0 || isPlayerVsAI || !vb.dotAndBoxesView.gameInProgress) return;
        stopTimer();
        timeLeft = initialTime;
        vb.timerTV.setText(String.valueOf(timeLeft));
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void stopTimer() {
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void resetTimer() {
        stopTimer();
        startTimer();
    }

    public void handleTimeButtons(View view) {
        switch(view.getTag().toString()) {
            case "+":
                if (initialTime >= 30) {
                    playSoundAndVibrate(this, R.raw.sound_error, true, 50);
                    return;
                }
                initialTime += 5;
                break;
            case "-":
                if (initialTime <= 0) {
                    playSoundAndVibrate(this, R.raw.sound_error, true, 50);
                    return;
                }
                initialTime -= 5;
                break;

        }
        vb.menuTimerTV.setText(String.valueOf(initialTime));
        toggleVisibility(initialTime != 0, vb.timerTV);
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        resetGame();
    }

    // =================== Back and Lifecycle ===================

    @Override
    public void backLogic() {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
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