package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.*;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
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

    private static final int ROWS = 6;
    private static final int COLS = 5;

    private TextView enterKey, backspaceKey, currentStreakTextView, streakTooltipTextView;
    private final TextView[] keyboard = new TextView[26];
    private ImageView replayImageView, homeImageView, settingImageView, backImageView;
    private final TextView[][] letterbox = new TextView[ROWS][COLS];
    private final List<String> commonWords = new ArrayList<>();
    private final List<String> dictionary = new ArrayList<>();
    private final StringBuilder userGuess = new StringBuilder();
    private int currentColumn = 0, currentRow = 0;
    private String targetWord;
    private boolean gameWon = false;
    private boolean gameLost = false;
    private int currentStreak, highestStreak;
    LottieAnimationView flameLottieAnimationView;

    private int revealGuessWord = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wordle_layout);

        initializeViews();
        animateViewsPulse();
        loadPreferences(this);
        loadCommonWords();
        loadDictionary();
        loadARandomCommonWord();

        currentStreakTextView.setText(String.valueOf(sharedPreferences.getInt(WORDLE_STREAK, 0)));

        if((sharedPreferences.getInt(WORDLE_STREAK, 0)>=5)) {
            flameLottieAnimationView.setVisibility(View.VISIBLE);
            animateViewScale(flameLottieAnimationView, 0.0f, 1.0f, 2000);
            flameLottieAnimationView.playAnimation();
        }
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
        animateViewBounce(letterbox[currentRow][currentColumn]);
        letterbox[currentRow][currentColumn].setText(alphabet);
        userGuess.insert(currentColumn,alphabet);
        currentColumn++;
    }

    private void enterClicked() {

        revealGuessWord++;
        if(revealGuessWord >= 15) {
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
        for (int i = 0; i < COLS; i++) {
            char targetChar = targetWord.charAt(i);
            targetLetterCount[targetChar - 'A']++;
        }
        return targetLetterCount;
    }

    private void markExactMatches(boolean[] letterMatched, int[] targetLetterCount) {
        for (int i = 0; i < COLS; i++) {
            char guessChar = userGuess.charAt(i);
            if (targetWord.charAt(i) == guessChar) {
                changeBackgroundColor(letterbox[currentRow][i], GREEN_COLOR);
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
                changeBackgroundColor(letterbox[currentRow][i], YELLOW_COLOR);
                targetLetterCount[guessChar - 'A']--;
                updateKeyboardColor(guessChar, YELLOW_COLOR);
            } else {
                changeBackgroundColor(letterbox[currentRow][i], GRAY_COLOR);
                updateKeyboardColor(guessChar, GRAY_COLOR);
            }
        }
    }

    private void handleEndOfTurn() {

        if (targetWord.equals(userGuess.toString())) {

            for (int i = 0; i < 5; i++) {
                float scaleDown = 1.0f - (i * 0.1f);
                int duration = (i + 1) * 100;

                animateViewScale(letterbox[currentRow][i], 1.0f, scaleDown, duration);
                animateViewScale(letterbox[currentRow][i], scaleDown, 1.0f, duration);
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(WORDLE_STREAK, ++currentStreak);
            if(currentStreak>sharedPreferences.getInt(WORDLE_HIGHEST_STREAK, 0)) editor.putInt(WORDLE_HIGHEST_STREAK,currentStreak);
            editor.apply();
            currentStreakTextView.setText(String.valueOf(sharedPreferences.getInt(WORDLE_STREAK, 0)));
            gameWon = true;
            playSound(this, R.raw.win);
            replayImageView.setVisibility(View.VISIBLE);

            if(currentStreak>=5) {
                flameLottieAnimationView.setVisibility(View.VISIBLE);
                animateViewScale(flameLottieAnimationView, 0.0f, 1.0f, 2000);
                flameLottieAnimationView.playAnimation();
            }
        }

        else {
            currentRow++;
            currentColumn = 0;
            userGuess.setLength(0);
        }

        if (currentRow == ROWS) {
            gameLost = true;
            playSound(this, R.raw.draw);

            currentStreak = 0;

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(WORDLE_STREAK, currentStreak);
            editor.apply();

            currentStreakTextView.setText("0");
            Toast.makeText(this, "The word was: " + targetWord, Toast.LENGTH_LONG).show();
            replayImageView.setVisibility(View.VISIBLE);
            flameLottieAnimationView.cancelAnimation();
            animateViewScale(flameLottieAnimationView, 1.0f, 0.0f, 1);
            flameLottieAnimationView.setVisibility(View.GONE);
        }
    }

    private void updateKeyboardColor(char letter, int newDrawableResId)
    {
        int index = letter - 'A';

        if (newDrawableResId == GREEN_COLOR)
        {
            changeBackgroundColor(keyboard[index], GREEN_COLOR);
        }
        else if (newDrawableResId == YELLOW_COLOR && !isKeyboardKeyGreen(index))
        {
            changeBackgroundColor(keyboard[index], YELLOW_COLOR);
        }
        else if (newDrawableResId == GRAY_COLOR && !isKeyboardKeyGreen(index) && !isKeyboardKeyYellow(index))
        {
            changeBackgroundColor(keyboard[index], GRAY_COLOR);
        }
    }

    private void backspaceClicked() {

        playSound(this,R.raw.backspace);

        if(currentColumn == 0) {
            jiggleRow();
            return;
        }

        currentColumn--;
        letterbox[currentRow][currentColumn].setText("");
        userGuess.deleteCharAt(currentColumn);
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
                letterbox[row][column].setText("");
                changeBackgroundColor(letterbox[row][column],RED_COLOR);
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
            animateViewJiggle(letterbox[currentRow][i]);
    }

    private boolean isKeyboardKeyGreen(int index) {
        Drawable background = keyboard[index].getBackground();
        return background instanceof ColorDrawable && ((ColorDrawable) background).getColor() == GREEN_COLOR;
    }

    private boolean isKeyboardKeyYellow(int index) {
        Drawable background = keyboard[index].getBackground();
        return background instanceof ColorDrawable && ((ColorDrawable) background).getColor() == YELLOW_COLOR;
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

        letterbox[0][0] = findViewById(R.id._0_0);
        letterbox[0][1] = findViewById(R.id._0_1);
        letterbox[0][2] = findViewById(R.id._0_2);
        letterbox[0][3] = findViewById(R.id._0_3);
        letterbox[0][4] = findViewById(R.id._0_4);
        letterbox[1][0] = findViewById(R.id._1_0);
        letterbox[1][1] = findViewById(R.id._1_1);
        letterbox[1][2] = findViewById(R.id._1_2);
        letterbox[1][3] = findViewById(R.id._1_3);
        letterbox[1][4] = findViewById(R.id._1_4);
        letterbox[2][0] = findViewById(R.id._2_0);
        letterbox[2][1] = findViewById(R.id._2_1);
        letterbox[2][2] = findViewById(R.id._2_2);
        letterbox[2][3] = findViewById(R.id._2_3);
        letterbox[2][4] = findViewById(R.id._2_4);
        letterbox[3][0] = findViewById(R.id._3_0);
        letterbox[3][1] = findViewById(R.id._3_1);
        letterbox[3][2] = findViewById(R.id._3_2);
        letterbox[3][3] = findViewById(R.id._3_3);
        letterbox[3][4] = findViewById(R.id._3_4);
        letterbox[4][0] = findViewById(R.id._4_0);
        letterbox[4][1] = findViewById(R.id._4_1);
        letterbox[4][2] = findViewById(R.id._4_2);
        letterbox[4][3] = findViewById(R.id._4_3);
        letterbox[4][4] = findViewById(R.id._4_4);
        letterbox[5][0] = findViewById(R.id._5_0);
        letterbox[5][1] = findViewById(R.id._5_1);
        letterbox[5][2] = findViewById(R.id._5_2);
        letterbox[5][3] = findViewById(R.id._5_3);
        letterbox[5][4] = findViewById(R.id._5_4);

        replayImageView = findViewById(R.id.resetImg);
        backImageView = findViewById(R.id.ivBackIcon);
        homeImageView = findViewById(R.id.ivHomeIcon);
        settingImageView = findViewById(R.id.ivSettingIcon);
        currentStreakTextView = findViewById(R.id.tvStreak);
        streakTooltipTextView = findViewById(R.id.tvStreakTooltip);
        flameLottieAnimationView = findViewById(R.id.lavFlame);
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