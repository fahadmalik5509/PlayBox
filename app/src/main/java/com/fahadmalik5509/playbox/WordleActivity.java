package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.*;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WordleActivity extends AppCompatActivity {

    private final int ROWS = 6;
    private final int COLS = 5;

    private TextView enterKey, backspaceKey, currentStreakTextView, streakTooltipTextView;
    private final TextView[] keyboard = new TextView[26];
    private ImageView replayImageView, homeImageView, settingImageView, backImageView;
    private final TextView[][] letterTiles = new TextView[ROWS][COLS];
    private final List<String> commonWords = new ArrayList<>();
    private final List<String> dictionary = new ArrayList<>();
    private final StringBuilder userGuess = new StringBuilder();
    private int currentColumn = 0, currentRow = 0;
    private String targetWord;
    private boolean gameWon = false;
    private boolean gameLost = false;
    private int currentStreak;
    LottieAnimationView flameLottieAnimationView;

    private int revealGuessWord = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wordle_layout);

        initializeViews();
        animateViewsPulse();
        loadGameData();
        updateStreak();
    }

    private void loadGameData() {
        loadPreference(this);
        loadCommonWords();
        loadDictionary();
        loadARandomCommonWord();
    }

    // OnClick Method
    public void keyboardClicked(View view) {

        if (gameWon || gameLost) return;
        String pressedKey = (String) view.getTag();

        if (pressedKey.equals("enter")) enterClicked();
        else if (pressedKey.equals("backspace")) backspaceClicked();
        else alphabetClicked(pressedKey);

    }

    private void alphabetClicked(String alphabet) {

        if(currentColumn == COLS) { 
            jiggleRow();
            return; 
        }

        playSound(this,R.raw.key);
        animateViewBounce(letterTiles[currentRow][currentColumn]);
        letterTiles[currentRow][currentColumn].setText(alphabet);
        userGuess.insert(currentColumn,alphabet);
        currentColumn++;
    }

    private void enterClicked() {

        revealGuessWord++;
        if(revealGuessWord >= 5) {
            Toast.makeText(this, targetWord, Toast.LENGTH_SHORT).show();
            revealGuessWord = 0;
        }

        if (!isGuessComplete() || !isValidGuess(userGuess.toString())) {
            jiggleRow();
            return;
        }

        playSound(this,R.raw.enter);
        boolean[] letterMatched = new boolean[COLS];
        int[] targetLetterCount = initializeTargetLetterCount();

        markExactMatches(letterMatched, targetLetterCount);
        markPartialMatches(letterMatched, targetLetterCount);
        handleEndOfTurn();

        revealGuessWord = 0;
    }

    private int[] initializeTargetLetterCount() {
        int[] targetLetterCount = new int[26];
        for (char targetChar : targetWord.toCharArray()) {
            targetLetterCount[targetChar - 'A']++;
        }
        return targetLetterCount;
    }

    private void markExactMatches(boolean[] letterMatched, int[] targetLetterCount) {
        for (int i = 0; i < COLS; i++) {
            char guessChar = userGuess.charAt(i);
            if (targetWord.charAt(i) == guessChar) {
                changeBackgroundColor(letterTiles[currentRow][i], GREEN_COLOR);
                letterMatched[i] = true;
                targetLetterCount[guessChar - 'A']--;
                updateKeyboardColor(guessChar, GREEN_COLOR);
            }
        }
    }

    private void markPartialMatches(boolean[] letterMatched, int[] targetLetterCount) {
        for (int i = 0; i < COLS; i++) {
            if (letterMatched[i]) continue;

            char guessChar = userGuess.charAt(i);
            if (targetLetterCount[guessChar - 'A'] > 0) {
                changeBackgroundColor(letterTiles[currentRow][i], YELLOW_COLOR);
                targetLetterCount[guessChar - 'A']--;
                updateKeyboardColor(guessChar, YELLOW_COLOR);
            } else {
                changeBackgroundColor(letterTiles[currentRow][i], GRAY_COLOR);
                updateKeyboardColor(guessChar, GRAY_COLOR);
            }
        }
    }

    private void handleEndOfTurn()
    {
        if (targetWord.equals(userGuess.toString())) handleWin();
        else {
            currentRow++;
            currentColumn = 0;
            userGuess.setLength(0);
            if (currentRow == ROWS) handleLoss();
        }
    }

    private void handleWin() {
        gameWon = true;
        playSound(this, R.raw.win);
        waveRow();
        replayImageView.setVisibility(View.VISIBLE);
        currentStreak++;
        updateStreak();
    }

    private void handleLoss() {
        gameLost = true;
        playSound(this, R.raw.draw);
        Toast.makeText(this, "The word was: " + targetWord, Toast.LENGTH_LONG).show();
        replayImageView.setVisibility(View.VISIBLE);
        currentStreak = 0;
        updateStreak();
    }

    private void updateStreak()
    {

        int highestStreak = sharedPreferences.getInt(WORDLE_HIGHEST_STREAK, 0);

        saveToSharedPreferences(WORDLE_STREAK, currentStreak);
        currentStreakTextView.setText(String.valueOf(currentStreak));

        if (currentStreak >= highestStreak) {

            if (currentStreak > highestStreak) {
                saveToSharedPreferences(WORDLE_HIGHEST_STREAK, currentStreak);
            }
            changeBackgroundColor(currentStreakTextView, Color.TRANSPARENT);
            if (flameLottieAnimationView.getVisibility() != View.VISIBLE) {
                flameLottieAnimationView.setVisibility(View.VISIBLE);
                flameLottieAnimationView.playAnimation();
            }
        } else {

            currentStreakTextView.setBackgroundTintList(null);
            currentStreakTextView.setBackgroundResource(R.drawable.circle_background);
            if (flameLottieAnimationView.getVisibility() == View.VISIBLE) {
                flameLottieAnimationView.setVisibility(View.GONE);
                flameLottieAnimationView.cancelAnimation();
            }
        }
    }

    private void updateKeyboardColor(char letter, int color) {
        int index = letter - 'A';
        int currentColor = getCurrentColor(keyboard[index]);

        if (color == GREEN_COLOR) {
            if (currentColor != GREEN_COLOR) {
                changeBackgroundColor(keyboard[index], GREEN_COLOR);
            }
        } else if (color == YELLOW_COLOR && currentColor != GREEN_COLOR) {
            if (currentColor != YELLOW_COLOR) {
                changeBackgroundColor(keyboard[index], YELLOW_COLOR);
            }
        } else if (color == GRAY_COLOR && currentColor != GREEN_COLOR && currentColor != YELLOW_COLOR) {
            changeBackgroundColor(keyboard[index], GRAY_COLOR);
        }
    }

    private int getCurrentColor(TextView view) {
        Drawable background = view.getBackground();
        if (background instanceof ColorDrawable) {
            return ((ColorDrawable) background).getColor();
        }
        return Color.TRANSPARENT;
    }


    private void backspaceClicked()
    {
        if(currentColumn == 0)
        {
            jiggleRow();
            return;
        }

        playSound(this,R.raw.backspace);
        currentColumn--;
        userGuess.deleteCharAt(currentColumn);
        letterTiles[currentRow][currentColumn].setText("");
    }

    // OnClick Method
    public void resetGame(View view) {
        gameWon = false;
        gameLost = false;
        currentRow = 0;
        currentColumn = 0;
        userGuess.setLength(0);
        replayImageView.setVisibility(View.GONE);
        loadARandomCommonWord();

        for(int row = 0; row < ROWS; row++) {
            for(int column = 0; column < 5; column++) {
                letterTiles[row][column].setText("");
                changeBackgroundColor(letterTiles[row][column],RED_COLOR);
            }
        }

        for(int i = 0; i < 26; i++) {
            changeBackgroundColor(keyboard[i],BEIGE_COLOR);
        }
    }

    private boolean isGuessComplete() {
        return userGuess.length() == COLS;
    }

    private boolean isValidGuess(String guess) {
        return dictionary.contains(guess.toUpperCase());
    }

    // OnClick Method
    public void streakClicked(View view) {
        playSound(this, R.raw.click_ui);
        streakTooltipTextView.setText("Streak\n(Current: " + sharedPreferences.getInt(WORDLE_STREAK, 0) + ")"+ "\n(Best: " + sharedPreferences.getInt(WORDLE_HIGHEST_STREAK, 0) + ")");
        streakTooltipTextView.setVisibility(View.VISIBLE);
        streakTooltipTextView.postDelayed(() -> streakTooltipTextView.setVisibility(View.GONE), 2000);
    }

    private void jiggleRow() {
        playSound(this,R.raw.click_error);
        for (int i = 0; i < COLS; i++)
            animateViewJiggle(letterTiles[currentRow][i]);
    }

    private void waveRow() {
        final int duration = 150;
        final int delayBetweenAnimations = 50;
        final float scaleDownValue = 0.7f;

        for (int i = 0; i < COLS; i++) {
            final int index = i;
            final int startDelay = i * delayBetweenAnimations;

            // Scale down animation
            letterTiles[currentRow][i].postDelayed(() -> {
                animateViewScale(letterTiles[currentRow][index], 1.0f, scaleDownValue, duration);
            }, startDelay);

            // Scale up animation
            letterTiles[currentRow][i].postDelayed(() -> {
                animateViewScale(letterTiles[currentRow][index], scaleDownValue, 1.0f, duration);
            }, startDelay + duration + delayBetweenAnimations);
        }
    }

    public void loadARandomCommonWord() {

        targetWord = commonWords.get(new Random().nextInt(commonWords.size()));
    }

    private void loadDictionary() {
        InputStream inputStream = this.getResources().openRawResource(R.raw.dictionary);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                dictionary.add(line.toUpperCase().trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void loadCommonWords() {
        InputStream inputStream = this.getResources().openRawResource(R.raw.commonwords);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                commonWords.add(line.toUpperCase().trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @Override
    public void onBackPressed() {
        vibrate(this, 50);
        changeActivity(this, HomeActivity.class, true, false);
    }

    private void initializeViews() {

        keyboard[0] = findViewById(R.id.aTextView);
        keyboard[1] = findViewById(R.id.bTextView);
        keyboard[2] = findViewById(R.id.cTextView);
        keyboard[3] = findViewById(R.id.dTextView);
        keyboard[4] = findViewById(R.id.eTextView);
        keyboard[5] = findViewById(R.id.fTextView);
        keyboard[6] = findViewById(R.id.gTextView);
        keyboard[7] = findViewById(R.id.hTextView);
        keyboard[8] = findViewById(R.id.iTextView);
        keyboard[9] = findViewById(R.id.jTextView);
        keyboard[10] = findViewById(R.id.kTextView);
        keyboard[11] = findViewById(R.id.lTextView);
        keyboard[12] = findViewById(R.id.mTextView);
        keyboard[13] = findViewById(R.id.nTextView);
        keyboard[14] = findViewById(R.id.oTextView);
        keyboard[15] = findViewById(R.id.pTextView);
        keyboard[16] = findViewById(R.id.qTextView);
        keyboard[17] = findViewById(R.id.rTextView);
        keyboard[18] = findViewById(R.id.sTextView);
        keyboard[19] = findViewById(R.id.tTextView);
        keyboard[20] = findViewById(R.id.uTextView);
        keyboard[21] = findViewById(R.id.vTextView);
        keyboard[22] = findViewById(R.id.wTextView);
        keyboard[23] = findViewById(R.id.xTextView);
        keyboard[24] = findViewById(R.id.yTextView);
        keyboard[25] = findViewById(R.id.zTextView);
        enterKey = findViewById(R.id.enterTextView);
        backspaceKey = findViewById(R.id.backspaceTextView);

        letterTiles[0][0] = findViewById(R.id._0_0);
        letterTiles[0][1] = findViewById(R.id._0_1);
        letterTiles[0][2] = findViewById(R.id._0_2);
        letterTiles[0][3] = findViewById(R.id._0_3);
        letterTiles[0][4] = findViewById(R.id._0_4);
        letterTiles[1][0] = findViewById(R.id._1_0);
        letterTiles[1][1] = findViewById(R.id._1_1);
        letterTiles[1][2] = findViewById(R.id._1_2);
        letterTiles[1][3] = findViewById(R.id._1_3);
        letterTiles[1][4] = findViewById(R.id._1_4);
        letterTiles[2][0] = findViewById(R.id._2_0);
        letterTiles[2][1] = findViewById(R.id._2_1);
        letterTiles[2][2] = findViewById(R.id._2_2);
        letterTiles[2][3] = findViewById(R.id._2_3);
        letterTiles[2][4] = findViewById(R.id._2_4);
        letterTiles[3][0] = findViewById(R.id._3_0);
        letterTiles[3][1] = findViewById(R.id._3_1);
        letterTiles[3][2] = findViewById(R.id._3_2);
        letterTiles[3][3] = findViewById(R.id._3_3);
        letterTiles[3][4] = findViewById(R.id._3_4);
        letterTiles[4][0] = findViewById(R.id._4_0);
        letterTiles[4][1] = findViewById(R.id._4_1);
        letterTiles[4][2] = findViewById(R.id._4_2);
        letterTiles[4][3] = findViewById(R.id._4_3);
        letterTiles[4][4] = findViewById(R.id._4_4);
        letterTiles[5][0] = findViewById(R.id._5_0);
        letterTiles[5][1] = findViewById(R.id._5_1);
        letterTiles[5][2] = findViewById(R.id._5_2);
        letterTiles[5][3] = findViewById(R.id._5_3);
        letterTiles[5][4] = findViewById(R.id._5_4);

        replayImageView = findViewById(R.id.resetImg);
        backImageView = findViewById(R.id.ivBackIcon);
        homeImageView = findViewById(R.id.ivHomeIcon);
        settingImageView = findViewById(R.id.ivSettingIcon);
        currentStreakTextView = findViewById(R.id.tvStreak);
        streakTooltipTextView = findViewById(R.id.tvStreakTooltip);
        flameLottieAnimationView = findViewById(R.id.lavFlame);

        currentStreak = sharedPreferences.getInt(WORDLE_STREAK,0);
    }
    private void animateViewsPulse() {
        for(int i=0; i<26; i++) animateViewPulse(this,keyboard[i]);

        animateViewPulse(this,enterKey);
        animateViewPulse(this,backspaceKey);

        animateViewPulse(this,replayImageView);
        animateViewPulse(this,homeImageView);
        animateViewPulse(this,settingImageView);
        animateViewPulse(this,backImageView);
        animateViewPulse(this,currentStreakTextView);
    }
}