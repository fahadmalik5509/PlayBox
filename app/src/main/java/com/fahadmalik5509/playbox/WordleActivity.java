package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.*;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.airbnb.lottie.LottieAnimationView;

public class WordleActivity extends AppCompatActivity {

    private final int MAX_ROWS = 6;
    private final int MAX_COLS = 5;

    RelativeLayout leaveGameRelativeLayout, currencyRelativeLayout, bombRelativeLayout, hintRelativeLayout, skipRelativeLayout;
    private TextView currentStreakTV, streakTooltipTV, currencyTV, bombTV, hintTV, skipTV, buyBombTV, buyHintTV, buySkipTV;
    private ImageView enterIV, backspaceIV, replayIV, homeIV, settingIV, backIV;
    private final TextView[] keyboard = new TextView[26];
    private final EditText[][] letterBox = new EditText[MAX_ROWS][MAX_COLS];
    Button leaveGameB, stayGameB, buyBombB, buyHintB, buySkipB, buyCloseB;
    View shadowView;
    LinearLayout currencyBuyLinearLayout;
    LottieAnimationView flameAV, blastAV, skipAV;

    private int currentColumn = 0, currentRow = 0, currentStreak, currentCurrency, currentBomb, currentSkip, currentHint, hintUsed = 0;
    private final List < String > commonWords = new ArrayList < > ();
    private final List < String > dictionary = new ArrayList < > ();
    private final List<Character> grayedOutLetters = new ArrayList<>();
    private final Map<Character, Integer> letterColorMap = new HashMap<>();
    private final Map<Integer, Character> revealedHints = new HashMap<>();
    private boolean gameWon = false, gameLost = false;
    private final StringBuilder userGuess = new StringBuilder();
    private String targetWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wordle_layout);

        loadColors(this);
        initializeViews();
        animateViewsPulse();
        loadGameData();
        updateStreak();
        updateCurrency();
        updateBomb();
        updateHint();
        updateSkip();
    }

    private void loadGameData() {
        loadPreference(this);
        loadCommonWords();
        loadDictionary();
        loadRandomTargetWord();
    }

    // OnClick Method
    public void keyboardClicked(View view) {

        if (gameWon || gameLost) return;
        String pressedKey = (String) view.getTag();

        if (pressedKey.equals("enter")) onEnterKeyClicked();
        else if (pressedKey.equals("backspace")) onBackspaceKeyClicked();
        else onLetterKeyClicked(pressedKey);

    }

    private void onLetterKeyClicked(String alphabet) {

        if (currentColumn == MAX_COLS) {
            playSound(this, R.raw.click_error);
            jiggleRow();
            return;
        }

        playSound(this, R.raw.key);
        animateViewBounce(letterBox[currentRow][currentColumn]);
        letterBox[currentRow][currentColumn].setText(alphabet);
        userGuess.insert(currentColumn, alphabet);
        currentColumn++;
    }

    private void onEnterKeyClicked() {

        if(userGuess.toString().equals("FAHAD")) cheat();

        if (!isGuessComplete() || !isValidGuess(userGuess.toString())) {
            playSound(this, R.raw.click_error);
            jiggleRow();
            return;
        }

        playSound(this, R.raw.enter);
        boolean[] letterMatched = new boolean[MAX_COLS];
        int[] targetLetterCount = initializeTargetLetterCount();

        markExactMatches(letterMatched, targetLetterCount);
        markPartialMatches(letterMatched, targetLetterCount);
        applyKeyboardColors();
        handleEndOfTurn();

    }

    private int[] initializeTargetLetterCount() {
        int[] targetLetterCount = new int[26];
        for (char targetChar: targetWord.toCharArray()) {
            targetLetterCount[targetChar - 'A']++;
        }
        return targetLetterCount;
    }

    private void markExactMatches(boolean[] letterMatched, int[] targetLetterCount) {
        for (int i = 0; i < MAX_COLS; i++) {
            char guessChar = userGuess.charAt(i);
            if (targetWord.charAt(i) == guessChar) {
                changeBackgroundColor(letterBox[currentRow][i], GREEN_COLOR);
                updateKeyboardColor(guessChar, GREEN_COLOR);
                letterMatched[i] = true;
                targetLetterCount[guessChar - 'A']--;
            }
        }
    }

    private void markPartialMatches(boolean[] letterMatched, int[] targetLetterCount) {
        for (int i = 0; i < MAX_COLS; i++) {
            if (letterMatched[i]) continue;

            char guessChar = userGuess.charAt(i);
            if (targetLetterCount[guessChar - 'A'] > 0) {
                changeBackgroundColor(letterBox[currentRow][i], YELLOW_COLOR);
                updateKeyboardColor(guessChar, YELLOW_COLOR);
                targetLetterCount[guessChar - 'A']--;
            } else {
                changeBackgroundColor(letterBox[currentRow][i], GRAY_COLOR);
                updateKeyboardColor(guessChar, GRAY_COLOR);
            }
        }
    }

    private void handleEndOfTurn() {
        if (targetWord.equals(userGuess.toString())) {
            handleWin();
        } else {
            currentRow++;
            currentColumn = 0;
            userGuess.setLength(0);
            if (currentRow == MAX_ROWS) {
                handleLoss();
            } else {
                // Apply the stored hints to the new row
                for (Map.Entry<Integer, Character> entry : revealedHints.entrySet()) {
                    int position = entry.getKey();
                    char hintLetter = entry.getValue();
                    letterBox[currentRow][position].setHint(String.valueOf(hintLetter));
                }
            }
        }
    }

    private void handleWin() {
        gameWon = true;
        waveRow();
        animateText(currencyTV, 0f, 360f, 300);
        playSound(this, R.raw.coin);
        currentCurrency += (50 + (5 * currentStreak) + ((6 - currentRow) * 5));
        updateCurrency();
        currentStreak++;
        updateStreak();
        new Handler().postDelayed(() -> replayIV.setVisibility(View.VISIBLE), 500);
    }

    private void handleLoss() {
        gameLost = true;
        playSound(this, R.raw.draw);
        Toast.makeText(this, "The word was: " + targetWord, Toast.LENGTH_LONG).show();
        replayIV.setVisibility(View.VISIBLE);
        currentStreak = 0;
        updateStreak();
    }


    private void updateKeyboardColor(char letter, int color) {
        Integer existingColor = letterColorMap.get(letter);

        // Apply the color based on priority
        if (color == GREEN_COLOR ||
                (color == YELLOW_COLOR && (existingColor == null || existingColor != GREEN_COLOR)) ||
                (color == GRAY_COLOR && (existingColor == null || existingColor != GREEN_COLOR && existingColor != YELLOW_COLOR))) {
            letterColorMap.put(letter, color);

            // Update grayedOutLetters list if the color is gray
            if (color == GRAY_COLOR && !grayedOutLetters.contains(letter)) {
                grayedOutLetters.add(letter);
            }
        }
    }

    private void applyKeyboardColors() {
        for (Map.Entry<Character, Integer> entry : letterColorMap.entrySet()) {
            char letter = entry.getKey();
            int color = entry.getValue();
            int index = letter - 'A';
            changeBackgroundColor(keyboard[index], color);
        }
    }

    private void onBackspaceKeyClicked() {
        if (currentColumn == 0) {
            playSound(this, R.raw.click_error);
            jiggleRow();
            return;
        }

        playSound(this, R.raw.backspace);
        currentColumn--;
        userGuess.deleteCharAt(currentColumn);
        letterBox[currentRow][currentColumn].setText("");
    }

    // OnClick Method
    public void onResetGameClicked(View view) {
        gameWon = false;
        gameLost = false;
        currentRow = 0;
        currentColumn = 0;
        hintUsed = 0;
        userGuess.setLength(0);
        letterColorMap.clear();
        grayedOutLetters.clear();
        revealedHints.clear();
        replayIV.setVisibility(View.GONE);
        loadRandomTargetWord();

        for (int row = 0; row < MAX_ROWS; row++) {
            for (int column = 0; column < 5; column++) {
                letterBox[row][column].setText("");
                letterBox[row][column].setHint("");
                changeBackgroundColor(letterBox[row][column], RED_COLOR);
            }
        }

        for (int i = 0; i < 26; i++) {
            changeBackgroundColor(keyboard[i], BEIGE_COLOR);
        }
    }

    private boolean isGuessComplete() {
        return userGuess.length() == MAX_COLS;
    }

    private boolean isValidGuess(String guess) {
        return dictionary.contains(guess.toUpperCase());
    }

    private void jiggleRow() {
        for (int i = 0; i < MAX_COLS; i++)
            animateViewJiggle(letterBox[currentRow][i], 150);
    }
    private void waveRow() {
        final int duration = 150;
        final int delayBetweenAnimations = 50;

        for (int i = 0; i < MAX_COLS; i++) {
            final int index = i;
            final int startDelay = i * delayBetweenAnimations;

            // Scale down animation
            letterBox[currentRow][i].postDelayed(() -> {
                animateViewScale(letterBox[currentRow][index], 1.0f, 0.7f, duration);
            }, startDelay);

            // Scale up animation
            letterBox[currentRow][i].postDelayed(() -> {
                animateViewScale(letterBox[currentRow][index], 0.7f, 1.0f, duration);
            }, startDelay + duration + delayBetweenAnimations);
        }
    }

    public void loadRandomTargetWord() {

        targetWord = commonWords.get(getRandomNumber(0,commonWords.size() - 1));
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
    public void onCurrencyClick(View view) {
        playSound(this, R.raw.register);
        shadowView.setVisibility(View.VISIBLE);
        currencyBuyLinearLayout.setVisibility(View.VISIBLE);
        buyBombTV.setText(String.valueOf(currentBomb));
        buyHintTV.setText(String.valueOf(currentHint));
        buySkipTV.setText(String.valueOf(currentSkip));
    }
    private void updateCurrency() {

        saveToSharedPreferences(CURRENCY_KEY, currentCurrency);
        currencyTV.setText(String.valueOf(currentCurrency));
    }

    // OnClick Method
    public void onStreakClick(View view) {
        if(flameAV.isAnimating()) playSound(this, R.raw.flamesfx);
        else playSound(this, R.raw.click_ui);
        streakTooltipTV.setText("Streak\n(Current: " + sharedPreferences.getInt(WORDLE_STREAK_KEY, 0) + ")" + "\n(Best: " + sharedPreferences.getInt(WORDLE_HIGHEST_STREAK_KEY, 0) + ")");
        streakTooltipTV.setVisibility(View.VISIBLE);
        streakTooltipTV.postDelayed(() -> streakTooltipTV.setVisibility(View.GONE), 2000);
    }
    private void updateStreak() {

        int highestStreak = sharedPreferences.getInt(WORDLE_HIGHEST_STREAK_KEY, 0);

        saveToSharedPreferences(WORDLE_STREAK_KEY, currentStreak);
        currentStreakTV.setText(String.valueOf(currentStreak));

        if (currentStreak >= highestStreak && currentStreak != 0) {
            saveToSharedPreferences(WORDLE_HIGHEST_STREAK_KEY, currentStreak);
            changeBackgroundColor(currentStreakTV, Color.TRANSPARENT);
            flameAV.setVisibility(View.VISIBLE);
            flameAV.playAnimation();
        }
        else {
            currentStreakTV.setBackgroundTintList(null);
            currentStreakTV.setBackgroundResource(R.drawable.circle_background);
            flameAV.setVisibility(View.GONE);
            flameAV.cancelAnimation();
        }
    }

    // OnClick Method
    public void onBombClick(View view) {
        if (gameWon || gameLost) return;
        if (currentBomb == 0) {
            playSound(this, R.raw.click_error);
            return;
        }

        int lettersToGrayOut = 0;

        for (char letter = 'A'; letter <= 'Z'; letter++) {
            if (!targetWord.contains(String.valueOf(letter)) && !grayedOutLetters.contains(letter)) {
                // Gray out the letter on the keyboard
                int keyboardIndex = letter - 'A';
                changeBackgroundColor(keyboard[keyboardIndex], GRAY_COLOR);
                updateKeyboardColor(letter, GREEN_COLOR);

                // Add the letter to grayed-out list
                grayedOutLetters.add(letter);

                lettersToGrayOut++;
                if (lettersToGrayOut >= 3) break; // Ensure up to 3 letters are selected
            }
        }

        if (lettersToGrayOut == 0) {
            playSound(this, R.raw.click_error);
            return;
        }

        playSound(this, R.raw.explosion);
        blastAV.setVisibility(View.VISIBLE);
        blastAV.playAnimation();
        blastAV.clearAnimation();
        if(currentBomb!=0) currentBomb--;
        updateBomb();
    }
    private void updateBomb() {
        saveToSharedPreferences(BOMB_KEY, currentBomb);
        bombTV.setText(String.valueOf(currentBomb));
        buyBombTV.setText(String.valueOf(currentBomb));
    }

    // OnClick Method
    public void onSkipClick(View view) {
        if (gameWon || gameLost) return;
        if(currentRow == 0 || currentSkip == 0) {
            playSound(this, R.raw.click_error);
            return;
        }
        if(currentSkip != 0) currentSkip--;
        updateSkip();

        playSound(this, R.raw.skip);
        skipAV.setMinFrame(10);
        skipAV.playAnimation();
        onResetGameClicked(view);
    }
    private void updateSkip() {
        saveToSharedPreferences(SKIP_KEY, currentSkip);
        skipTV.setText(String.valueOf(currentSkip));
        buySkipTV.setText(String.valueOf(currentSkip));
    }

    // OnClick Method
    public void onHintClick(View view) {
        int HINTS_PER_ROUND = 2;

        if (gameWon || gameLost || hintUsed >= HINTS_PER_ROUND) {
            playSound(this, R.raw.click_error);
            return;
        }

        List<Integer> unrevealedPositions = new ArrayList<>();

        for (int i = 0; i < MAX_COLS; i++) {
            char targetChar = targetWord.charAt(i);
            Integer keyboardColor = letterColorMap.get(targetChar);

            if (keyboardColor == null || keyboardColor != GREEN_COLOR) {
                unrevealedPositions.add(i);
            }
        }

        if (unrevealedPositions.isEmpty() || currentHint == 0) {
            playSound(this, R.raw.click_error);
            return;
        }

        playSound(this, R.raw.hint);

        int randomIndex = getRandomNumber(0, unrevealedPositions.size() - 1);
        int positionToReveal = unrevealedPositions.get(randomIndex);

        char correctLetter = targetWord.charAt(positionToReveal);
        letterBox[currentRow][positionToReveal].setHint(String.valueOf(correctLetter));
        animateText(letterBox[currentRow][positionToReveal], 0f, 360f, 300);

        // Store the revealed hint
        revealedHints.put(positionToReveal, correctLetter);

        updateKeyboardColor(correctLetter, GREEN_COLOR);
        applyKeyboardColors();

        hintUsed++;
        if(currentHint!=0) currentHint--;
        updateHint();
    }
    private void updateHint() {
        saveToSharedPreferences(HINT_KEY, currentHint);
        hintTV.setText(String.valueOf(currentHint));
        buyHintTV.setText(String.valueOf(currentHint));
    }

    // OnClick Method
    public void handleLeaveButtons(View view) {
        playSound(this, R.raw.click_ui);
        if (view.getTag().equals("leave")) {
            shadowView.setVisibility(View.GONE);
            leaveGameRelativeLayout.setVisibility(View.GONE);
            saveToSharedPreferences(WORDLE_STREAK_KEY, 0);
            changeActivity(this, HomeActivity.class, true, false);

        } else {
            shadowView.setVisibility(View.GONE);
            leaveGameRelativeLayout.setVisibility(View.GONE);
        }
    }

    // OnClick Method
    public void handleBuyButtons(View view) {

        if (view.getTag().equals("bomb")) {
            if (currentCurrency >=40) {
                playSound(this, R.raw.bought);
                currentBomb++;
                currentCurrency -= 40;
                updateBomb();
                updateCurrency();
            }
            else { playSound(this, R.raw.click_error); }
        } else if (view.getTag().equals("hint")) {
            if (currentCurrency >= 100) {
                playSound(this, R.raw.bought);
                currentHint++;
                currentCurrency -= 100;
                updateHint();
                updateCurrency();
            }
            else { playSound(this, R.raw.click_error); }
        } else if (view.getTag().equals("skip")) {
            if (currentCurrency >= 200) {
                playSound(this, R.raw.bought);
                currentSkip++;
                currentCurrency -= 200;
                updateSkip();
                updateCurrency();
            }
            else { playSound(this, R.raw.click_error); }
        } else {
            playSound(this, R.raw.click_ui);
            shadowView.setVisibility(View.GONE);
            currencyBuyLinearLayout.setVisibility(View.GONE);
        }
    }

    // OnClick Method
    public void goToSetting(View view) {
        playSound(this, R.raw.click_ui);
        Intent intent = new Intent(this, SettingActivity.class);
        intent.putExtra("origin_activity", this.getClass().getSimpleName());
        this.startActivity(intent);
    }

    // OnClick Method
    public void goToHome(View view) {
        playSound(this, R.raw.click_ui);
        if(currentRow>0) {
            shadowView.setVisibility(View.VISIBLE);
            leaveGameRelativeLayout.setVisibility(View.VISIBLE);
        }
        else {
            changeActivity(this, HomeActivity.class, true, false);
        }
    }

    @Override
    public void onBackPressed() {
        vibrate(this, 50);
        if(currentRow>0) {
            shadowView.setVisibility(View.VISIBLE);
            leaveGameRelativeLayout.setVisibility(View.VISIBLE);
        }
        else {
            changeActivity(this, HomeActivity.class, true, false);
        }
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
        enterIV = findViewById(R.id.enterImageView);
        backspaceIV = findViewById(R.id.backspaceImageView);

        letterBox[0][0] = findViewById(R.id._0_0);
        letterBox[0][1] = findViewById(R.id._0_1);
        letterBox[0][2] = findViewById(R.id._0_2);
        letterBox[0][3] = findViewById(R.id._0_3);
        letterBox[0][4] = findViewById(R.id._0_4);
        letterBox[1][0] = findViewById(R.id._1_0);
        letterBox[1][1] = findViewById(R.id._1_1);
        letterBox[1][2] = findViewById(R.id._1_2);
        letterBox[1][3] = findViewById(R.id._1_3);
        letterBox[1][4] = findViewById(R.id._1_4);
        letterBox[2][0] = findViewById(R.id._2_0);
        letterBox[2][1] = findViewById(R.id._2_1);
        letterBox[2][2] = findViewById(R.id._2_2);
        letterBox[2][3] = findViewById(R.id._2_3);
        letterBox[2][4] = findViewById(R.id._2_4);
        letterBox[3][0] = findViewById(R.id._3_0);
        letterBox[3][1] = findViewById(R.id._3_1);
        letterBox[3][2] = findViewById(R.id._3_2);
        letterBox[3][3] = findViewById(R.id._3_3);
        letterBox[3][4] = findViewById(R.id._3_4);
        letterBox[4][0] = findViewById(R.id._4_0);
        letterBox[4][1] = findViewById(R.id._4_1);
        letterBox[4][2] = findViewById(R.id._4_2);
        letterBox[4][3] = findViewById(R.id._4_3);
        letterBox[4][4] = findViewById(R.id._4_4);
        letterBox[5][0] = findViewById(R.id._5_0);
        letterBox[5][1] = findViewById(R.id._5_1);
        letterBox[5][2] = findViewById(R.id._5_2);
        letterBox[5][3] = findViewById(R.id._5_3);
        letterBox[5][4] = findViewById(R.id._5_4);

        replayIV = findViewById(R.id.resetImg);
        backIV = findViewById(R.id.ivBackIcon);
        homeIV = findViewById(R.id.ivHomeIcon);
        settingIV = findViewById(R.id.ivSettingIcon);
        currentStreakTV = findViewById(R.id.tvStreak);
        streakTooltipTV = findViewById(R.id.tvStreakTooltip);
        currencyTV = findViewById(R.id.tvCurrency);

        flameAV = findViewById(R.id.lavFlame);
        blastAV = findViewById(R.id.lavBlast);
        skipAV = findViewById(R.id.lavSkip);

        bombRelativeLayout = findViewById(R.id.rlBomb);
        hintRelativeLayout = findViewById(R.id.rlHint);
        skipRelativeLayout = findViewById(R.id.rlSkip);
        currencyRelativeLayout = findViewById(R.id.rlCurrency);

        bombTV = findViewById(R.id.tvBomb);
        hintTV = findViewById(R.id.tvHint);
        skipTV = findViewById(R.id.tvSkip);
        buyBombTV = findViewById(R.id.tvBuyBomb);
        buyHintTV = findViewById(R.id.tvBuyHint);
        buySkipTV = findViewById(R.id.tvBuySkip);


        leaveGameRelativeLayout = findViewById(R.id.rlLeaveGame);
        leaveGameB = findViewById(R.id.bGameLeave);
        stayGameB = findViewById(R.id.bGameStay);

        shadowView = findViewById(R.id.vShadow);

        currentStreak = sharedPreferences.getInt(WORDLE_STREAK_KEY, 0);
        currentCurrency = sharedPreferences.getInt(CURRENCY_KEY, 300);
        currentBomb = sharedPreferences.getInt(BOMB_KEY, 10);
        currentSkip = sharedPreferences.getInt(SKIP_KEY, 5);
        currentHint = sharedPreferences.getInt(HINT_KEY, 10);

        buyBombB = findViewById(R.id.bBuyBomb);
        buyHintB = findViewById(R.id.bBuyHint);
        buySkipB = findViewById(R.id.bBuySkip);
        buyCloseB = findViewById(R.id.bBuyClose);

        currencyBuyLinearLayout = findViewById(R.id.llCurrencyBuy);
    }

    private void animateViewsPulse() {
        for (int i = 0; i < 26; i++) animateViewPulse(this, keyboard[i]);

        animateViewPulse(this, enterIV);
        animateViewPulse(this, backspaceIV);

        animateViewPulse(this, replayIV);
        animateViewPulse(this, homeIV);
        animateViewPulse(this, settingIV);
        animateViewPulse(this, backIV);
        animateViewPulse(this, currentStreakTV);

        animateViewPulse(this, bombRelativeLayout);
        animateViewPulse(this, skipRelativeLayout);
        animateViewPulse(this, hintRelativeLayout);
        animateViewPulse(this, currencyRelativeLayout);

        animateViewPulse(this, leaveGameB);
        animateViewPulse(this, stayGameB);

        animateViewPulse(this, buyBombB);
        animateViewPulse(this, buyHintB);
        animateViewPulse(this, buySkipB);
        animateViewPulse(this, buyCloseB);
    }

    private void cheat() {
        //currentCurrency = 99999;
        //updateCurrency();
        Toast.makeText(this, targetWord, Toast.LENGTH_SHORT).show();
    }
}