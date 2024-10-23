package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.*;
import com.fahadmalik5509.playbox.databinding.WordleLayoutBinding;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
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

public class WordleActivity extends AppCompatActivity {

    WordleLayoutBinding vb;

    private final int MAX_ROWS = 6;
    private final int MAX_COLS = 5;

    private TextView[] keyboard;
    private EditText[][] letterBox;

    private int currentColumn = 0, currentRow = 0, hintUsed = 0, currentStreakCount,
            currentCurrencyCount, currentBombCount, currentSkipCount, currentHintCount;
    private final List < String > commonWords = new ArrayList <> ();
    private final List < String > dictionary = new ArrayList <> ();
    private final List<Character> grayedOutLetters = new ArrayList<>();
    private final Map<Character, Integer> letterColorMap = new HashMap<>();
    private final Map<Integer, Character> revealedHints = new HashMap<>();
    private boolean gameWon = false, gameLost = false;
    private final StringBuilder userGuess = new StringBuilder();
    private String targetWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = WordleLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        initializeViews();
        animateViewsPulse();
        loadGameData();
        updateUI();
    }

    private void loadGameData() {
        loadColors(this);
        loadPreference(this);
        loadCommonWords();
        loadDictionary();
        loadRandomTargetWord();
    }
    private void updateUI() {
        updateStreak();
        updateCurrency();
        updateBomb();
        updateHint();
        updateSkip();
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
        if(userGuess.toString().equals("NIGGA"))  unCheat();

        if (!isValidGuess()) {
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
        if (targetWord.equals(userGuess.toString())) handleWin();
        else {
            prepareNextTurn();
            if (currentRow == MAX_ROWS) handleLoss();
            else applyStoredHintsToNewRow();
        }
    }

    private void prepareNextTurn() {
        currentRow++;
        currentColumn = 0;
        userGuess.setLength(0);
    }

    private void applyStoredHintsToNewRow() {
        for (Map.Entry<Integer, Character> entry : revealedHints.entrySet()) {
            int position = entry.getKey();
            char hintLetter = entry.getValue();
            letterBox[currentRow][position].setHint(String.valueOf(hintLetter));
        }
    }

    private void handleWin() {
        gameWon = true;
        waveRow();
        animateText(vb.currencyCountTV, 0f, 360f, 300);
        vb.coinblastLAV.setVisibility(View.VISIBLE);
        vb.coinblastLAV.playAnimation();
        playSound(this, R.raw.coin);
        currentCurrencyCount += (50 + (5 * currentStreakCount) + ((6 - currentRow) * 5));
        updateCurrency();
        currentStreakCount++;
        updateStreak();
        new Handler().postDelayed(() -> toggleVisibility(true, vb.resetIV), 500);
    }

    private void handleLoss() {
        gameLost = true;
        playSound(this, R.raw.draw);
        Toast.makeText(this, "The word was: " + targetWord, Toast.LENGTH_LONG).show();
        toggleVisibility(true, vb.resetIV);
        currentStreakCount = 0;
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

    // OnClick Method
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
        vb.resetIV.setVisibility(View.GONE);
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

    private boolean isValidGuess() {
        return  userGuess.length() == MAX_COLS && dictionary.contains(userGuess.toString().toUpperCase());
    }

    private void jiggleRow() {
        for (int i = 0; i < MAX_COLS; i++) animateViewJiggle(letterBox[currentRow][i], 150);
    }
    private void waveRow() {
        final int duration = 150;
        final int delayBetweenAnimations = 50;

        for (int i = 0; i < MAX_COLS; i++) {
            final int index = i;
            final int startDelay = i * delayBetweenAnimations;

            // Scale down animation
            letterBox[currentRow][i].postDelayed(() -> animateViewScale(letterBox[currentRow][index], 1.0f, 0.7f, duration), startDelay);

            // Scale up animation
            letterBox[currentRow][i].postDelayed(() -> animateViewScale(letterBox[currentRow][index], 0.7f, 1.0f, duration), startDelay + duration + delayBetweenAnimations);
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
        animateViewScale(vb.shopLL,0f,1.0f,200);
        toggleVisibility(true, vb.shadowV, vb.shopLL);

        vb.shopBombCountTV.setText(String.valueOf(currentBombCount));
        vb.shopHintCountTV.setText(String.valueOf(currentHintCount));
        vb.shopSkipCountTV.setText(String.valueOf(currentSkipCount));
    }
    private void updateCurrency() {

        saveToSharedPreferences(CURRENCY_KEY, currentCurrencyCount);
        vb.currencyCountTV.setText(String.valueOf(currentCurrencyCount));
    }

    // OnClick Method
    public void onStreakClick(View view) {
        if(vb.flameLAV.isAnimating()) playSound(this, R.raw.flamesfx);
        else playSound(this, R.raw.click_ui);
        vb.streakTooltipTV.setText(getString(R.string.streak_tooltip, sharedPreferences.getInt(WORDLE_STREAK_KEY, 0), sharedPreferences.getInt(WORDLE_HIGHEST_STREAK_KEY, 0)));
        toggleVisibility(true, vb.streakTooltipTV);
        vb.streakTooltipTV.postDelayed(() -> toggleVisibility(false, vb.streakTooltipTV), 2000);
    }
    private void updateStreak() {

        int bestStreakCount = sharedPreferences.getInt(WORDLE_HIGHEST_STREAK_KEY, 0);

        saveToSharedPreferences(WORDLE_STREAK_KEY, currentStreakCount);
        vb.currentStreakTV.setText(String.valueOf(currentStreakCount));

        if (currentStreakCount >= bestStreakCount && currentStreakCount != 0) {
            saveToSharedPreferences(WORDLE_HIGHEST_STREAK_KEY, currentStreakCount);
            changeBackgroundColor(vb.currentStreakTV, Color.TRANSPARENT);
            vb.flameLAV.setVisibility(View.VISIBLE);
            vb.flameLAV.playAnimation();
        }
        else {
            vb.currentStreakTV.setBackgroundTintList(null);
            vb.currentStreakTV.setBackgroundResource(R.drawable.circle_background);
            vb.flameLAV.setVisibility(View.GONE);
            vb.flameLAV.cancelAnimation();
        }
    }

    // OnClick Method
    public void onBombClick(View view) {
        if (gameWon || gameLost || currentBombCount == 0) {
            playSound(this, R.raw.click_error);
            return;
        }

        int lettersToGrayOut = 0;

        for (char letter = 'A'; letter <= 'Z'; letter++) {
            if (!targetWord.contains(String.valueOf(letter)) && !grayedOutLetters.contains(letter)) {
                // Gray out the letter on the keyboard
                int keyboardIndex = letter - 'A';
                changeBackgroundColor(keyboard[keyboardIndex], GRAY_COLOR);
                updateKeyboardColor(letter, GRAY_COLOR);

                // Add the letter to grayed-out list
                grayedOutLetters.add(letter);

                lettersToGrayOut++;
                if (lettersToGrayOut >= 3) break;
            }
        }

        if (lettersToGrayOut == 0) {
            playSound(this, R.raw.click_error);
            return;
        }

        if(sharedPreferences.getInt(EXPLOSION_KEY, 0) == 1)  playSound(this, R.raw.explosion2);
        else playSound(this, R.raw.explosion);

        vb.blastLAV.setVisibility(View.VISIBLE);
        vb.blastLAV.playAnimation();

        currentBombCount--;
        updateBomb();
    }
    private void updateBomb() {
        saveToSharedPreferences(BOMB_KEY, currentBombCount);
        vb.bombTV.setText(String.valueOf(currentBombCount));
        vb.shopBombCountTV.setText(String.valueOf(currentBombCount));
    }

    // OnClick Method
    public void onSkipClick(View view) {
        if (gameWon || gameLost || currentRow == 0 || currentSkipCount == 0) {
            playSound(this, R.raw.click_error);
            return;
        }

        currentSkipCount--;
        updateSkip();

        playSound(this, R.raw.skip);
        vb.skipLAV.setMinFrame(10);
        vb.skipLAV.playAnimation();
        onResetGameClicked(view);
    }
    private void updateSkip() {
        saveToSharedPreferences(SKIP_KEY, currentSkipCount);
        vb.skipCountTV.setText(String.valueOf(currentSkipCount));
        vb.shopSkipCountTV.setText(String.valueOf(currentSkipCount));
    }

    // OnClick Method
    public void onHintClick(View view) {
        if (gameWon || gameLost || hintUsed >= 2 || currentHintCount == 0) {
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

        if (unrevealedPositions.isEmpty()) {
            playSound(this, R.raw.click_error);
            return;
        }

        int randomIndex = getRandomNumber(0, unrevealedPositions.size() - 1);
        int positionToReveal = unrevealedPositions.get(randomIndex);

        char correctLetter = targetWord.charAt(positionToReveal);
        letterBox[currentRow][positionToReveal].setHint(String.valueOf(correctLetter));
        animateText(letterBox[currentRow][positionToReveal], 0f, 360f, 300);

        // Store the revealed hint
        revealedHints.put(positionToReveal, correctLetter);

        playSound(this, R.raw.hint);
        updateKeyboardColor(correctLetter, GREEN_COLOR);
        applyKeyboardColors();

        hintUsed++;
        currentHintCount--;
        updateHint();
    }
    private void updateHint() {
        saveToSharedPreferences(HINT_KEY, currentHintCount);
        vb.hintCountTV.setText(String.valueOf(currentHintCount));
        vb.shopHintCountTV.setText(String.valueOf(currentHintCount));
    }

    // OnClick Method
    public void handleLeaveButtons(View view) {
        playSound(this, R.raw.click_ui);
        if (view.getTag().equals("leave")) {
            saveToSharedPreferences(WORDLE_STREAK_KEY, 0);
            changeActivity(this, HomeActivity.class, true, false);

        } else {
            toggleVisibility(false, vb.shadowV, vb.leaveGameRL);
        }
    }

    // OnClick Method
    public void handleStopButtons(View view) {

        if (view.getTag().equals("bomb")) {
            if (currentCurrencyCount >=40) {
                playSound(this, R.raw.bought);
                currentBombCount++;
                currentCurrencyCount -= 40;
                updateBomb();
                updateCurrency();
            }
            else { playSound(this, R.raw.click_error); }
        } else if (view.getTag().equals("hint")) {
            if (currentCurrencyCount >= 100) {
                playSound(this, R.raw.bought);
                currentHintCount++;
                currentCurrencyCount -= 100;
                updateHint();
                updateCurrency();
            }
            else { playSound(this, R.raw.click_error); }
        } else if (view.getTag().equals("skip")) {
            if (currentCurrencyCount >= 200) {
                playSound(this, R.raw.bought);
                currentSkipCount++;
                currentCurrencyCount -= 200;
                updateSkip();
                updateCurrency();
            }
            else { playSound(this, R.raw.click_error); }
        } else {
            playSound(this, R.raw.click_ui);
            toggleVisibility(false, vb.shadowV, vb.shopLL);
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
            toggleVisibility(true, vb.shadowV, vb.leaveGameRL);
        }
        else {
            changeActivity(this, HomeActivity.class, true, false);
        }
    }

    @Override
    public void onBackPressed() {
        vibrate(this, 50);
        if(currentRow>0) {
            toggleVisibility(true, vb.shadowV, vb.leaveGameRL);
        }
        else {
            changeActivity(this, HomeActivity.class, true, false);
        }
    }

    private void initializeViews() {

        keyboard = new TextView[] {
                vb.aTextView, vb.bTextView, vb.cTextView, vb.dTextView, vb.eTextView, vb.fTextView,
                vb.gTextView, vb.hTextView, vb.iTextView, vb.jTextView, vb.kTextView, vb.lTextView,
                vb.mTextView, vb.nTextView, vb.oTextView, vb.pTextView, vb.qTextView, vb.rTextView,
                vb.sTextView, vb.tTextView, vb.uTextView, vb.vTextView, vb.wTextView, vb.xTextView,
                vb.yTextView, vb.zTextView
        };

        letterBox = new EditText[][] {
                {vb.letterBoxR0C0EV, vb.letterBoxR0C1EV, vb.letterBoxR0C2EV, vb.letterBoxR0C3EV, vb.letterBoxR0C4EV},
                {vb.letterBoxR1C0EV, vb.letterBoxR1C1EV, vb.letterBoxR1C2EV, vb.letterBoxR1C3EV, vb.letterBoxR1C4EV},
                {vb.letterBoxR2C0EV, vb.letterBoxR2C1EV, vb.letterBoxR2C2EV, vb.letterBoxR2C3EV, vb.letterBoxR2C4EV},
                {vb.letterBoxR3C0EV, vb.letterBoxR3C1EV, vb.letterBoxR3C2EV, vb.letterBoxR3C3EV, vb.letterBoxR3C4EV},
                {vb.letterBoxR4C0EV, vb.letterBoxR4C1EV, vb.letterBoxR4C2EV, vb.letterBoxR4C3EV, vb.letterBoxR4C4EV},
                {vb.letterBoxR5C0EV, vb.letterBoxR5C1EV, vb.letterBoxR5C2EV, vb.letterBoxR5C3EV, vb.letterBoxR5C4EV}
        };

        currentStreakCount = sharedPreferences.getInt(WORDLE_STREAK_KEY, 0);
        currentCurrencyCount = sharedPreferences.getInt(CURRENCY_KEY, 300);
        currentBombCount = sharedPreferences.getInt(BOMB_KEY, 10);
        currentSkipCount = sharedPreferences.getInt(SKIP_KEY, 5);
        currentHintCount = sharedPreferences.getInt(HINT_KEY, 10);
    }

    private void animateViewsPulse() {
        for (int i = 0; i < 26; i++) animateViewPulse(this, keyboard[i]);

        animateViewPulse(this, vb.enterIV);
        animateViewPulse(this, vb.backspaceIV);

        animateViewPulse(this, vb.resetIV);
        animateViewPulse(this, vb.homeIconIV);
        animateViewPulse(this, vb.settingIconIV);
        animateViewPulse(this, vb.backIconIV);
        animateViewPulse(this, vb.currentStreakTV);

        animateViewPulse(this, vb.bombRL);
        animateViewPulse(this, vb.skipRL);
        animateViewPulse(this, vb.hintRL);
        animateViewPulse(this, vb.currencyRL);

        animateViewPulse(this, vb.popupLeaveB);
        animateViewPulse(this, vb.popupStayB);

        animateViewPulse(this, vb.shopBombBuyB);
        animateViewPulse(this, vb.shopHintBuyB);
        animateViewPulse(this, vb.shopSkipBuyB);
        animateViewPulse(this, vb.shopCloseBuyB);
    }

    private void cheat() {
        currentCurrencyCount = 99999;
        updateCurrency();
        saveToSharedPreferences(EXPLOSION_KEY, 1);
        Toast.makeText(this, targetWord, Toast.LENGTH_SHORT).show();
    }

    private  void unCheat() {
        saveToSharedPreferences(EXPLOSION_KEY, 0);
        currentCurrencyCount = 300;
        updateCurrency();
        Toast.makeText(this, "Cheat Disabled Nigga", Toast.LENGTH_SHORT).show();
    }
}