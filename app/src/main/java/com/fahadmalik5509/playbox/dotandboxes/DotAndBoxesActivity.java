package com.fahadmalik5509.playbox.dotandboxes;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.CHARCOAL_COLOR;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.DNBS_PLAYER_ONE_NAME_KEY;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.DNBS_PLAYER_TWO_NAME_KEY;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.LIGHT_GREEN_COLOR;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.LIGHT_RED_COLOR;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.animateViewScale;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.changeActivity;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.changeBackgroundColor;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.saveToSharedPreferences;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.sharedPreferences;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.toggleVisibility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.databinding.DotandboxesLayoutBinding;
import com.fahadmalik5509.playbox.databinding.NavigationLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShadowLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShopButtonLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShopLayoutBinding;
import com.fahadmalik5509.playbox.miscellaneous.BaseActivity;
import com.fahadmalik5509.playbox.miscellaneous.GamesActivity;

public class DotAndBoxesActivity extends BaseActivity{

    private DotandboxesLayoutBinding vb;
    private TextView previouslySelectedGridSizeTV = null;
    private boolean isPvAI, isCasual, previousTurnIsPlayerOne;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = DotandboxesLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        // Initialize previous turn state.
        previousTurnIsPlayerOne = vb.dotAndBoxesView.getGame().isPlayerOneTurn();

        // Set up the game view touch listener.
        vb.dotAndBoxesView.setOnTouchListener((v, event) -> {
            v.onTouchEvent(event);
            updateScore();
            boolean currentTurnIsPlayerOne = vb.dotAndBoxesView.getGame().isPlayerOneTurn();
            if (currentTurnIsPlayerOne != previousTurnIsPlayerOne) {
                updateTurnUI();
                previousTurnIsPlayerOne = currentTurnIsPlayerOne;
            }
            if (isPvAI && !currentTurnIsPlayerOne) {
                performCasualAIMove();
            }
            return true;
        });

