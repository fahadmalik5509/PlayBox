package com.fahadmalik5509.playbox;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.fahadmalik5509.playbox.ActivityUtils.PUZZLE_BEST_SCORE;
import static com.fahadmalik5509.playbox.ActivityUtils.animateBlink;
import static com.fahadmalik5509.playbox.ActivityUtils.animateViewJiggle;
import static com.fahadmalik5509.playbox.ActivityUtils.animateViewPulse;
import static com.fahadmalik5509.playbox.ActivityUtils.changeActivity;
import static com.fahadmalik5509.playbox.ActivityUtils.getRandomNumber;
import static com.fahadmalik5509.playbox.ActivityUtils.playSound;
import static com.fahadmalik5509.playbox.ActivityUtils.saveToSharedPreferences;
import static com.fahadmalik5509.playbox.ActivityUtils.sharedPreferences;
import static com.fahadmalik5509.playbox.ActivityUtils.toggleVisibility;
import static com.fahadmalik5509.playbox.ActivityUtils.vibrate;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.fahadmalik5509.playbox.databinding.ColorpuzzleLayoutBinding;

import java.util.Random;

public class ColorPuzzleActivity extends AppCompatActivity {

    private ColorpuzzleLayoutBinding vb;
    private static final byte INITIAL_GRID_SIZE = 3;
    private static final byte MAX_GRID_SIZE = 9;
    private static final byte CHANGE_GRID_SIZE_AFTER = 3;
    private static final byte MAX_LIVES = 3;
    private static final byte INITIAL_COLOR_DELTA = 30;
    private static final byte LOWEST_COLOR_DELTA = 6;
    private static final byte CHANGE_IN_COLOR_DELTA = 3;
    private byte currentGridSize = INITIAL_GRID_SIZE,  currentColorDelta = INITIAL_COLOR_DELTA, numberOfLives = MAX_LIVES, successCount = 0, consecutiveWin = 0;
    private int currentScore = 0;
    private boolean isGridChange = false;
    private Button targetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = ColorpuzzleLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackNavigation();
            }
        });

        setupGame();
    }

    private void setupGame() {
        animateViewPulse(this, vb.resetTV, true);
        vb.gridContainer.setLayoutTransition(new LayoutTransition());
        updateBestScoreDisplay();
        generateGrid(currentGridSize);
    }

    private void generateGrid(final int gridSize) {
        vb.gridContainer.removeAllViews();
        vb.gridContainer.post(() -> {
            int containerWidth = vb.gridContainer.getWidth();
            int paddingPx = dpToPx(8);
            int gapPx = dpToPx(1);
            int totalMargins = gridSize * gapPx * 2;
            int buttonSize = (containerWidth - (paddingPx * 2) - totalMargins) / gridSize;

            GridLayout gridLayout = new GridLayout(this);
            gridLayout.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
            gridLayout.setColumnCount(gridSize);
            gridLayout.setRowCount(gridSize);
            FrameLayout.LayoutParams gridParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            gridParams.gravity = Gravity.CENTER;
            gridLayout.setLayoutParams(gridParams);

            int baseColor = getBaseColor();
            int targetColor = getTargetColor(baseColor);
            int totalButtons = gridSize * gridSize;
            int targetIndex = new Random().nextInt(totalButtons);

            for (int i = 0; i < totalButtons; i++) {
                boolean isTarget = (i == targetIndex);
                Button button = createGridButton(buttonSize, gapPx, isTarget, baseColor, targetColor);
                if (isTarget) {
                    targetButton = button; // Store reference to the target button
                }
                gridLayout.addView(button);
            }
            vb.gridContainer.addView(gridLayout);
        });
    }

    private Button createGridButton(int size, int gapPx, boolean isTarget, int baseColor, int targetColor) {
        Button button = new Button(this);
        button.setBackgroundColor(isTarget ? targetColor : baseColor);
        animateViewPulse(this, button, false);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = size;
        params.height = size;
        params.setMargins(gapPx, gapPx, gapPx, gapPx);
        button.setLayoutParams(params);
        button.setOnClickListener(v -> {
            if (isTarget) {
                handleWin();
            } else {
                handleLoss(button);
            }
            playSound(this, isTarget ? (isGridChange ? R.raw.newlevel : R.raw.sucess) : R.raw.aheartbreak);
            isGridChange = false;
        });
        return button;
    }

    private void handleWin() {
        consecutiveWin++;
        successCount++;
        handleLevelUp();
        handleLifeIncrement();

        currentScore++;
        updateScoreDisplay();

        if (currentScore > sharedPreferences.getInt(PUZZLE_BEST_SCORE, 0)) {
            saveToSharedPreferences(PUZZLE_BEST_SCORE, currentScore);
            updateBestScoreDisplay();
            vb.fireLAV.playAnimation();
            vb.fireLAV.setVisibility(VISIBLE);
        }

        generateGrid(currentGridSize);
    }

    private void handleLoss(Button button) {
        animateViewJiggle(button, 150);
        vb.fireLAV.cancelAnimation();
        vb.fireLAV.setVisibility(GONE);
        consecutiveWin = 0;

        if (numberOfLives > 0) numberOfLives--;

        if (numberOfLives == 0) {
            toggleVisibility(true, vb.shadowV, vb.gameOverLAV);
            vb.gameOverLAV.playAnimation();
            playSound(this, R.raw.agameover);

            // Zoom out the target button when the game is over
            if (targetButton != null) animateBlink(targetButton, 300, 3);
        }

        playHeartAnimation();
    }

    private void handleLevelUp() {
        if (successCount >= CHANGE_GRID_SIZE_AFTER) {
            if (currentGridSize < MAX_GRID_SIZE) {
                currentGridSize++;
                isGridChange = true;
            }
            if (currentColorDelta > LOWEST_COLOR_DELTA) {
                currentColorDelta -= CHANGE_IN_COLOR_DELTA;
            }
            successCount = 0;
        }
    }
    private void handleLifeIncrement() {
        if (consecutiveWin == 5 || (consecutiveWin == 3 && currentScore >= 20)) {
            if (numberOfLives < 3) {
                numberOfLives++;
            }
            consecutiveWin = 0;
            resetHearts();
        }
    }
    private void resetHearts() {
        switch (numberOfLives) {
            case 3:
                vb.heartOneLAV.cancelAnimation();
                vb.heartTwoLAV.cancelAnimation();
                vb.heartThreeLAV.cancelAnimation();

                vb.heartOneLAV.setFrame(0);
                vb.heartTwoLAV.setFrame(0);
                vb.heartThreeLAV.setFrame(0);
                break;
            case 2:
                vb.heartOneLAV.cancelAnimation();
                vb.heartTwoLAV.cancelAnimation();

                vb.heartOneLAV.setFrame(0);
                vb.heartTwoLAV.setFrame(0);
                break;
            case 1:
                vb.heartTwoLAV.cancelAnimation();
                vb.heartTwoLAV.setFrame(0);
                break;
        }
    }
    private void playHeartAnimation() {
        switch (numberOfLives) {
            case 2:
                vb.heartThreeLAV.playAnimation();
                break;
            case 1:
                vb.heartTwoLAV.playAnimation();
                break;
            default:
                vb.heartOneLAV.playAnimation();
                break;
        }
    }
    private void updateScoreDisplay() { vb.currentScoreTV.setText(getString(R.string.score, currentScore)); }
    private void updateBestScoreDisplay() { vb.bestScoreTV.setText(getString(R.string.best_score, sharedPreferences.getInt(PUZZLE_BEST_SCORE, 0))); }
    public void handleResetClick(View view) {
        playSound(this, R.raw.click_ui);
        resetGameState();
        generateGrid(currentGridSize);
    }

    public void handleExitButtons(View view) {
        playSound(this, R.raw.click_ui);
        if(view.getTag().equals("no")) toggleVisibility(false, vb.leaveRL, vb.shadowV);
        else changeActivity(this, GamesActivity.class);
    }

    private void handleBackNavigation() {
        vibrate(this, 50);
        if (currentScore > 0) {
            toggleVisibility(vb.leaveRL.getVisibility() != View.VISIBLE, vb.leaveRL, vb.shadowV);
        } else {
            changeActivity(this, GamesActivity.class);
        }
    }

    private void resetGameState() {
        currentScore = 0;
        consecutiveWin = 0;
        successCount = 0;
        numberOfLives = MAX_LIVES;
        currentGridSize = INITIAL_GRID_SIZE;
        currentColorDelta = INITIAL_COLOR_DELTA;
        toggleVisibility(false, vb.shadowV, vb.fireLAV, vb.gameOverLAV);
        resetHearts();
        updateScoreDisplay();
    }
    private int getBaseColor() {
        return Color.rgb(getRandomNumber(0, 256), getRandomNumber(0, 256), getRandomNumber(0, 256));
    }
    private int getTargetColor(int baseColor) {
        int adjust = new Random().nextBoolean() ? currentColorDelta : -currentColorDelta;
        return Color.rgb(
                clampColorValue(Color.red(baseColor) + adjust),
                clampColorValue(Color.green(baseColor) + adjust),
                clampColorValue(Color.blue(baseColor) + adjust)
        );
    }
    private int clampColorValue(int value) { return Math.min(255, Math.max(0, value)); }
    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
    public void goToSetting(View view) {
        playSound(this, R.raw.click_ui);
        Intent intent = new Intent(this, SettingActivity.class);
        intent.putExtra("origin_activity", getClass().getSimpleName());
        startActivity(intent);
    }
    public void goToHome(View view) {
        playSound(this, R.raw.click_ui);
        changeActivity(this, HomeActivity.class);
    }
    public void goBack(View view) {
        playSound(this, R.raw.click_ui);
        if(currentScore > 0) toggleVisibility(true, vb.leaveRL, vb.shadowV);
        else changeActivity(this, GamesActivity.class);
    }
}