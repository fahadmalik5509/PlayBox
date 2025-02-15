package com.fahadmalik5509.playbox;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.fahadmalik5509.playbox.ActivityUtils.*;
import com.fahadmalik5509.playbox.databinding.ColorpuzzleLayoutBinding;
import androidx.appcompat.app.AppCompatActivity;
import android.animation.LayoutTransition;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;

import java.util.Random;

public class ColorPuzzleActivity extends AppCompatActivity {

    private ColorpuzzleLayoutBinding vb;
    private final byte MAX_GRID_SIZE = 9;
    private byte currentGridSize = 3, numberofLifes = 3, consecutiveWin = 0;
    private int delta = 30, successCount = 0, score = 0;

    boolean newlevel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = ColorpuzzleLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());
        vb.bestScoreTV.setText("Best Score: " + sharedPreferences.getInt(PUZZLE_BEST_SCORE, 0));
        vb.gridContainer.setLayoutTransition(new LayoutTransition());
        animateViewPulse(this, vb.resetTV, true);
        generateGrid(currentGridSize);
    }

    private void generateGrid(final int gridSize) {
        vb.gridContainer.removeAllViews();
        vb.gridContainer.post(() -> {
            int containerWidth = vb.gridContainer.getWidth();
            int paddingPx = dpToPx(8), gapPx = dpToPx(1);
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
                gridLayout.addView(createGridButton(buttonSize, gapPx, i == targetIndex, baseColor, targetColor));
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
            if (isTarget) gameWon(); else gameLose(button);
            playSound(this, isTarget ? (newlevel ? R.raw.newlevel : R.raw.sucess) : R.raw.aheartbreak);
            newlevel = false;
        });
        return button;
    }

    private void gameWon() {
        consecutiveWin++;
        if (++successCount >= 5) {
            if (currentGridSize < MAX_GRID_SIZE) {
                currentGridSize++;
                newlevel = true;
            }
            if (delta > 5) delta -= 5;
            successCount = 0;
        }

        if(consecutiveWin >= 5) {
            if(numberofLifes < 3) numberofLifes++;
            consecutiveWin = 0;

            switch (numberofLifes) {
                case 3:
                    vb.heartOneLAV.setFrame(0);
                    vb.heartTwoLAV.setFrame(0);
                    vb.heartThreeLAV.setFrame(0);
                    break;
                case 2:
                    vb.heartOneLAV.setFrame(0);
                    vb.heartTwoLAV.setFrame(0);
                    break;
                case 1:
                    vb.heartTwoLAV.setFrame(0);
            }
        }

        generateGrid(currentGridSize);
        score++;
        vb.currentScoreTV.setText("Score: " + score);
        if(score > sharedPreferences.getInt(PUZZLE_BEST_SCORE, 0)) {
            saveToSharedPreferences(PUZZLE_BEST_SCORE, score);
            vb.bestScoreTV.setText("Best Score: " + score);
            vb.fireLAV.playAnimation();
            vb.fireLAV.setVisibility(VISIBLE);
        }
    }

    private void gameLose(Button button) {
        animateViewJiggle(button, 150);
        vb.fireLAV.cancelAnimation();
        vb.fireLAV.setVisibility(GONE);
        consecutiveWin = 0;

        if(numberofLifes > 0) numberofLifes--;
        if(numberofLifes == 0) {
            toggleVisibility(true, vb.shadowV);
            vb.gameOverLAV.playAnimation();
            playSound(this,R.raw.agameover);
        }

        switch (numberofLifes) {
            case 2:
                vb.heartThreeLAV.playAnimation();
                break;
            case 1:
                vb.heartTwoLAV.playAnimation();
                break;
            default:
                vb.heartOneLAV.playAnimation();
        }


    }

    public void handleResetClick(View view) {
        playSound(this,R.raw.click_ui);
        score = 0;
        vb.currentScoreTV.setText("Score: " + score);
        numberofLifes = 3;
        currentGridSize = 3;
        consecutiveWin = 0;
        delta = 30;
        toggleVisibility(false, vb.shadowV);
        vb.gameOverLAV.setFrame(0);
        vb.heartOneLAV.setFrame(0);
        vb.heartTwoLAV.setFrame(0);
        vb.heartThreeLAV.setFrame(0);
        vb.gameOverLAV.cancelAnimation();
        vb.fireLAV.cancelAnimation();
        vb.fireLAV.setVisibility(GONE);
        generateGrid(currentGridSize);
    }

    private int getBaseColor() {
        return Color.rgb(getRandomNumber(0, 256), getRandomNumber(0, 256), getRandomNumber(0, 256));
    }

    private int getTargetColor(int baseColor) {
        int adjust = new Random().nextBoolean() ? delta : -delta;
        int r = Math.min(255, Math.max(0, Color.red(baseColor) + adjust));
        int g = Math.min(255, Math.max(0, Color.green(baseColor) + adjust));
        int b = Math.min(255, Math.max(0, Color.blue(baseColor) + adjust));
        return Color.rgb(r, g, b);
    }

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
        changeActivity(this, HomeActivity.class, true, false);
    }

    public void goBack(View view) {
        playSound(this, R.raw.click_ui);
        changeActivity(this, GameModeActivity.class, true, false);
    }
}
