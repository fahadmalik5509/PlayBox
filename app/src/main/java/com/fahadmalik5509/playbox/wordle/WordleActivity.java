package com.fahadmalik5509.playbox.wordle;

import static android.view.View.VISIBLE;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.*;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.core.text.HtmlCompat;

import com.fahadmalik5509.playbox.databinding.NavigationLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShadowLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShopButtonLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShopLayoutBinding;
import com.fahadmalik5509.playbox.miscellaneous.BaseActivity;
import com.fahadmalik5509.playbox.miscellaneous.GamesActivity;
import com.fahadmalik5509.playbox.miscellaneous.HomeActivity;
import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.databinding.WordleLayoutBinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WordleActivity extends BaseActivity implements BaseActivity.ShopUpdateListener {

    // === UI and Binding ===
    WordleLayoutBinding vb;
    private TextView[] keyboard;
    private EditText[][] letterBox;

    // === Game Constants ===
    private static final byte MAX_ROWS = 6;
    private static final byte MAX_COLS = 5;

    // === Game State Variables ===
    private byte currentRow = 0, currentColumn = 0, hintUsed = 0;
    private int streakCount;
    private final StringBuilder userGuess = new StringBuilder();
    private String targetWord;

    private List < String > commonWords = new ArrayList <>();
    private List < String > dictionary = new ArrayList <>();
    private final List<Character> grayedOutLetters = new ArrayList<>();
    private final Map<Character, Integer> letterColorMap = new HashMap<>();
    private final Map<Integer, Character> revealedHints = new HashMap<>();
    private boolean gameWon = false, gameLost = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = WordleLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        setupOnBackPressed();
        getBindings();
        initialize();
        loadGameData();
        updateUI();
    }

    // === Setup Methods ===
    private void getBindings() {
        ShopButtonLayoutBinding ShopButtonBinding = ShopButtonLayoutBinding.bind(vb.ShopButton.getRoot());
        ShopLayoutBinding ShopBinding = ShopLayoutBinding.bind(vb.Shop.getRoot());
        NavigationLayoutBinding NavigationBinding = NavigationLayoutBinding.bind(vb.Navigation.getRoot());
        ShadowLayoutBinding ShadowBinding = ShadowLayoutBinding.bind(vb.Shadow.getRoot());
        setBindings(ShopButtonBinding, ShopBinding, NavigationBinding, ShadowBinding);
    }

    private void initialize() {

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

        streakCount = sharedPreferences.getInt(WORDLE_STREAK_KEY, 0);
    }
    private void loadGameData() {
        dictionary = loadWordList(R.raw.dictionary);
        commonWords = loadWordList(R.raw.commonwords);
        loadRandomTargetWord();
    }

    // === UI Update ===
    @Override
    public void onShopClosed() {
        vb.bombCountTV.setText(String.valueOf(bombCount));
        vb.hintCountTV.setText(String.valueOf(hintCount));
        vb.skipCountTV.setText(String.valueOf(skipCount));
    }
    private void updateUI() {
        updateStreak();
        updateCount(bombCount, vb.bombCountTV);
        updateCount(hintCount, vb.hintCountTV);
        updateCount(skipCount, vb.skipCountTV);
    }

    public void keyboardClicked(View view) {

        if (gameWon || gameLost) return;
        String pressedKey = (String) view.getTag();

        if (pressedKey.equals("enter")) onEnterKeyClicked();
        else if (pressedKey.equals("backspace")) onBackspaceKeyClicked();
        else onLetterKeyClicked(pressedKey);

    }

    private void onLetterKeyClicked(String alphabet) {

        if (currentColumn == MAX_COLS) {
            playSoundAndVibrate(this, R.raw.sound_error, false, 0);
            animateJiggleEditTextsRow();
            return;
        }

        playSoundAndVibrate(this, R.raw.sound_key, true, 50);
        animateViewPulse(letterBox[currentRow][currentColumn], 1f, 1.2f, 100);
        letterBox[currentRow][currentColumn].setText(alphabet);
        userGuess.insert(currentColumn, alphabet);
        currentColumn++;
    }

    private void onEnterKeyClicked() {

        if(userGuess.toString().equals("FAHAD")) cheat();
        if(userGuess.toString().equals("NIGGA")) unCheat();

        if (!isValidGuess()) {
            playSoundAndVibrate(this, R.raw.sound_error, false, 0);
            animateJiggleEditTextsRow();
            return;
        }

        playSoundAndVibrate(this, R.raw.sound_enter, true, 50);
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
                changeBackgroundColor(letterBox[currentRow][i], LIGHT_GREEN_COLOR);
                updateKeyboardColor(guessChar, LIGHT_GREEN_COLOR);
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
        animateWaveEditTextsRow();
        animateText(vb.ShopButton.currencyCountTV, 0f, 360f, 300);
        vb.coinBlastLAV.setVisibility(VISIBLE);
        vb.coinBlastLAV.playAnimation();
        playSoundAndVibrate(this, R.raw.sound_coin, false, 0);
        increaseAndSaveCurrencyCount((50 + (5 * streakCount) + ((MAX_ROWS - currentRow) * 5)));
        streakCount++;
        updateStreak();
        vb.resetIV.postDelayed(() -> toggleVisibility(true, vb.resetIV), 500);
    }

    private void handleLoss() {
        gameLost = true;
        playSoundAndVibrate(this, R.raw.sound_draw, true, 100);
        displayTargetWord();
        toggleVisibility(true, vb.resetIV);
        streakCount = 0;
        updateStreak();
    }

    private void updateKeyboardColor(char letter, int color) {
        Integer existingColor = letterColorMap.get(letter);

        // Apply the color based on priority
        if (color == LIGHT_GREEN_COLOR ||
                (color == YELLOW_COLOR && (existingColor == null || existingColor != LIGHT_GREEN_COLOR)) ||
                (color == GRAY_COLOR && (existingColor == null || existingColor != LIGHT_GREEN_COLOR && existingColor != YELLOW_COLOR))) {
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
            playSoundAndVibrate(this, R.raw.sound_error, false, 0);
            animateJiggleEditTextsRow();
            return;
        }

        playSoundAndVibrate(this, R.raw.sound_backspace, true, 50);
        currentColumn--;
        userGuess.deleteCharAt(currentColumn);
        letterBox[currentRow][currentColumn].setText("");
    }

    // OnClick Method
    public void handleResetClick(View view) {
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
            for (int column = 0; column < MAX_COLS; column++) {
                letterBox[row][column].setText("");
                letterBox[row][column].setHint("");
                changeBackgroundColor(letterBox[row][column], DARK_RED_COLOR);
            }
        }

        for (int i = 0; i < 26; i++) {
            changeBackgroundColor(keyboard[i], BEIGE_COLOR);
        }
    }

    private boolean isValidGuess() {
        return  userGuess.length() == MAX_COLS && dictionary.contains(userGuess.toString().toUpperCase());
    }

    public void loadRandomTargetWord() {

        targetWord = commonWords.get(new Random().nextInt(commonWords.size()));
    }

    private List<String> loadWordList(int resourceId) {
        List<String> words = new ArrayList<>();
        InputStream inputStream = getResources().openRawResource(resourceId);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                words.add(line.toUpperCase().trim());
            }
        } catch (IOException e) {
            Log.e("WordleActivity", "Error reading word list resource: " + resourceId, e);
            // Optional user feedback:
            runOnUiThread(() -> Toast.makeText(this,
                    "Game data load failed!",
                    Toast.LENGTH_SHORT).show());
        }
        return words;
    }

    // OnClick Method
    public void onStreakClick(View view) {
        if(vb.flameLAV.isAnimating()) playSoundAndVibrate(this, R.raw.sound_flame, true, 50);
        else playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        vb.streakTooltipTV.setText(getString(R.string.streak_tooltip, sharedPreferences.getInt(WORDLE_STREAK_KEY, 0), sharedPreferences.getInt(WORDLE_HIGHEST_STREAK_KEY, 0)));
        toggleVisibility(true, vb.streakTooltipTV);
        vb.streakTooltipTV.postDelayed(() -> toggleVisibility(false, vb.streakTooltipTV), 2000);
    }
    private void updateStreak() {

        int bestStreakCount = sharedPreferences.getInt(WORDLE_HIGHEST_STREAK_KEY, 0);

        saveToSharedPreferences(WORDLE_STREAK_KEY, streakCount);
        vb.currentStreakTV.setText(String.valueOf(streakCount));

        if (streakCount >= bestStreakCount && streakCount != 0) {
            saveToSharedPreferences(WORDLE_HIGHEST_STREAK_KEY, streakCount);
            changeBackgroundColor(vb.currentStreakTV, Color.TRANSPARENT);
            vb.flameLAV.setVisibility(VISIBLE);
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
        if (gameWon || gameLost || bombCount == 0) {
            playSoundAndVibrate(this, R.raw.sound_error, false, 0);
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
            playSoundAndVibrate(this, R.raw.sound_error, true, 50);
            return;
        }

        if(sharedPreferences.getInt(WORDLE_EXPLOSION_KEY, 0) == 1)  playSoundAndVibrate(this, R.raw.sound_explosion2, true, 50);
        else playSoundAndVibrate(this, R.raw.sound_explosion, true, 50);

        vb.blastLAV.setVisibility(VISIBLE);
        vb.blastLAV.playAnimation();


        decreaseAndSaveBombCount();
        updateCount(bombCount, vb.bombCountTV);
    }

    // OnClick Method
    public void onSkipClick(View view) {
        if (gameWon || gameLost || currentRow == 0 || skipCount == 0) {
            playSoundAndVibrate(this, R.raw.sound_error, true, 50);
            return;
        }

        animateFallingEditTexts();

        displayTargetWord();
        decreaseAndSaveSkipCount();
        updateCount(skipCount, vb.skipCountTV);

        playSoundAndVibrate(this, R.raw.sound_skip, true, 50);
        view.postDelayed(() -> handleResetClick(view), 300);
    }

    // OnClick Method
    public void onHintClick(View view) {
        if (gameWon || gameLost || hintUsed >= 2 || hintCount == 0) {
            playSoundAndVibrate(this, R.raw.sound_error, true, 50);
            return;
        }

        List<Integer> unrevealedPositions = new ArrayList<>();

        for (int i = 0; i < MAX_COLS; i++) {
            char targetChar = targetWord.charAt(i);
            Integer keyboardColor = letterColorMap.get(targetChar);

            if (keyboardColor == null || keyboardColor != LIGHT_GREEN_COLOR) {
                unrevealedPositions.add(i);
            }
        }

        if (unrevealedPositions.isEmpty()) {
            playSoundAndVibrate(this, R.raw.sound_error, true, 50);
            return;
        }

        int randomIndex = new Random().nextInt(unrevealedPositions.size());
        int positionToReveal = unrevealedPositions.get(randomIndex);

        char correctLetter = targetWord.charAt(positionToReveal);
        letterBox[currentRow][positionToReveal].setHint(String.valueOf(correctLetter));
        animateText(letterBox[currentRow][positionToReveal], 0f, 360f, 300);

        // Store the revealed hint
        revealedHints.put(positionToReveal, correctLetter);

        playSoundAndVibrate(this, R.raw.sound_hint, true, 50);
        updateKeyboardColor(correctLetter, LIGHT_GREEN_COLOR);
        applyKeyboardColors();

        hintUsed++;
        decreaseAndSaveHintCount();
        updateCount(hintCount, vb.hintCountTV);
    }

    private void updateCount(int count, TextView view) {
        view.setText(String.valueOf(count));
    }

    // OnClick Method
    public void handleLeaveButtons(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        if (view.getTag().equals("leave")) {
            saveToSharedPreferences(WORDLE_STREAK_KEY, 0);
            changeActivity(this, GamesActivity.class);

        } else {
            toggleVisibility(false, vb.Shadow.ShadowLayout, vb.leaveGameRL);
        }
    }

    private void displayTargetWord() {
        vb.targetWordTV.setVisibility(VISIBLE);
        vb.targetWordTV.setText(HtmlCompat.fromHtml(
                "Word was: <font color='#FFFF00'>" + targetWord + "</font>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
        ));
        new Handler().postDelayed(() -> vb.targetWordTV.setVisibility(View.GONE), 2000);
    }


    @Override
    protected Class<?> getBackDestination() {
            return GamesActivity.class;
    }

    @Override
    public void backLogic() {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);

        if(vb.Shop.ShopLayout.getVisibility() == VISIBLE) {
            toggleVisibility(false, vb.Shop.ShopLayout, vb.Shadow.ShadowLayout);
            return;
        }

        if(gameWon) {
            changeActivity(this, getBackDestination());
            return;
        }


        if ((currentRow > 0) && (streakCount != 0)) {
            toggleVisibility(vb.leaveGameRL.getVisibility() != View.VISIBLE, vb.Shadow.ShadowLayout, vb.leaveGameRL);
            return;
        }

        changeActivity(this, getBackDestination());
    }

    // OnClick Method
    @Override
    public void handleHomeClick(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        if(currentRow>0 && streakCount != 0) {
            toggleVisibility(true, vb.Shadow.ShadowLayout, vb.leaveGameRL);
        }
        else {
            changeActivity(this, HomeActivity.class);
        }
    }

    // === ANIMATION METHODS ===
    private void animateJiggleEditTextsRow() {
        for (int i = 0; i < MAX_COLS; i++) animateViewJiggle(letterBox[currentRow][i], 150);
    }
    private void animateWaveEditTextsRow() {
        final int duration = 200;
        final int delayBetweenAnimations = 50;

        for (int i = 0; i < MAX_COLS; i++) {
            final int index = i;
            final int startDelay = i * delayBetweenAnimations;

            // Scale down animation
            letterBox[currentRow][i].postDelayed(() -> animateViewScale(letterBox[currentRow][index], 1.0f, 0.5f, duration), startDelay);

            // Scale up animation
            letterBox[currentRow][i].postDelayed(() -> animateViewScale(letterBox[currentRow][index], 0.5f, 1.0f, duration), startDelay + duration + delayBetweenAnimations);
        }
    }
    private void animateFallingEditTexts() {
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        Random random = new Random();
        int baseDelay = 100; // Falling delay per row (ms)

        // Falling animation: bottom-to-top cascade.
        for (int row = 0; row < MAX_ROWS; row++) {
            // For falling, bottom row (MAX_ROWS-1) starts with no delay.
            int rowDelay = (MAX_ROWS - 1 - row) * baseDelay;
            for (int col = 0; col < MAX_COLS; col++) {
                final TextView view = letterBox[row][col];
                // Generate a subtle random tilt between -15° and 15°
                float randomTilt = random.nextFloat() * 30f - 15f;

                // Tilt (rotation) animation.
                ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(view, "rotation", 0f, randomTilt);
                rotationAnim.setDuration(300);
                rotationAnim.setInterpolator(new AccelerateInterpolator());

                // Fall (translation) animation.
                ObjectAnimator translationAnim = ObjectAnimator.ofFloat(view, "translationY", 0f, screenHeight);
                translationAnim.setDuration(500);
                translationAnim.setInterpolator(new AccelerateInterpolator());

                // Play tilt then fall.
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playSequentially(rotationAnim, translationAnim);
                animatorSet.setStartDelay(rowDelay);
                animatorSet.start();
            }
        }

        // Calculate total time until the slowest (top) row has finished falling.
        // For row 0: delay = (MAX_ROWS-1)*baseDelay, plus 300ms (tilt) + 500ms (fall).
        // Add an extra offset (e.g., 100ms) to ensure falling is complete.
        int totalFallingTime = (MAX_ROWS - 1) * baseDelay + 300 + 500 + 100;

        // Schedule regeneration after all falling animations complete.
        new Handler().postDelayed(this::animateRegenerateEditTexts, totalFallingTime);
    }
    private void animateRegenerateEditTexts() {
        int baseRegenDelay = 100; // Regeneration delay per row (ms)

        // Regenerate from top (row 0) to bottom.
        for (int row = 0; row < MAX_ROWS; row++) {
            for (int col = 0; col < MAX_COLS; col++) {
                final TextView view = letterBox[row][col];

                // Cancel any ongoing animations.
                view.animate().cancel();
                // Reset translation and rotation.
                view.setTranslationY(0);
                view.setRotation(0);
                // Ensure the view is visible.
                view.setVisibility(View.VISIBLE);
                view.setAlpha(1f);
                // Set scale to 0 to prepare for regeneration.
                view.setScaleX(0f);
                view.setScaleY(0f);

                // For regeneration, apply a top-to-bottom cascade.
                int regenDelay = row * baseRegenDelay;
                view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .setStartDelay(regenDelay)
                        .start();
            }
        }
    }

    // === To Be Removed Later ===
    private void cheat() {
        increaseAndSaveCurrencyCount(99999);
        displayTargetWord();
    }
    private  void unCheat() {
        saveToSharedPreferences(WORDLE_EXPLOSION_KEY, 0);
        Toast.makeText(this, "Cheat Disabled Nigga", Toast.LENGTH_SHORT).show();
    }
}