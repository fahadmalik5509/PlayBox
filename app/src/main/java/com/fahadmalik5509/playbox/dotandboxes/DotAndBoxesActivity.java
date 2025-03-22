package com.fahadmalik5509.playbox.dotandboxes;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.animateViewScale;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.changeActivity;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.toggleVisibility;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;

import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.databinding.DotandboxesLayoutBinding;
import com.fahadmalik5509.playbox.databinding.NavigationLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShadowLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShopButtonLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShopLayoutBinding;
import com.fahadmalik5509.playbox.miscellaneous.BaseActivity;
import com.fahadmalik5509.playbox.miscellaneous.GamesActivity;

public class DotAndBoxesActivity extends BaseActivity {

    private DotandboxesLayoutBinding vb;
    private TextView previouslySelectedGridSizeTV = null;
    private TextView previouslySelectedDifficultyTV = null;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = DotandboxesLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        vb.dotAndBoxesView.setOnTouchListener((v, event) -> {
            v.onTouchEvent(event);
            updateScore();
            return true;
        });

        setupOnBackPressed();
        setupGameUI();
        getBindings();
    }

    private void setupGameUI() {
        // Default values (grid size here represents boxes count)
        int defaultGridSize = 6;
        int defaultDifficulty = 0;
        String defaultGameMode = "pvp";

        toggleVisibility(true, vb.DotAndBoxesMenuLayout, vb.Shadow.ShadowLayout);
        vb.dotAndBoxesView.updateGridSize(defaultGridSize);
        updateGridSizeUI(defaultGridSize);
        updateGameModeUI(defaultGameMode);
        updateDifficultyUI(defaultDifficulty);
        updateScore();
    }

    private void setupOnBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Implement back navigation logic here if needed.
            }
        });
    }

    private void getBindings() {
        ShopButtonLayoutBinding shopButtonBinding = ShopButtonLayoutBinding.bind(vb.ShopButton.getRoot());
        ShopLayoutBinding shopBinding = ShopLayoutBinding.bind(vb.Shop.getRoot());
        NavigationLayoutBinding navigationBinding = NavigationLayoutBinding.bind(vb.Navigation.getRoot());
        ShadowLayoutBinding shadowBinding = ShadowLayoutBinding.bind(vb.Shadow.getRoot());
        setBindings(shopButtonBinding, shopBinding, navigationBinding, shadowBinding);
    }

    @Override
    protected Class<?> getBackDestination() {
        return GamesActivity.class;
    }

    private void updateScore() {
        String score = vb.dotAndBoxesView.getScoreText();
        if (vb.dotAndBoxesView.isGameOver()) {
            score += "\nGame Over!";
            vb.dotAndBoxesView.gameInProgress = false;
        }
        vb.tvScore.setText(score);
    }

    public void handleDotAndBoxesMenuClick(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        if ("open".equals(view.getTag())) {
            toggleVisibility(true, vb.DotAndBoxesMenuLayout, vb.Shadow.ShadowLayout);
        } else if ("close".equals(view.getTag())) {
            toggleVisibility(false, vb.DotAndBoxesMenuLayout, vb.Shadow.ShadowLayout);
        }
    }

    public void handleGridSizeButtons(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        // Assuming the tag value represents the number of dots (e.g., "8")
        int dotsCount = Integer.parseInt(view.getTag().toString());
        // If grid size in game logic represents boxes count, subtract 1.
        int boxesCount = dotsCount - 1;
        vb.dotAndBoxesView.updateGridSize(boxesCount);
        updateScore();
        updateGridSizeUI(boxesCount);
    }

    private void updateGridSizeUI(int boxesCount) {
        int dotCount = boxesCount + 1;
        if (dotCount < 5 || dotCount > 10) return;

        TextView[] gridSizeTVs = new TextView[]{
                vb.gridSize5TV,
                vb.gridSize6TV,
                vb.gridSize7TV,
                vb.gridSize8TV,
                vb.gridSize9TV,
                vb.gridSize10TV
        };

        int selectedIndex = dotCount - 5;
        for (int i = 0; i < gridSizeTVs.length; i++) {
            TextView tv = gridSizeTVs[i];
            if (i == selectedIndex) {
                if (previouslySelectedGridSizeTV != tv) {
                    animateViewScale(tv, 1.0f, 1.1f, 200);
                    tv.setSelected(true);
                    if (previouslySelectedGridSizeTV != null && previouslySelectedGridSizeTV != tv) {
                        animateViewScale(previouslySelectedGridSizeTV, 1.1f, 1.0f, 0);
                        previouslySelectedGridSizeTV.setSelected(false);
                    }
                    previouslySelectedGridSizeTV = tv;
                }
            } else {
                tv.setSelected(false);
            }
        }
    }

    public void handleGameModeButtons(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        updateGameModeUI(view.getTag().toString());
    }

    private void updateGameModeUI(String mode) {
        if ("pvai".equals(mode)) {
            vb.pvaiTV.setSelected(true);
            vb.pvpTV.setSelected(false);
            animateViewScale(vb.pvaiTV, 1f, 1.05f, 200);
            animateViewScale(vb.pvpTV, 1.05f, 1f, 0);
            toggleVisibility(true, vb.selectDifficultyTV, vb.selectDifficultyLL);
        } else {
            vb.pvaiTV.setSelected(false);
            vb.pvpTV.setSelected(true);
            animateViewScale(vb.pvaiTV, 1.05f, 1f, 0);
            animateViewScale(vb.pvpTV, 1f, 1.05f, 200);
            toggleVisibility(false, vb.selectDifficultyTV, vb.selectDifficultyLL);
        }
    }

    public void handleDifficultyButtons(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        int selected = Integer.parseInt(view.getTag().toString());
        updateDifficultyUI(selected - 1);
    }

    private void updateDifficultyUI(int selectedDifficulty) {
        TextView[] difficultyTVs = new TextView[]{
                vb.easyTV,
                vb.mediumTV,
                vb.hardTV
        };

        for (int i = 0; i < difficultyTVs.length; i++) {
            TextView tv = difficultyTVs[i];
            if (i == selectedDifficulty) {
                if (previouslySelectedDifficultyTV != tv) {
                    animateViewScale(tv, 1.0f, 1.1f, 200);
                    tv.setSelected(true);
                    if (previouslySelectedDifficultyTV != null && previouslySelectedDifficultyTV != tv) {
                        animateViewScale(previouslySelectedDifficultyTV, 1.1f, 1.0f, 0);
                        previouslySelectedDifficultyTV.setSelected(false);
                    }
                    previouslySelectedDifficultyTV = tv;
                }
            } else {
                tv.setSelected(false);
            }
        }
    }

    public void handleResetClick(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        if (vb.dotAndBoxesView.gameInProgress) {
            toggleVisibility(true, vb.resetRL, vb.Shadow.ShadowLayout);
        }
        if ("yes".equals(view.getTag())) {
            toggleVisibility(false, vb.resetRL, vb.Shadow.ShadowLayout);
            vb.dotAndBoxesView.gameInProgress = false;
            vb.dotAndBoxesView.restartGame();
            updateScore();
            return;
        } else if ("no".equals(view.getTag())) {
            toggleVisibility(false, vb.resetRL, vb.Shadow.ShadowLayout);
            return;
        }
        vb.dotAndBoxesView.restartGame();
        updateScore();
    }

    public void handleExitButtons(View view) {
        playSoundAndVibrate(this, R.raw.sound_ui, true, 50);
        if ("no".equals(view.getTag())) {
            toggleVisibility(false, vb.leaveRL, vb.Shadow.ShadowLayout);
        } else {
            changeActivity(this, GamesActivity.class);
        }
    }
}
