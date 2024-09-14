package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.*;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
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

    private TextView enterKey, backspaceKey,streakTextView;
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
    private int streak;
    SharedPreferences sharedPreferences;

    private int revealGuessWord = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wordle_layout);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initializeViews();
        animateViewsPulse();
        loadCommonWords();
        loadDictionary();
        loadARandomCommonWord();

        for(int i = 0; i < COLS; i++) {
            setBackgroundDrawable(letterbox[currentRow][i],R.drawable.defaultselected_letterbox);
        }

        streakTextView.setText(String.valueOf(sharedPreferences.getInt(WORDLE_STREAK, 0)));
    }

    // OnClick Method
    public void keyboardClicked(View view) {

        if(gameWon || gameLost) return;
        String pressedKey = (String) view.getTag();

        if (pressedKey.equals("enter")) enterClicked();
        else if (pressedKey.equals("backspace")) backspaceClicked();
        else alphabetClicked(pressedKey);
    }

    private void alphabetClicked(String alphabet) {
        playSound(this,R.raw.key);
        if(currentColumn == COLS) { 
            jiggleRow();
            return; 
        }
        animateViewBounce(letterbox[currentRow][currentColumn]);
        letterbox[currentRow][currentColumn].setText(alphabet);
        userGuess.insert(currentColumn,alphabet);
        currentColumn++;
    }

    private void enterClicked() {
        playSound(this,R.raw.enter);
        revealGuessWord++;
        if(revealGuessWord >= 15) {
            Toast.makeText(this, targetWord, Toast.LENGTH_SHORT).show();
            revealGuessWord = 0;
        }

        if (!isGuessComplete() || !isValidGuess(userGuess.toString())) {
            jiggleRow();
            return;
        }

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
                setBackgroundDrawable(letterbox[currentRow][i], R.drawable.green_letterbox);
                letterMatched[i] = true;
                targetLetterCount[guessChar - 'A']--;
                updateKeyboardColor(guessChar, R.drawable.green_letterbox);
            }
        }
    }

    private void markPartialMatches(boolean[] letterMatched, int[] targetLetterCount) {
        for (int i = 0; i < COLS; i++) {
            if (letterMatched[i]) continue;

            char guessChar = userGuess.charAt(i);
            if (targetLetterCount[guessChar - 'A'] > 0) {
                setBackgroundDrawable(letterbox[currentRow][i], R.drawable.yellow_letterbox);
                targetLetterCount[guessChar - 'A']--;
                updateKeyboardColor(guessChar, R.drawable.yellow_letterbox);
            } else {
                setBackgroundDrawable(letterbox[currentRow][i], R.drawable.gray_letterbox);
                updateKeyboardColor(guessChar, R.drawable.gray_letterbox);
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
            editor.putInt(WORDLE_STREAK, ++streak);
            editor.apply();

            streakTextView.setText(String.valueOf(sharedPreferences.getInt(WORDLE_STREAK, 0)));
            gameWon = true;
            playSound(this, R.raw.win);
            replayImageView.setVisibility(View.VISIBLE);
        } else {
            currentRow++;
            for(int i = 0; i < COLS; i++) {
                if (currentRow < ROWS)
                    setBackgroundDrawable(letterbox[currentRow][i],R.drawable.defaultselected_letterbox);
            }
            currentColumn = 0;
            userGuess.setLength(0);
        }

        if (currentRow == ROWS) {
            gameLost = true;
            playSound(this, R.raw.draw);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(WORDLE_STREAK, 0);
            editor.apply();

            streakTextView.setText("0");
            Toast.makeText(this, "The word was: " + targetWord, Toast.LENGTH_LONG).show();
            replayImageView.setVisibility(View.VISIBLE);
        }
    }

    private void updateKeyboardColor(char letter, int newDrawableResId) {
        int index = letter - 'A';

        if (newDrawableResId == R.drawable.green_letterbox) {
            setBackgroundDrawable(keyboard[index], R.drawable.green_letterbox);
        } else if (newDrawableResId == R.drawable.yellow_letterbox &&
                !isKeyboardKeyGreen(index)) {
            setBackgroundDrawable(keyboard[index], R.drawable.yellow_letterbox);
        } else if (newDrawableResId ==R.drawable.gray_letterbox &&
                !isKeyboardKeyGreen(index) && !isKeyboardKeyYellow(index)) {
            setBackgroundDrawable(keyboard[index], R.drawable.gray_letterbox);
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
                setBackgroundDrawable(letterbox[row][column],R.drawable.default_letterbox);
            }
        }

        for(int i = 0; i < 26; i++) {
            setBackgroundDrawable(keyboard[i],R.drawable.keyboard_border);
        }

        for(int i = 0; i < COLS; i++) {
            setBackgroundDrawable(letterbox[currentRow][i],R.drawable.defaultselected_letterbox);
        }
    }

    private boolean isGuessComplete() {
        return userGuess.length() == COLS;
    }

    private boolean isValidGuess(String guess) {
        return dictionary.contains(guess.toUpperCase());
    }

    private void jiggleRow() { for (int i = 0; i < COLS; i++) animateViewJiggle(letterbox[currentRow][i]); }

    private void setBackgroundDrawable(TextView textView, int drawableResId) { textView.setBackgroundResource(drawableResId); }

    private boolean isKeyboardKeyGreen(int index) { return keyboard[index].getBackground().getConstantState().equals(getDrawable(R.drawable.green_letterbox).getConstantState()); }

    private boolean isKeyboardKeyYellow(int index) { return keyboard[index].getBackground().getConstantState().equals(getDrawable(R.drawable.yellow_letterbox).getConstantState()); }

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
        streakTextView = findViewById(R.id.streak);
    }

    private void animateViewsPulse() {
        for(int i=0; i<26; i++) animateViewPulse(this,keyboard[i]);

        animateViewPulse(this,enterKey);
        animateViewPulse(this,backspaceKey);

        animateViewPulse(this,replayImageView);
        animateViewPulse(this,homeImageView);
        animateViewPulse(this,settingImageView);
        animateViewPulse(this,backImageView);
    }

}