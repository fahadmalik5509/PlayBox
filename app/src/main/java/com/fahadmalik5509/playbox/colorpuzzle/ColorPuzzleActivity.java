package com.fahadmalik5509.playbox.colorpuzzle;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.*;

import android.animation.AnimatorInflater;
import android.animation.LayoutTransition;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;

import androidx.activity.OnBackPressedCallback;

import com.fahadmalik5509.playbox.databinding.NavigationLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShadowLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShopButtonLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShopLayoutBinding;
import com.fahadmalik5509.playbox.miscellaneous.BaseActivity;
import com.fahadmalik5509.playbox.miscellaneous.GamesActivity;
import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.databinding.ColorpuzzleLayoutBinding;
import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ColorPuzzleActivity extends BaseActivity implements BaseActivity.ShopUpdateListener {

    private static final byte CHANGE_GRID_SIZE_AFTER = 5;
    private static byte CHANGE_IN_COLOR_DELTA;
    private static final byte MAX_LIVES = 3;
    private byte numberOfLives = MAX_LIVES;
    private byte MAX_GRID_SIZE;
    private byte LOWEST_COLOR_DELTA;
    private byte currentGridSize;
    private byte currentColorDelta;
    private byte successCount;
    private byte consecutiveWin;
    private int currentScore;
    private boolean isGridSizeChanged = false, isSpotlightUsed = false, isStrikeUsed = false, gameLost = false;
    private List<Integer> targetIndices;
    private final List<Button> targetButtons = new ArrayList<>();
    private byte difficultyLevel = 1;

    private int currentBaseColor;
    private int currentGridDelta;
    private String BEST_SCORE_KEY_BASED_ON_DIFFICULTY;

    private ColorpuzzleLayoutBinding vb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = ColorpuzzleLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        // Handle back navigation.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() { handleBackNavigation(); }
        });

        getBindings();
        setupGame();
        updateUI();
    }

    private void getBindings() {
        ShopButtonLayoutBinding ShopButtonBinding = ShopButtonLayoutBinding.bind(vb.ShopButton.getRoot());
        ShopLayoutBinding ShopBinding = ShopLayoutBinding.bind(vb.Shop.getRoot());
        NavigationLayoutBinding NavigationBinding = NavigationLayoutBinding.bind(vb.Navigation.getRoot());
        ShadowLayoutBinding ShadowBinding = ShadowLayoutBinding.bind(vb.Shadow.getRoot());
        setBindings(ShopButtonBinding, ShopBinding, NavigationBinding, ShadowBinding);
        setShopUpdateListener(this);
    }

    private void setupGame() {
        vb.gridContainer.setLayoutTransition(new LayoutTransition());
        vb.Shop.colorpuzzleTV.setSelected(true);
        vb.Shop.wordleTV.setSelected(false);
        vb.Shop.shopWordleLL.setVisibility(GONE);
        vb.Shop.shopCPLL.setVisibility(VISIBLE);
        updateGameBasedOnDifficulty();
        updateBestScoreDisplay();
    }

    private void generateGrid() {
        removeSpotlightBorder();
        resetPowerUps();
        targetButtons.clear();

        vb.gridContainer.removeAllViews();
        vb.gridContainer.post(() -> {
            int containerWidth = vb.gridContainer.getWidth();
            int paddingPx = dpToPx(8);
            int gapPx = dpToPx(1);
            int totalMargins = currentGridSize * gapPx * 2;
            int buttonSize = (containerWidth - (paddingPx * 2) - totalMargins) / currentGridSize;

            GridLayout gridLayout = createGridLayout(currentGridSize, paddingPx);
            currentBaseColor = getBaseColor(); // Store base color for the grid
            // Determine adjust direction once per grid
            int adjust = new Random().nextBoolean() ? currentColorDelta : -currentColorDelta;
            currentGridDelta = adjust; // Store adjust for the current grid
            int targetColor = Color.rgb(
                    clampColorValue(Color.red(currentBaseColor) + adjust),
                    clampColorValue(Color.green(currentBaseColor) + adjust),
                    clampColorValue(Color.blue(currentBaseColor) + adjust)
            );
            int totalButtons = currentGridSize * currentGridSize;

            // Generate target indices based on difficulty.
            int targetCount = difficultyLevel;
            targetIndices = generateUniqueRandomIndices(totalButtons, targetCount);

            for (int i = 0; i < totalButtons; i++) {
                boolean isTarget = targetIndices.contains(i);
                Button button = createGridButton(buttonSize, gapPx, isTarget, currentBaseColor, targetColor);
                if (isTarget) {
                    targetButtons.add(button);
                }
                gridLayout.addView(button);
            }
            vb.gridContainer.addView(gridLayout);
        });
    }

    private List<Integer> generateUniqueRandomIndices(int total, int count) {
        Set<Integer> indices = new HashSet<>();
        Random random = new Random();
        while (indices.size() < count && indices.size() < total) {
            indices.add(random.nextInt(total));
        }
        return new ArrayList<>(indices);
    }

    private GridLayout createGridLayout(int gridSize, int paddingPx) {
        GridLayout gridLayout = new GridLayout(this);
        gridLayout.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        gridLayout.setColumnCount(gridSize);
        gridLayout.setRowCount(gridSize);
        FrameLayout.LayoutParams gridParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        gridParams.gravity = Gravity.CENTER;
        gridLayout.setLayoutParams(gridParams);
        return gridLayout;
    }

    private void removeSpotlightBorder() {
        View spotlightBorder = vb.gridContainer.findViewWithTag("spotlight_border");
        if (spotlightBorder != null) {
            vb.gridContainer.removeView(spotlightBorder);
        }
    }

    private void resetPowerUps() {
        isSpotlightUsed = false;
        isStrikeUsed = false;
    }

    private Button createGridButton(int size, int gapPx, boolean isTarget, int baseColor, int targetColor) {
        Button button = new Button(this);
        button.setBackgroundColor(isTarget ? targetColor : baseColor);
        button.setStateListAnimator(AnimatorInflater.loadStateListAnimator(this, R.animator.pulse_animation));
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = size;
        params.height = size;
        params.setMargins(gapPx, gapPx, gapPx, gapPx);
        button.setLayoutParams(params);

        // Set click listener to handle win/loss.
        button.setOnClickListener(v -> {
            if (isTarget) {
                if ("found".equals(button.getTag())) {
                    return;
                }
                button.setTag("found");
                // Set the tick emoji and adjust text size.
                button.setText("\uD83C\uDFAF");
                button.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 11);

                // Check if all target buttons are found.
                boolean allFound = true;
                for (Button targetButton : targetButtons) {
                    if (!"found".equals(targetButton.getTag())) {
                        allFound = false;
                        break;
                    }
                }
                if (allFound) {
                    handleWin();
                }
            } else {
                handleLoss(button);
            }
            playSoundAndVibrate(this,
                    isTarget ? (isGridSizeChanged ? R.raw.sound_new_level : R.raw.sound_success) : R.raw.sound_heart_crack,
                    false, 0);
            isGridSizeChanged = false;
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
        if(difficultyLevel == 1) increaseAndSaveCurrencyCount(20);
        else if(difficultyLevel == 2) increaseAndSaveCurrencyCount(20 + Math.abs(currentScore/2));
        else increaseAndSaveCurrencyCount(30 + currentScore);


        if (currentScore > sharedPreferences.getInt(BEST_SCORE_KEY_BASED_ON_DIFFICULTY, 0)) {
            saveToSharedPreferences(BEST_SCORE_KEY_BASED_ON_DIFFICULTY, currentScore);
            updateBestScoreDisplay();
            playCrownAnimation(true);
        }
        generateGrid();
    }



    private void playCrownAnimation(boolean play) {
        if (play) {
            vb.crownLAV.playAnimation();
            vb.crownLAV.setVisibility(VISIBLE);
        }
        else {
            vb.crownLAV.cancelAnimation();
            vb.crownLAV.setVisibility(GONE);
        }

    }

    private void handleLoss(Button button) {
        animateViewJiggle(button, 150);
        consecutiveWin = 0;
        if (numberOfLives > 0) {
            numberOfLives--;
        }
        if (numberOfLives == 0) {
            gameLost = true;
            toggleVisibility(true, vb.Shadow.ShadowLayout, vb.gameOverLAV);
            vb.gameOverLAV.playAnimation();
            playSoundAndVibrate(this, R.raw.sound_game_over, true, 100);
            // Animate all target buttons on game loss.
            for (Button tButton : targetButtons) {
                animateBlink(tButton, 300, 3);
            }
        }
        playHeartAnimation();
    }

    private void handleLevelUp() {
        if (successCount >= CHANGE_GRID_SIZE_AFTER) {
            if (currentGridSize < MAX_GRID_SIZE) {
                currentGridSize++;
                isGridSizeChanged = true;
            }
            if (currentColorDelta > LOWEST_COLOR_DELTA) {
                currentColorDelta -= CHANGE_IN_COLOR_DELTA;
            }
            successCount = 0;
        }
    }

    private void handleLifeIncrement() {
        if (consecutiveWin == 5) {
            if (numberOfLives < MAX_LIVES) {
                numberOfLives++;
            }
            consecutiveWin = 0;
            resetHearts();
        }
    }

    private void resetHearts() {
        if(numberOfLives == 3) {
            resetHeartAnimation(vb.heartOneLAV);
            resetHeartAnimation(vb.heartTwoLAV);
            resetHeartAnimation(vb.heartThreeLAV);
        }
        else if(numberOfLives == 2) {
            resetHeartAnimation(vb.heartOneLAV);
            resetHeartAnimation(vb.heartTwoLAV);
        }
    }

    private void resetHeartAnimation(LottieAnimationView heart) {
        heart.cancelAnimation();
        heart.setFrame(0);
    }

    private void playHeartAnimation() {
        if (numberOfLives == 2) {
            vb.heartThreeLAV.playAnimation();
        } else if (numberOfLives == 1) {
            vb.heartTwoLAV.playAnimation();
        } else {
            vb.heartOneLAV.playAnimation();
        }
    }

    private void updateScoreDisplay() {
        vb.currentScoreTV.setText(getString(R.string.score, currentScore));
    }

    private void updateBestScoreDisplay() {
        vb.bestScoreTV.setText(getString(R.string.best_score, sharedPreferences.getInt(BEST_SCORE_KEY_BASED_ON_DIFFICULTY, 0)));
    }

    public void handleResetClick(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        resetGameState();
    }

    public void handleStrikeClick(View view) {
        if (strikeCount == 0 || isStrikeUsed) {
            playSoundAndVibrate(this, R.raw.sound_error, false, 0);
            return;
        }

        isStrikeUsed = true;

        decreaseAndSaveStrikeCount();
        vb.strikeCountTV.setText(String.valueOf(strikeCount));

        playSoundAndVibrate(this, R.raw.sound_explosion, false, 0);

        vb.strikeLAV.playAnimation();
        vb.strikeLAV.setVisibility(VISIBLE);
        eliminateNonTargetButtons();
    }

    private void eliminateNonTargetButtons() {
        if (vb.gridContainer.getChildCount() == 0) return;
        GridLayout gridLayout = (GridLayout) vb.gridContainer.getChildAt(0);
        List<View> nonTargetButtons = new ArrayList<>();

        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);
            if (child instanceof Button
                    && !targetButtons.contains(child)
                    && child.getVisibility() == View.VISIBLE
                    && child.getParent() == gridLayout) { // Check parent
                nonTargetButtons.add(child);
            }
        }

        int numToEliminate = nonTargetButtons.size() / 2;
        Collections.shuffle(nonTargetButtons);

        for (int i = 0; i < numToEliminate; i++) {
            View button = nonTargetButtons.get(i);
            if (button.getParent() == gridLayout) { // Ensure button is still in the grid
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

    public void handleJumpClick(View view) {
        if(jumpCount == 0) {
            playSoundAndVibrate(this, R.raw.sound_error, false, 0);
            return;
        }

        decreaseAndSaveJumpCount();
        vb.jumpCountTV.setText(String.valueOf(jumpCount));

        playSoundAndVibrate(this, R.raw.sound_skip, true, 50);
        vb.jumpLAV.setVisibility(VISIBLE);
        vb.jumpLAV.playAnimation();
        vb.jumpLAV.postDelayed(() -> vb.jumpLAV.setVisibility(GONE), 600);

        vb.gridContainer.animate()
                .translationX(vb.gridContainer.getWidth())
                .setDuration(200)
                .withEndAction(() -> {
                    generateGrid();
                    vb.gridContainer.setTranslationX(-vb.gridContainer.getWidth());
                    vb.gridContainer.animate()
                            .translationX(0)
                            .setDuration(200);
                });
    }

    public void handleSpotlightClick(View view) {
        if (isSpotlightUsed || spotlightCount == 0) {
            playSoundAndVibrate(this, R.raw.sound_error, false, 0);
            return;
        }
        playSoundAndVibrate(this, R.raw.sound_reveal, true, 50);

        decreaseAndSaveSpotlightCount();
        vb.spotlightCountTV.setText(String.valueOf(spotlightCount));

        vb.spotlightLAV.setVisibility(View.VISIBLE);
        vb.spotlightLAV.setMinFrame(20);
        vb.spotlightLAV.playAnimation();
        animateViewScale(vb.spotlightLAV, 0, 1, 200);
        new Handler().postDelayed(() -> animateViewScale(vb.spotlightLAV, 1, 0, 200), 400);

        isSpotlightUsed = true;
        if (targetButtons.isEmpty()) return;
        View border = vb.gridContainer.findViewWithTag("spotlight_border");
        if (border == null) {
            border = createSpotlightBorder();
        }
        assert border != null;
        blinkBorderAndHide(border);
    }

    private View createSpotlightBorder() {
        // Calculate a block boundary that includes (at least) one target.
        int blockDimension = Math.max(3, (int) Math.ceil(currentGridSize * 0.6));
        blockDimension = Math.min(blockDimension, currentGridSize);

        // Select an anchor target button that hasn't been found.
        Button anchor = null;
        for (Button btn : targetButtons) {
            if (!"found".equals(btn.getTag())) {
                anchor = btn;
                break;
            }
        }

        // If all target buttons are already found, don't show the spotlight.
        if (anchor == null) {
            return null;
        }

        // Get the grid layout from the container.
        GridLayout grid = (GridLayout) vb.gridContainer.getChildAt(0);
        // Use the grid layout to get the index of the anchor button.
        int targetIndex = grid.indexOfChild(anchor);

        int targetRow = targetIndex / currentGridSize;
        int targetCol = targetIndex % currentGridSize;
        int minRow = Math.max(0, targetRow - blockDimension + 1);
        int maxRow = Math.min(targetRow, currentGridSize - blockDimension);
        int blockStartRow = (minRow == maxRow) ? minRow : minRow + new Random().nextInt(maxRow - minRow + 1);
        int minCol = Math.max(0, targetCol - blockDimension + 1);
        int maxCol = Math.min(targetCol, currentGridSize - blockDimension);
        int blockStartCol = (minCol == maxCol) ? minCol : minCol + new Random().nextInt(maxCol - minCol + 1);

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

        View border = new View(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(borderWidth, borderHeight);
        params.leftMargin = relativeLeft;
        params.topMargin = relativeTop;
        border.setLayoutParams(params);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.TRANSPARENT);
        int thickness = dpToPx(1 + currentGridSize / 4);
        drawable.setStroke(thickness, BLUE_COLOR);
        border.setBackground(drawable);
        border.setTag("spotlight_border");
        vb.gridContainer.addView(border);

        positionSpotlight(borderWidth, borderHeight, relativeLeft, relativeTop);
        return border;
    }

    private void positionSpotlight(int borderWidth, int borderHeight, int relativeLeft, int relativeTop) {
        int centerX = relativeLeft + borderWidth / 2;
        int centerY = relativeTop + borderHeight / 2;
        vb.spotlightLAV.post(() -> {
            int spotlightWidth = vb.spotlightLAV.getWidth();
            int spotlightHeight = vb.spotlightLAV.getHeight();
            vb.spotlightLAV.setX(centerX - spotlightWidth / 2f);
            vb.spotlightLAV.setY(centerY - spotlightHeight / 2f);
        });
    }

    public void handleContrastClick(View view) {
        if(contrastCount == 0) {
            playSoundAndVibrate(this, R.raw.sound_error, false, 0);
            return;
        }
        decreaseAndSaveContrastCount();
        vb.contrastCountTV.setText(String.valueOf(contrastCount));

        playSoundAndVibrate(this, R.raw.sound_contrast, true, 50);
        vb.contrastLAV.setVisibility(VISIBLE);
        vb.contrastLAV.playAnimation();

        vb.contrastLAV.postDelayed(() -> {
            vb.contrastLAV.cancelAnimation();
            vb.contrastLAV.setVisibility(View.GONE);
        }, 400);

        // Increase delta by 1 in the original direction (positive/negative)
        int newDelta = currentGridDelta + (currentGridDelta > 0 ? 1 : -1);

        // Calculate new target color with updated delta
        int newTargetColor = Color.rgb(
                clampColorValue(Color.red(currentBaseColor) + newDelta),
                clampColorValue(Color.green(currentBaseColor) + newDelta),
                clampColorValue(Color.blue(currentBaseColor) + newDelta)
        );

        // Update all visible target buttons that haven't been found
        for (Button targetButton : targetButtons) {
            if (targetButton.getVisibility() == View.VISIBLE && !"found".equals(targetButton.getTag())) {
                targetButton.setBackgroundColor(newTargetColor);
            }
        }

        // Update currentGridDelta to reflect the new delta for subsequent clicks
        currentGridDelta = newDelta;
    }

    private void blinkBorderAndHide(final View border) {
        border.animate().alpha(0f).setDuration(200)
                .withEndAction(() -> border.animate().alpha(1f).setDuration(200)
                        .withEndAction(() -> blinkBorderAndHide(border)).start()).start();
    }

    public void handleDifficultyButton(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        vb.difficultyLAV.playAnimation();
        animateViewScale(vb.DifficultyMenu.ColorPuzzleDifficultyLayout, 0f, 1f, 200);
        toggleVisibility(true, vb.DifficultyMenu.ColorPuzzleDifficultyLayout, vb.Shadow.ShadowLayout);
    }

    public void onDifficultySelected(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        byte temp = difficultyLevel;

        difficultyLevel = Byte.parseByte(view.getTag().toString());

        if(difficultyLevel == 0 || temp == difficultyLevel) {
            difficultyLevel = temp;
            toggleVisibility(false, vb.DifficultyMenu.ColorPuzzleDifficultyLayout, vb.Shadow.ShadowLayout);
            return;
        }

        updateGameBasedOnDifficulty();
        toggleVisibility(false, vb.DifficultyMenu.ColorPuzzleDifficultyLayout, vb.Shadow.ShadowLayout);
    }

    private void updateGameBasedOnDifficulty() {
        int easyColor = CHARCOAL_COLOR, mediumColor = CHARCOAL_COLOR, hardColor = CHARCOAL_COLOR;
        switch (difficultyLevel) {
            case 1:
                BEST_SCORE_KEY_BASED_ON_DIFFICULTY = PUZZLE_EASY_SCORE_KEY;
                easyColor = GREEN_COLOR;
                animateViewScale(vb.DifficultyMenu.easyLayout, 1f, 1.05f, 200);
                resetGameForDifficulty((byte) 3, (byte) 5, (byte) 30, (byte) 10, (byte) 4);
                break;
            case 2:
                BEST_SCORE_KEY_BASED_ON_DIFFICULTY = PUZZLE_MEDIUM_SCORE_KEY;
                mediumColor = YELLOW_COLOR;
                animateViewScale(vb.DifficultyMenu.mediumLayout, 1f, 1.05f, 200);
                resetGameForDifficulty((byte) 5, (byte) 7, (byte) 20, (byte) 8, (byte) 4);
                break;

            case 3:
                BEST_SCORE_KEY_BASED_ON_DIFFICULTY = PUZZLE_HARD_SCORE_KEY;
                hardColor = RED_COLOR;
                animateViewScale(vb.DifficultyMenu.hardLayout, 1f, 1.05f, 200);
                resetGameForDifficulty((byte) 7, (byte) 9, (byte) 5, (byte) 5, (byte) 0);
                break;
        }
        // Update difficulty button backgrounds:
        changeBackgroundColor(vb.DifficultyMenu.easyLayout, easyColor);
        changeBackgroundColor(vb.DifficultyMenu.mediumLayout, mediumColor);
        changeBackgroundColor(vb.DifficultyMenu.hardLayout, hardColor);
    }

    private void resetGameForDifficulty(byte initialGrid, byte maxGrid, byte initialDelta, byte lowestDelta, byte changeDelta) {
        currentScore = 0;
        consecutiveWin = 0;
        successCount = 0;
        MAX_GRID_SIZE = maxGrid;
        LOWEST_COLOR_DELTA = lowestDelta;
        CHANGE_IN_COLOR_DELTA = changeDelta;
        currentGridSize = initialGrid;
        currentColorDelta = initialDelta;
        numberOfLives = MAX_LIVES;
        resetHearts();
        updateScoreDisplay();
        updateBestScoreDisplay();
        playCrownAnimation(false);
        generateGrid();
    }

    public void handleExitButtons(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        if ("no".equals(view.getTag())) {
            toggleVisibility(false, vb.leaveRL, vb.Shadow.ShadowLayout);
        } else {
            changeActivity(this, GamesActivity.class);
        }
    }

    private void handleBackNavigation() {
        vibrate(this, 50);
        if (currentScore > 0 && !gameLost) {
            toggleVisibility(vb.leaveRL.getVisibility() != View.VISIBLE, vb.leaveRL, vb.Shadow.ShadowLayout);
        } else {
            changeActivity(this, GamesActivity.class);
        }
    }

    private void resetGameState() {
        gameLost = false;
        toggleVisibility(false, vb.Shadow.ShadowLayout, vb.crownLAV, vb.gameOverLAV);
        updateGameBasedOnDifficulty();
    }

    private int getBaseColor() {
        return Color.rgb(new Random().nextInt(256), new Random().nextInt(256), new Random().nextInt(256));
    }

    private int clampColorValue(int value) {
        return Math.min(255, Math.max(0, value));
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    @Override
    protected Class<?> getBackDestination() {
        return GamesActivity.class;
    }

    @Override
    public void onShopClosed() {
        vb.strikeCountTV.setText(String.valueOf(strikeCount));
        vb.spotlightCountTV.setText(String.valueOf(spotlightCount));
        vb.contrastCountTV.setText(String.valueOf(contrastCount));
        vb.jumpCountTV.setText(String.valueOf(jumpCount));
    }
    private void updateUI() {
        vb.strikeCountTV.setText(String.valueOf(strikeCount));
        vb.spotlightCountTV.setText(String.valueOf(spotlightCount));
        vb.contrastCountTV.setText(String.valueOf(contrastCount));
        vb.jumpCountTV.setText(String.valueOf(jumpCount));
    }
}