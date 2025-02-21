package com.fahadmalik5509.playbox.colorpuzzle;

import static android.view.View.VISIBLE;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.PUZZLE_BEST_SCORE;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.animateBlink;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.animateViewJiggle;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.animateViewPulse;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.changeActivity;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.getRandomNumber;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSound;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.saveToSharedPreferences;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.sharedPreferences;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.toggleVisibility;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.vibrate;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.fahadmalik5509.playbox.miscellaneous.GamesActivity;
import com.fahadmalik5509.playbox.miscellaneous.HomeActivity;
import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.miscellaneous.SettingActivity;
import com.fahadmalik5509.playbox.databinding.ColorpuzzleLayoutBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ColorPuzzleActivity extends AppCompatActivity {

    private int targetIndex;
    private View hintBorderView;
    private ColorpuzzleLayoutBinding vb;
    private static final byte INITIAL_GRID_SIZE = 3;
    private static final byte MAX_GRID_SIZE = 9;
    private static final byte CHANGE_GRID_SIZE_AFTER = 3;
    private static final byte MAX_LIVES = 3;
    private static final byte INITIAL_COLOR_DELTA = 30;
    private static final byte LOWEST_COLOR_DELTA = 5;
    private static final byte CHANGE_IN_COLOR_DELTA = 5;
    private byte currentGridSize = INITIAL_GRID_SIZE,  currentColorDelta = INITIAL_COLOR_DELTA, numberOfLives = MAX_LIVES, successCount = 0, consecutiveWin = 0;
    private int currentScore = 0;
    private boolean isGridChange = false, isHintUsed = false, isExplosionUsed = false;
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
        if (hintBorderView != null) {
            vb.gridContainer.removeView(hintBorderView);
            hintBorderView = null;
        }

        isHintUsed = false;
        isExplosionUsed = false;

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

            // Directly assign to the field 'targetIndex'
            targetIndex = new Random().nextInt(totalButtons);

            for (int i = 0; i < totalButtons; i++) {
                boolean isTarget = (i == targetIndex);
                Button button = createGridButton(buttonSize, gapPx, isTarget, baseColor, targetColor);
                if (isTarget) {
                    targetButton = button; // Store reference to the target button.
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
            playSound(this, isTarget ? (isGridChange ? R.raw.sound_new_level : R.raw.sound_success) : R.raw.sound_heart_crack);
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
            vb.crownLAV.playAnimation();
            vb.crownLAV.setVisibility(VISIBLE);
        }

        generateGrid(currentGridSize);
    }

    private void handleLoss(Button button) {
        animateViewJiggle(button, 150);
        consecutiveWin = 0;

        if (numberOfLives > 0) numberOfLives--;

        if (numberOfLives == 0) {
            toggleVisibility(true, vb.shadowV, vb.gameOverLAV);
            vb.gameOverLAV.playAnimation();
            playSound(this, R.raw.sound_game_over);

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
        if (consecutiveWin == 5) {
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
        playSound(this, R.raw.sound_ui);
        resetGameState();
        generateGrid(currentGridSize);
    }

    public void handleEliminateClick(View view) {

        if(isExplosionUsed) {
            playSound(this, R.raw.sound_error);
            return;
        }

        isExplosionUsed = true;

        playSound(this, R.raw.sound_explosion);
        vb.explosionLAV.playAnimation();
        vb.explosionLAV.setVisibility(VISIBLE);

        if (vb.gridContainer.getChildCount() > 0) {
            GridLayout gridLayout = (GridLayout) vb.gridContainer.getChildAt(0);
            int childCount = gridLayout.getChildCount();

            // Collect all visible non-target buttons
            List<View> nonTargetButtons = new ArrayList<>();
            for (int i = 0; i < childCount; i++) {
                View child = gridLayout.getChildAt(i);
                if (child instanceof Button && child != targetButton && child.getVisibility() == View.VISIBLE) {
                    nonTargetButtons.add(child);
                }
            }

            // Determine how many buttons to eliminate (hide half)
            int numToEliminate = nonTargetButtons.size() / 2;

            // Randomize the selection
            Collections.shuffle(nonTargetButtons);

            // Fade out and then hide the buttons
            for (int i = 0; i < numToEliminate; i++) {
                View button = nonTargetButtons.get(i);
                button.animate()
                        .alpha(0f)
                        .setDuration(150)
                        .withEndAction(() -> {
                            button.setVisibility(View.INVISIBLE);
                            button.setAlpha(1f);
                        });
            }
        }
    }

    public void handleHintClick(View view) {

        if (isHintUsed) {
            playSound(this, R.raw.sound_error);
            return;
        }

        playSound(this, R.raw.sound_hint);
        isHintUsed = true;
        if (targetButton == null) return;

        // Try to find an existing hint border by its tag.
        View border = vb.gridContainer.findViewWithTag("hint_border");
        if (border == null) {
            // Calculate the block dimension (60% of grid, at least 3 cells)
            int blockDimension = Math.max(3, (int) Math.ceil(currentGridSize * 0.6));
            blockDimension = Math.min(blockDimension, currentGridSize);
            int targetRow = targetIndex / currentGridSize;
            int targetCol = targetIndex % currentGridSize;
            int minRow = Math.max(0, targetRow - blockDimension + 1);
            int maxRow = Math.min(targetRow, currentGridSize - blockDimension);
            int blockStartRow = (minRow == maxRow) ? minRow : minRow + new Random().nextInt(maxRow - minRow + 1);
            int minCol = Math.max(0, targetCol - blockDimension + 1);
            int maxCol = Math.min(targetCol, currentGridSize - blockDimension);
            int blockStartCol = (minCol == maxCol) ? minCol : minCol + new Random().nextInt(maxCol - minCol + 1);

            // Retrieve the GridLayout (first child of gridContainer)
            GridLayout grid = (GridLayout) vb.gridContainer.getChildAt(0);
            int topLeftIndex = blockStartRow * currentGridSize + blockStartCol;
            int bottomRightIndex = (blockStartRow + blockDimension - 1) * currentGridSize + (blockStartCol + blockDimension - 1);
            View topLeft = grid.getChildAt(topLeftIndex);
            View bottomRight = grid.getChildAt(bottomRightIndex);

            int[] gridLoc = new int[2];
            vb.gridContainer.getLocationOnScreen(gridLoc);
            int[] topLeftLoc = new int[2];
            topLeft.getLocationOnScreen(topLeftLoc);
            int[] bottomRightLoc = new int[2];
            bottomRight.getLocationOnScreen(bottomRightLoc);

            int relativeLeft = topLeftLoc[0] - gridLoc[0];
            int relativeTop = topLeftLoc[1] - gridLoc[1];
            int relativeRight = bottomRightLoc[0] - gridLoc[0] + bottomRight.getWidth();
            int relativeBottom = bottomRightLoc[1] - gridLoc[1] + bottomRight.getHeight();

            int borderWidth = relativeRight - relativeLeft;
            int borderHeight = relativeBottom - relativeTop;

            // Create the border view.
            border = new View(this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(borderWidth, borderHeight);
            params.leftMargin = relativeLeft;
            params.topMargin = relativeTop;
            border.setLayoutParams(params);

            // Create a border drawable with a thin stroke.
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(Color.TRANSPARENT);
            int thickness = dpToPx(1 + currentGridSize / 4); // Adjust thickness as desired.
            drawable.setStroke(thickness, Color.YELLOW);
            drawable.setCornerRadius(0);
            border.setBackground(drawable);

            // Tag the view so we can find it later.
            border.setTag("hint_border");
            vb.gridContainer.addView(border);
        }
        // Reanimate the border (blink it) without recalculating position.
        reanimateBorder(border);
    }

    public void handleSkipClick(View view) {
        playSound(this, R.raw.sound_skip);
        vb.skipLAV.setMinFrame(10);
        vb.skipLAV.playAnimation();
        generateGrid(currentGridSize);
    }

    private void reanimateBorder(final View border) {
        border.animate().cancel();
        border.setVisibility(VISIBLE);
        border.setAlpha(1f); // start fully visible
        blinkBorderAndHide(border);
    }

    private void blinkBorderAndHide(final View border) {
        // Animate fade-out then fade-in, and repeat indefinitely.
        border.animate().alpha(0f).setDuration(200).withEndAction(() -> {
            border.animate().alpha(1f).setDuration(200).withEndAction(() -> {
                blinkBorderAndHide(border); // recursion for indefinite blinking
            }).start();
        }).start();
    }

    public void handleExitButtons(View view) {
        playSound(this, R.raw.sound_ui);
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
        toggleVisibility(false, vb.shadowV, vb.crownLAV, vb.gameOverLAV);
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
        playSound(this, R.raw.sound_ui);
        Intent intent = new Intent(this, SettingActivity.class);
        intent.putExtra("origin_activity", getClass().getSimpleName());
        startActivity(intent);
    }
    public void goToHome(View view) {
        playSound(this, R.raw.sound_ui);
        changeActivity(this, HomeActivity.class);
    }
    public void goBack(View view) {
        playSound(this, R.raw.sound_ui);
        if(currentScore > 0) toggleVisibility(true, vb.leaveRL, vb.shadowV);
        else changeActivity(this, GamesActivity.class);
    }
}