package com.fahadmalik5509.playbox.miscellaneous;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.WORDLE_BOMB_KEY;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.WORDLE_CONTRAST_KEY;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.WORDLE_CURRENCY_KEY;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.WORDLE_HINT_KEY;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.WORDLE_JUMP_KEY;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.WORDLE_SKIP_KEY;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.WORDLE_SPOTLIGHT_KEY;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.WORDLE_STRIKE_KEY;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.changeActivity;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.saveToSharedPreferences;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.sharedPreferences;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.toggleVisibility;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.gpamanager.ProfileActivity;
import com.fahadmalik5509.playbox.gpamanager.SemesterActivity;
import com.fahadmalik5509.playbox.gpamanager.SubjectActivity;
import com.fahadmalik5509.playbox.databinding.NavigationLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShadowLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShopButtonLayoutBinding;
import com.fahadmalik5509.playbox.databinding.ShopLayoutBinding;

public abstract class BaseActivity extends AppCompatActivity {

    private ShopButtonLayoutBinding ShopButtonBinding;
    private ShopLayoutBinding ShopBinding;
    private NavigationLayoutBinding NavigationBinding;
    private ShadowLayoutBinding ShadowBinding;

    protected int currencyCount, bombCount, hintCount, skipCount, strikeCount, spotlightCount, contrastCount, jumpCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize();
    }

    public interface ShopUpdateListener {
        void onShopClosed();
    }

    private ShopUpdateListener shopUpdateListener;

    public void setShopUpdateListener(ShopUpdateListener listener) {
        this.shopUpdateListener = listener;
    }

    private void initialize() {
        setupOnBackPressed();

        currencyCount = sharedPreferences.getInt(WORDLE_CURRENCY_KEY, 300);
        bombCount = sharedPreferences.getInt(WORDLE_BOMB_KEY, 5);
        hintCount = sharedPreferences.getInt(WORDLE_HINT_KEY, 5);
        skipCount = sharedPreferences.getInt(WORDLE_SKIP_KEY, 5);
        strikeCount = sharedPreferences.getInt(WORDLE_STRIKE_KEY, 5);
        spotlightCount = sharedPreferences.getInt(WORDLE_SPOTLIGHT_KEY, 5);
        contrastCount = sharedPreferences.getInt(WORDLE_CONTRAST_KEY, 5);
        jumpCount = sharedPreferences.getInt(WORDLE_JUMP_KEY, 5);
    }

    protected void setBindings(ShopButtonLayoutBinding ShopButtonBinding, ShopLayoutBinding ShopBinding, NavigationLayoutBinding NavigationBinding, ShadowLayoutBinding ShadowBinding) {
        this.ShopButtonBinding = ShopButtonBinding;
        this.ShopBinding = ShopBinding;
        this.NavigationBinding = NavigationBinding;
        this.ShadowBinding = ShadowBinding;
        updateShopUI();
    }

    private void updateShopUI() {
        ShopButtonBinding.currencyCountTV.setText(String.valueOf(currencyCount));
        ShopBinding.shopCurrencyCountTV.setText(String.valueOf(currencyCount));
        ShopBinding.shopBombCountTV.setText(String.valueOf(bombCount));
        ShopBinding.shopHintCountTV.setText(String.valueOf(hintCount));
        ShopBinding.shopSkipCountTV.setText(String.valueOf(skipCount));
        ShopBinding.shopStrikeCountTV.setText(String.valueOf(strikeCount));
        ShopBinding.shopSpotlightCountTV.setText(String.valueOf(spotlightCount));
        ShopBinding.shopContrastCountTV.setText(String.valueOf(contrastCount));
        ShopBinding.shopJumpCountTV.setText(String.valueOf(jumpCount));
        ShopBinding.wordleTV.setSelected(true);
    }

    protected void increaseAndSaveCurrencyCount(int amount) {
        currencyCount += amount;
        saveToSharedPreferences(WORDLE_CURRENCY_KEY, currencyCount);
        ShopButtonBinding.currencyCountTV.setText(String.valueOf(currencyCount));
        ShopBinding.shopCurrencyCountTV.setText(String.valueOf(currencyCount));
    }

    protected void decreaseAndSaveCurrencyCount(int amount) {
        currencyCount -= amount;
        saveToSharedPreferences(WORDLE_CURRENCY_KEY, currencyCount);
        ShopButtonBinding.currencyCountTV.setText(String.valueOf(currencyCount));
        ShopBinding.shopCurrencyCountTV.setText(String.valueOf(currencyCount));
    }
    protected void decreaseAndSaveBombCount() {
        bombCount--;
        saveToSharedPreferences(WORDLE_BOMB_KEY, bombCount);

    }
    protected void decreaseAndSaveHintCount() {
        hintCount--;
        saveToSharedPreferences(WORDLE_HINT_KEY, hintCount);
    }
    protected void decreaseAndSaveSkipCount() {
        skipCount--;
        saveToSharedPreferences(WORDLE_SKIP_KEY, skipCount);
    }
    protected void decreaseAndSaveStrikeCount() {
        strikeCount--;
        saveToSharedPreferences(WORDLE_STRIKE_KEY, strikeCount);
    }
    protected void decreaseAndSaveSpotlightCount() {
        spotlightCount--;
        saveToSharedPreferences(WORDLE_SPOTLIGHT_KEY, spotlightCount);
    }
    protected void decreaseAndSaveContrastCount() {
        contrastCount--;
        saveToSharedPreferences(WORDLE_CONTRAST_KEY, contrastCount);
    }
    protected void decreaseAndSaveJumpCount() {
        jumpCount--;
        saveToSharedPreferences(WORDLE_JUMP_KEY, jumpCount);
    }

    public void handleShopButtonClick(View view) {

        playSoundAndVibrate(R.raw.sound_register, true, 50);

        ShopBinding.shopCurrencyCountTV.setText(String.valueOf(currencyCount));
        ShopBinding.shopBombCountTV.setText(String.valueOf(bombCount));
        ShopBinding.shopSkipCountTV.setText(String.valueOf(skipCount));
        ShopBinding.shopHintCountTV.setText(String.valueOf(hintCount));
        ShopBinding.shopStrikeCountTV.setText(String.valueOf(strikeCount));
        ShopBinding.shopSpotlightCountTV.setText(String.valueOf(spotlightCount));
        ShopBinding.shopContrastCountTV.setText(String.valueOf(contrastCount));
        ShopBinding.shopJumpCountTV.setText(String.valueOf(jumpCount));

        toggleVisibility(true, ShopBinding.ShopLayout, ShadowBinding.ShadowLayout);
    }

    public void handleShopButtons(View view) {
        if(view.getTag().toString().equals("close")) {
            playSoundAndVibrate(R.raw.sound_ui, true, 50);
            toggleVisibility(false, ShopBinding.ShopLayout, ShadowBinding.ShadowLayout);

            if (shopUpdateListener != null) {
                shopUpdateListener.onShopClosed();
            }
            return;
        }

        switch (view.getTag().toString()) {
            case "bomb":
                if(currencyCount >= 40) {
                    playSoundAndVibrate(R.raw.sound_bought, true, 50);
                    bombCount += 1;
                    saveToSharedPreferences(WORDLE_BOMB_KEY, bombCount);
                    ShopBinding.shopBombCountTV.setText(String.valueOf(bombCount));
                    decreaseAndSaveCurrencyCount(40);
                }
                else playSoundAndVibrate(R.raw.sound_error, true, 50);
                break;
            case "hint":
                if(currencyCount >= 100) {
                    playSoundAndVibrate(R.raw.sound_bought, true, 50);
                    hintCount += 1;
                    saveToSharedPreferences(WORDLE_HINT_KEY, hintCount);
                    ShopBinding.shopHintCountTV.setText(String.valueOf(hintCount));
                    decreaseAndSaveCurrencyCount(100);
                }
                else playSoundAndVibrate(R.raw.sound_error, true, 50);
                break;
            case "skip":
                if(currencyCount >= 200) {
                    playSoundAndVibrate(R.raw.sound_bought, true, 50);
                    skipCount += 1;
                    saveToSharedPreferences(WORDLE_SKIP_KEY, skipCount);
                    ShopBinding.shopSkipCountTV.setText(String.valueOf(skipCount));
                    decreaseAndSaveCurrencyCount(200);
                }
                else playSoundAndVibrate(R.raw.sound_error, true, 50);
                break;
            case "strike":
                if(currencyCount >= 100) {
                    playSoundAndVibrate(R.raw.sound_bought, true, 50);
                    strikeCount += 1;
                    saveToSharedPreferences(WORDLE_STRIKE_KEY, strikeCount);
                    ShopBinding.shopStrikeCountTV.setText(String.valueOf(strikeCount));
                    decreaseAndSaveCurrencyCount(100);
                }
                else playSoundAndVibrate(R.raw.sound_error, true, 50);
                break;
            case "spotlight":
                if(currencyCount >= 100) {
                    playSoundAndVibrate(R.raw.sound_bought, true, 50);
                    spotlightCount += 1;
                    saveToSharedPreferences(WORDLE_SPOTLIGHT_KEY, spotlightCount);
                    ShopBinding.shopSpotlightCountTV.setText(String.valueOf(spotlightCount));
                    decreaseAndSaveCurrencyCount(100);
                }
                else playSoundAndVibrate(R.raw.sound_error, true, 50);
                break;
            case "contrast":
                if(currencyCount >= 150) {
                    playSoundAndVibrate(R.raw.sound_bought, true, 50);
                    contrastCount += 1;
                    saveToSharedPreferences(WORDLE_CONTRAST_KEY, contrastCount);
                    ShopBinding.shopContrastCountTV.setText(String.valueOf(contrastCount));
                    decreaseAndSaveCurrencyCount(150);
                }
                else playSoundAndVibrate(R.raw.sound_error, true, 50);
                break;
            case "jump":
                if(currencyCount >= 200) {
                    playSoundAndVibrate(R.raw.sound_bought, true, 50);
                    jumpCount += 1;
                    saveToSharedPreferences(WORDLE_JUMP_KEY, jumpCount);
                    ShopBinding.shopJumpCountTV.setText(String.valueOf(jumpCount));
                    decreaseAndSaveCurrencyCount(200);
                }
                else playSoundAndVibrate(R.raw.sound_error, true, 50);
                break;

            case "wordle":
                if(ShopBinding.shopWordleLL.getVisibility() == VISIBLE) return;

                playSoundAndVibrate(R.raw.sound_ui, true, 50);
                ShopBinding.wordleTV.setSelected(true);
                ShopBinding.colorpuzzleTV.setSelected(false);
                ShopBinding.shopWordleLL.setVisibility(VISIBLE);
                ShopBinding.shopCPLL.setVisibility(GONE);
                break;

            case "colorpuzzle":
                if(ShopBinding.shopCPLL.getVisibility() == VISIBLE) return;
                playSoundAndVibrate(R.raw.sound_ui, true, 50);
                ShopBinding.wordleTV.setSelected(false);
                ShopBinding.colorpuzzleTV.setSelected(true);
                ShopBinding.shopWordleLL.setVisibility(GONE);
                ShopBinding.shopCPLL.setVisibility(VISIBLE);
                break;
        }
    }

    public void handleBackClick(View view) {
        backLogic();
    }

    public void backLogic() {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);

        if(this instanceof ToolsActivity || this instanceof ProfileActivity || this instanceof SemesterActivity || this instanceof SubjectActivity) {
            changeActivity(this, getBackDestination());
            return;
        }

        if(this instanceof HomeActivity) {
            return;
        }

        if(ShopBinding.ShopLayout.getVisibility() == VISIBLE) {
            toggleVisibility(false, ShopBinding.ShopLayout, ShadowBinding.ShadowLayout);
            return;
        }

        changeActivity(this, getBackDestination());
    }

    public void handleHomeClick(View view) {
        if (this instanceof HomeActivity) {
            playSoundAndVibrate(R.raw.sound_error, true, 50);
            return;
        }

        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        changeActivity(this, HomeActivity.class);
    }

    public void goToSetting(View view) {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        Intent intent = new Intent(this, SettingActivity.class);
        intent.putExtra("origin_activity", this.getClass().getSimpleName());
        startActivity(intent);
    }

    protected abstract Class<?> getBackDestination();

    public void setupOnBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backLogic();
            }
        });
    }
}
