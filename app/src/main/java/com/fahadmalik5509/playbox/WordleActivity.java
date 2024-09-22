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

    WordleLayoutBinding wb;

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
        wb = WordleLayoutBinding.inflate(getLayoutInflater());
        setContentView(wb.getRoot());

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
        animateText(wb.currencyCountTV, 0f, 360f, 300);
        playSound(this, R.raw.coin);
        currentCurrencyCount += (50 + (5 * currentStreakCount) + ((6 - currentRow) * 5));
        updateCurrency();
        currentStreakCount++;
        updateStreak();
        new Handler().postDelayed(() -> toggleVisibility(wb.resetIV), 500);
    }

    private void handleLoss() {
        gameLost = true;
        playSound(this, R.raw.draw);
        Toast.makeText(this, "The word was: " + targetWord, Toast.LENGTH_LONG).show();
        toggleVisibility(wb.resetIV);
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
        toggleVisibility(wb.resetIV);
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
        toggleVisibility(wb.shadowV, wb.shopLL);

        wb.shopBombCountTV.setText(String.valueOf(currentBombCount));
        wb.shopHintCountTV.setText(String.valueOf(currentHintCount));
        wb.shopSkipCountTV.setText(String.valueOf(currentSkipCount));
    }
    private void updateCurrency() {

        saveToSharedPreferences(CURRENCY_KEY, currentCurrencyCount);
        wb.currencyCountTV.setText(String.valueOf(currentCurrencyCount));
    }

    // OnClick Method
    public void onStreakClick(View view) {
        if(wb.flameLAV.isAnimating()) playSound(this, R.raw.flamesfx);
        else playSound(this, R.raw.click_ui);
        wb.streakTooltipTV.setText("Streak\n(Current: " + sharedPreferences.getInt(WORDLE_STREAK_KEY, 0) + ")" + "\n(Best: " + sharedPreferences.getInt(WORDLE_HIGHEST_STREAK_KEY, 0) + ")");
        toggleVisibility(wb.streakTooltipTV);
        wb.streakTooltipTV.postDelayed(() -> toggleVisibility(wb.streakTooltipTV), 2000);
    }
    private void updateStreak() {

        int bestStreakCount = sharedPreferences.getInt(WORDLE_HIGHEST_STREAK_KEY, 0);

        saveToSharedPreferences(WORDLE_STREAK_KEY, currentStreakCount);
        wb.currentStreakTV.setText(String.valueOf(currentStreakCount));

        if (currentStreakCount >= bestStreakCount && currentStreakCount != 0) {
            saveToSharedPreferences(WORDLE_HIGHEST_STREAK_KEY, currentStreakCount);
            changeBackgroundColor(wb.currentStreakTV, Color.TRANSPARENT);
            wb.flameLAV.setVisibility(View.VISIBLE);
            wb.flameLAV.playAnimation();
        }
        else {
            wb.currentStreakTV.setBackgroundTintList(null);
            wb.currentStreakTV.setBackgroundResource(R.drawable.circle_background);
            wb.flameLAV.setVisibility(View.GONE);
            wb.flameLAV.cancelAnimation();
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

        playSound(this, R.raw.explosion);
        wb.blastLAV.setVisibility(View.VISIBLE);
        wb.blastLAV.playAnimation();

        currentBombCount--;
        updateBomb();
    }
    private void updateBomb() {
        saveToSharedPreferences(BOMB_KEY, currentBombCount);
        wb.bombTV.setText(String.valueOf(currentBombCount));
        wb.shopBombCountTV.setText(String.valueOf(currentBombCount));
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
        wb.skipLAV.setMinFrame(10);
        wb.skipLAV.playAnimation();
        onResetGameClicked(view);
    }
    private void updateSkip() {
        saveToSharedPreferences(SKIP_KEY, currentSkipCount);
        wb.skipCountTV.setText(String.valueOf(currentSkipCount));
        wb.shopSkipCountTV.setText(String.valueOf(currentSkipCount));
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
        wb.hintCountTV.setText(String.valueOf(currentHintCount));
        wb.shopHintCountTV.setText(String.valueOf(currentHintCount));
    }

    // OnClick Method
    public void handleLeaveButtons(View view) {
        playSound(this, R.raw.click_ui);
        if (view.getTag().equals("leave")) {
            saveToSharedPreferences(WORDLE_STREAK_KEY, 0);
            changeActivity(this, HomeActivity.class, true, false);

        } else {
            toggleVisibility(wb.shadowV, wb.leaveGameRL);
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
            toggleVisibility(wb.shadowV, wb.shopLL);
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
            toggleVisibility(wb.shadowV, wb.leaveGameRL);
        }
        else {
            changeActivity(this, HomeActivity.class, true, false);
        }
    }

    @Override
    public void onBackPressed() {
        vibrate(this, 50);
        if(currentRow>0) {
            toggleVisibility(wb.shadowV, wb.leaveGameRL);
        }
        else {
            changeActivity(this, HomeActivity.class, true, false);
        }
    }

    private void initializeViews() {

        keyboard = new TextView[] {
                wb.aTextView, wb.bTextView, wb.cTextView, wb.dTextView, wb.eTextView, wb.fTextView,
                wb.gTextView, wb.hTextView, wb.iTextView, wb.jTextView, wb.kTextView, wb.lTextView,
                wb.mTextView, wb.nTextView, wb.oTextView, wb.pTextView, wb.qTextView, wb.rTextView,
                wb.sTextView, wb.tTextView, wb.uTextView, wb.vTextView, wb.wTextView, wb.xTextView,
                wb.yTextView, wb.zTextView
        };

        letterBox = new EditText[][] {
                {wb.letterBoxR0C0EV, wb.letterBoxR0C1EV, wb.letterBoxR0C2EV, wb.letterBoxR0C3EV, wb.letterBoxR0C4EV},
                {wb.letterBoxR1C0EV, wb.letterBoxR1C1EV, wb.letterBoxR1C2EV, wb.letterBoxR1C3EV, wb.letterBoxR1C4EV},
                {wb.letterBoxR2C0EV, wb.letterBoxR2C1EV, wb.letterBoxR2C2EV, wb.letterBoxR2C3EV, wb.letterBoxR2C4EV},
                {wb.letterBoxR3C0EV, wb.letterBoxR3C1EV, wb.letterBoxR3C2EV, wb.letterBoxR3C3EV, wb.letterBoxR3C4EV},
                {wb.letterBoxR4C0EV, wb.letterBoxR4C1EV, wb.letterBoxR4C2EV, wb.letterBoxR4C3EV, wb.letterBoxR4C4EV},
                {wb.letterBoxR5C0EV, wb.letterBoxR5C1EV, wb.letterBoxR5C2EV, wb.letterBoxR5C3EV, wb.letterBoxR5C4EV}
        };

        currentStreakCount = sharedPreferences.getInt(WORDLE_STREAK_KEY, 0);
        currentCurrencyCount = sharedPreferences.getInt(CURRENCY_KEY, 300);
        currentBombCount = sharedPreferences.getInt(BOMB_KEY, 10);
        currentSkipCount = sharedPreferences.getInt(SKIP_KEY, 5);
        currentHintCount = sharedPreferences.getInt(HINT_KEY, 10);
    }

    private void animateViewsPulse() {
        for (int i = 0; i < 26; i++) animateViewPulse(this, keyboard[i]);

        animateViewPulse(this, wb.enterIV);
        animateViewPulse(this, wb.backspaceIV);

        animateViewPulse(this, wb.resetIV);
        animateViewPulse(this, wb.homeIconIV);
        animateViewPulse(this, wb.settingIconIV);
        animateViewPulse(this, wb.backIconIV);
        animateViewPulse(this, wb.currentStreakTV);

        animateViewPulse(this, wb.bombRL);
        animateViewPulse(this, wb.skipRL);
        animateViewPulse(this, wb.hintRL);
        animateViewPulse(this, wb.currencyRL);

        animateViewPulse(this, wb.popupLeaveB);
        animateViewPulse(this, wb.popupStayB);

        animateViewPulse(this, wb.shopBombBuyB);
        animateViewPulse(this, wb.shopHintBuyB);
        animateViewPulse(this, wb.shopSkipBuyB);
        animateViewPulse(this, wb.shopCloseBuyB);
    }

    private void cheat() {
        currentCurrencyCount = 99999;
        updateCurrency();
        Toast.makeText(this, targetWord, Toast.LENGTH_SHORT).show();
    }
}