        setupGameUI();
        initBindings();
    }

    // Update the player card UI based on whose turn it is.
    private void updateTurnUI() {
        if (vb.dotAndBoxesView.getGame().isPlayerOneTurn()) {
            changeBackgroundColor(vb.playerOneRL, LIGHT_GREEN_COLOR);
            changeBackgroundColor(vb.playerTwoRL, CHARCOAL_COLOR);
            animateViewScale(vb.playerOneRL, 1f, 1.1f, 200);
            animateViewScale(vb.playerTwoRL, 1.1f, 1f, 200);
        } else {
            changeBackgroundColor(vb.playerOneRL, CHARCOAL_COLOR);
            changeBackgroundColor(vb.playerTwoRL, LIGHT_RED_COLOR);
            animateViewScale(vb.playerOneRL, 1.1f, 1f, 200);
            animateViewScale(vb.playerTwoRL, 1f, 1.1f, 200);
        }
    }

    // Set up default game settings and UI.
    private void setupGameUI() {
        final int defaultGridSize = 6;
        final String defaultGameMode = "pvai";
        final String defaultDifficulty = "casual";

        vb.playerOneNameTV.setText(sharedPreferences.getString(DNBS_PLAYER_ONE_NAME_KEY, "Player 1"));
        vb.playerTwoNameTV.setText(sharedPreferences.getString(DNBS_PLAYER_TWO_NAME_KEY, "Player 2"));

        toggleVisibility(true, vb.DotAndBoxesMenuLayout, vb.Shadow.ShadowLayout);
        vb.dotAndBoxesView.updateGridSize(defaultGridSize);
        updateGridSizeUI(defaultGridSize);
        updateGameMode(defaultGameMode);
        updateDifficultyUI(defaultDifficulty);
        updateScore();
        updateTurnUI();
    }

    // Initialize additional view bindings.
    private void initBindings() {
        ShopButtonLayoutBinding shopButtonBinding = ShopButtonLayoutBinding.bind(vb.ShopButton.getRoot());
        ShopLayoutBinding shopBinding = ShopLayoutBinding.bind(vb.Shop.getRoot());
        NavigationLayoutBinding NavigationBinding = NavigationLayoutBinding.bind(vb.Navigation.getRoot());
        ShadowLayoutBinding ShadowBinding = ShadowLayoutBinding.bind(vb.Shadow.getRoot());
        setBindings(shopButtonBinding, shopBinding, NavigationBinding, ShadowBinding);
    }

    // Update the score UI.
    private void updateScore() {
        int[] score = vb.dotAndBoxesView.getScore();
        if (vb.dotAndBoxesView.isGameOver()) {
            vb.dotAndBoxesView.gameInProgress = false;
        }
        vb.playerOneScoreTV.setText(String.valueOf(score[0]));
        vb.playerTwoScoreTV.setText(String.valueOf(score[1]));
    }

    // Update grid size UI (for grid size selection views).
    private void updateGridSizeUI(int boxesCount) {
        int dotCount = boxesCount + 1;
        if (dotCount < 5 || dotCount > 10) return;
        TextView[] gridSizeTVs = new TextView[]{
                vb.gridSize5TV, vb.gridSize6TV, vb.gridSize7TV,
                vb.gridSize8TV, vb.gridSize9TV, vb.gridSize10TV
        };
        int selectedIndex = dotCount - 5;
        for (int i = 0; i < gridSizeTVs.length; i++) {
            TextView tv = gridSizeTVs[i];
            tv.setSelected(i == selectedIndex);
            if (i == selectedIndex && previouslySelectedGridSizeTV != tv) {
                animateViewScale(tv, 1.0f, 1.1f, 200);
                if (previouslySelectedGridSizeTV != null) {
                    animateViewScale(previouslySelectedGridSizeTV, 1.1f, 1.0f, 0);
                    previouslySelectedGridSizeTV.setSelected(false);
                }
                previouslySelectedGridSizeTV = tv;
            }
        }
    }

    public void handleDotAndBoxesMenuClick(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        toggleVisibility("open".equals(view.getTag()), vb.DotAndBoxesMenuLayout, vb.Shadow.ShadowLayout);
    }

    public void handleGridSizeButtons(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        int dotsCount = Integer.parseInt(view.getTag().toString());
        vb.dotAndBoxesView.updateGridSize(dotsCount - 1);
        updateScore();
        updateGridSizeUI(dotsCount - 1);
        vb.dotAndBoxesView.gameInProgress = false;
    }

    public void handleGameModeButtons(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        updateGameMode(view.getTag().toString());
    }

    private void updateGameMode(String mode) {
        isPvAI = "pvai".equals(mode);
        vb.pvaiTV.setSelected(isPvAI);
        vb.pvpTV.setSelected(!isPvAI);
        if (isPvAI) {
            animateViewScale(vb.pvaiTV, 1f, 1.05f, 200);
            animateViewScale(vb.pvpTV, 1.05f, 1f, 0);
            toggleVisibility(true, vb.difficultyTV, vb.difficultyLL);
            vb.playerOneNameTV.setText(R.string.you);
            vb.playerTwoNameTV.setText(R.string.ai);
            toggleVisibility(false, vb.profileIV);
            vb.timerTV.setVisibility(INVISIBLE);
        } else {
            animateViewScale(vb.pvaiTV, 1.05f, 1f, 0);
            animateViewScale(vb.pvpTV, 1f, 1.05f, 200);
            toggleVisibility(false, vb.difficultyTV, vb.difficultyLL);
            vb.playerOneNameTV.setText(sharedPreferences.getString(DNBS_PLAYER_ONE_NAME_KEY, "Player 1"));
            vb.playerTwoNameTV.setText(sharedPreferences.getString(DNBS_PLAYER_TWO_NAME_KEY, "Player 2"));
            toggleVisibility(true, vb.profileIV, vb.timerTV);
        }
        vb.dotAndBoxesView.restartGame();
        updateScore();
    }

    public void handleDifficultyButtons(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        isCasual = view.getTag().equals("casual");
        updateDifficultyUI(view.getTag().toString());
    }

    private void updateDifficultyUI(String difficulty) {
        isCasual = "casual".equals(difficulty);
        vb.casualTV.setSelected(isCasual);
        vb.tacticalTV.setSelected(!isCasual);
        if (isCasual) {
            animateViewScale(vb.casualTV, 1f, 1.05f, 200);
            animateViewScale(vb.tacticalTV, 1.05f, 1f, 0);
        } else {
            animateViewScale(vb.casualTV, 1.05f, 1f, 0);
            animateViewScale(vb.tacticalTV, 1f, 1.05f, 200);
        }
    }

    public void handleResetClick(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);

        // If the reset confirmation layout is already visible, process the confirmation.
        if (vb.resetRL.getVisibility() == VISIBLE) {
            if ("yes".equals(view.getTag())) {
                resetGame();
            }
            // In either case, hide the confirmation layout.
            toggleVisibility(false, vb.resetRL, vb.Shadow.ShadowLayout);
            return;
        }

        // If the game is over, reset immediately without showing the confirmation layout.
        if (vb.dotAndBoxesView.isGameOver()) {
            resetGame();
            return;
        }

        // If the game is in progress, show the reset confirmation layout.
        if (vb.dotAndBoxesView.gameInProgress) {
            toggleVisibility(true, vb.resetRL, vb.Shadow.ShadowLayout);
        }
    }

    private void resetGame() {
        vb.dotAndBoxesView.gameInProgress = false;
        vb.dotAndBoxesView.animateGameReset();
        vb.dotAndBoxesView.restartGame();
        updateScore();
        updateTurnUI();
    }

    public void handleExitButtons(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        if ("no".equals(view.getTag())) {
            toggleVisibility(false, vb.leaveRL, vb.Shadow.ShadowLayout);
        } else {
            changeActivity(this, GamesActivity.class);
        }
    }

    public void handleProfileClick(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        animateViewScale(vb.profileRL, 0f, 1.0f, 200);
        toggleVisibility(true, vb.profileRL, vb.Shadow.ShadowLayout);
        vb.playerOneET.setText(sharedPreferences.getString(DNBS_PLAYER_ONE_NAME_KEY, "Player 1"));
        vb.playerTwoET.setText(sharedPreferences.getString(DNBS_PLAYER_TWO_NAME_KEY, "Player 2"));
    }

    public void handleProfileButtons(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        if ("save".equals(view.getTag())) updateProfilesUI();
        toggleVisibility(false, vb.profileRL, vb.Shadow.ShadowLayout);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void updateProfilesUI() {
        if (vb.playerOneET.getText().toString().trim().isEmpty())
            vb.playerOneET.setText(getString(R.string.player_one));
        if (vb.playerTwoET.getText().toString().trim().isEmpty())
            vb.playerTwoET.setText(getString(R.string.player_two));
        String playerOneName = vb.playerOneET.getText().toString().trim().replaceAll("\\s", "");
        String playerTwoName = vb.playerTwoET.getText().toString().trim().replaceAll("\\s", "");
        saveToSharedPreferences(DNBS_PLAYER_ONE_NAME_KEY, playerOneName);
        saveToSharedPreferences(DNBS_PLAYER_TWO_NAME_KEY, playerTwoName);
        vb.playerOneNameTV.setText(sharedPreferences.getString(DNBS_PLAYER_ONE_NAME_KEY, "Player 1"));
        vb.playerTwoNameTV.setText(sharedPreferences.getString(DNBS_PLAYER_TWO_NAME_KEY, "Player 2"));
    }

    // AI move: calls our DotAndBoxesAI helper and updates the UI.
    private void performCasualAIMove() {
        vb.dotAndBoxesView.setInputEnabled(false);
        new Handler().postDelayed(() -> {
            DotAndBoxesGame game = vb.dotAndBoxesView.getGame();
            if (game.isPlayerOneTurn()) {
                vb.dotAndBoxesView.setInputEnabled(true);
                return;
            }
            DotAndBoxesAI.Move chosenMove = DotAndBoxesAI.chooseAIMove(game);
            if (chosenMove == null) {
                vb.dotAndBoxesView.setInputEnabled(true);
                return;
            }
            int boxesBefore = countClaimedBoxes(game.getBoxes());
            game.markLine(chosenMove.isHorizontal, chosenMove.row, chosenMove.col);
            vb.dotAndBoxesView.invalidate();
            updateScore();
            updateTurnUI();
            int boxesAfter = countClaimedBoxes(game.getBoxes());
            if (boxesAfter > boxesBefore) {
                vb.dotAndBoxesView.animateCompletedBoxes();
                playSoundAndVibrate(this, R.raw.sound_box_complete, true, 200);
                performCasualAIMove();
            } else {
                playSoundAndVibrate(this, R.raw.sound_dot_clicked, true, 50);
                vb.dotAndBoxesView.setInputEnabled(true);
            }
        }, 1000);
    }

    private int countClaimedBoxes(int[][] boxes) {
        int count = 0;
        for (int[] row : boxes) {
            for (int box : row) {
                if (box != 0) count++;
            }
        }
        return count;
    }

    @Override
    public void backLogic() {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        if (vb.Shop.ShopLayout.getVisibility() == VISIBLE) {
            toggleVisibility(false, vb.Shop.ShopLayout, vb.Shadow.ShadowLayout);
            return;
        }
        if (vb.DotAndBoxesMenuLayout.getVisibility() == VISIBLE) {
            toggleVisibility(false, vb.DotAndBoxesMenuLayout, vb.Shadow.ShadowLayout);
            return;
        }
        if (vb.resetRL.getVisibility() == VISIBLE) {
            toggleVisibility(false, vb.resetRL, vb.Shadow.ShadowLayout);
            return;
        }
        if (vb.leaveRL.getVisibility() == VISIBLE) {
            toggleVisibility(false, vb.leaveRL, vb.Shadow.ShadowLayout);
            return;
        }
        if (vb.dotAndBoxesView.gameInProgress) {
            toggleVisibility(true, vb.leaveRL, vb.Shadow.ShadowLayout);
            return;
        }
        changeActivity(this, getBackDestination());
    }

    @Override
    protected Class<?> getBackDestination() {
        return GamesActivity.class;
    }
}
