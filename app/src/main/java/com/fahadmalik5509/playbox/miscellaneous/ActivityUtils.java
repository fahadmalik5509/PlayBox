package com.fahadmalik5509.playbox.miscellaneous;

import static android.content.Context.MODE_PRIVATE;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.SparseIntArray;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.core.content.ContextCompat;

import com.fahadmalik5509.playbox.R;


public class ActivityUtils {

    public static boolean isSoundEnabled, isVibrationEnabled;
    public static final String PREFS_NAME = "MyAppSettings";
    public static final String SOUND_KEY = "soundEnabled";
    public static final String VIBRATION_KEY = "vibrationEnabled";
    public static final String TTT_DIFFICULTY_KEY = "TTT_currentDifficulty";
    public static final String TTT_PLAYER_ONE_NAME_KEY = "playerOneName";
    public static final String TTT_PLAYER_TWO_NAME_KEY = "playerTwoName";
    public static final String WORDLE_STREAK_KEY = "streakNumber";
    public static final String WORDLE_HIGHEST_STREAK_KEY = "streakHighestNumber";
    public static final String WORDLE_EXPLOSION_KEY = "boomKey";
    public static final String WORDLE_CURRENCY_KEY = "currency";
    public static final String WORDLE_BOMB_KEY = "bomb";
    public static final String WORDLE_SKIP_KEY = "skip";
    public static final String WORDLE_HINT_KEY = "hint";
    public static final String PUZZLE_EASY_SCORE_KEY = "puzzleEasyScore";
    public static final String PUZZLE_MEDIUM_SCORE_KEY = "puzzleMediumScore";
    public static final String PUZZLE_HARD_SCORE_KEY = "puzzleHardScore";
    public static final String WORDLE_STRIKE_KEY = "strike";
    public static final String WORDLE_SPOTLIGHT_KEY = "spotlight";
    public static final String WORDLE_CONTRAST_KEY = "contrast";
    public static final String WORDLE_JUMP_KEY = "jump";

    public static int BACKGROUND_COLOR;
    public static int BLACK_COLOR;
    public static int GRAY_COLOR;
    public static int WHITE_COLOR;
    public static int BEIGE_COLOR;
    public static int HINT_COLOR;
    public static int DARK_RED_COLOR;
    public static int LIGHT_RED_COLOR;
    public static int YELLOW_COLOR;
    public static int DARK_GREEN_COLOR;
    public static int LIGHT_GREEN_COLOR;
    public static int BLUE_COLOR;
    public static int CHARCOAL_COLOR;
    public static int BROWNISH_GRAY_COLOR;
    public static int ESPRESSO_COLOR;

    private static SoundPool soundPool;
    public static int fun_openURL = 0;
    private static final SparseIntArray soundMap = new SparseIntArray();
    public static SharedPreferences sharedPreferences;

    private ActivityUtils() {
        throw new UnsupportedOperationException("Cannot instantiate utility class.");
    }

    public static void initializeSoundPool(Context context) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        // Preload sounds (You can load more sounds as needed)
        soundMap.put(R.raw.sound_click, soundPool.load(context, R.raw.sound_click, 1));
        soundMap.put(R.raw.sound_ui, soundPool.load(context, R.raw.sound_ui, 1));
        soundMap.put(R.raw.sound_error, soundPool.load(context, R.raw.sound_error, 1));
        soundMap.put(R.raw.sound_draw, soundPool.load(context, R.raw.sound_draw, 1));
        soundMap.put(R.raw.sound_win, soundPool.load(context, R.raw.sound_win, 1));
        soundMap.put(R.raw.sound_key, soundPool.load(context, R.raw.sound_key, 1));
        soundMap.put(R.raw.sound_enter, soundPool.load(context, R.raw.sound_enter, 1));
        soundMap.put(R.raw.sound_backspace, soundPool.load(context, R.raw.sound_backspace, 1));
        soundMap.put(R.raw.sound_explosion, soundPool.load(context, R.raw.sound_explosion, 1));
        soundMap.put(R.raw.sound_explosion2, soundPool.load(context, R.raw.sound_explosion2, 1));
        soundMap.put(R.raw.sound_hint, soundPool.load(context, R.raw.sound_hint, 1));
        soundMap.put(R.raw.sound_skip, soundPool.load(context, R.raw.sound_skip, 1));
        soundMap.put(R.raw.sound_bought, soundPool.load(context, R.raw.sound_bought, 1));
        soundMap.put(R.raw.sound_coin, soundPool.load(context, R.raw.sound_coin, 1));
        soundMap.put(R.raw.sound_register, soundPool.load(context, R.raw.sound_register, 1));
        soundMap.put(R.raw.sound_flame, soundPool.load(context, R.raw.sound_flame, 1));
        soundMap.put(R.raw.sound_coin_flip, soundPool.load(context, R.raw.sound_coin_flip, 1));
        soundMap.put(R.raw.sound_coin_reveal, soundPool.load(context, R.raw.sound_coin_reveal, 1));
        soundMap.put(R.raw.sound_success, soundPool.load(context, R.raw.sound_success, 1));
        soundMap.put(R.raw.sound_heart_crack, soundPool.load(context, R.raw.sound_heart_crack, 1));
        soundMap.put(R.raw.sound_game_over, soundPool.load(context, R.raw.sound_game_over, 1));
        soundMap.put(R.raw.sound_new_level, soundPool.load(context, R.raw.sound_new_level, 1));
        soundMap.put(R.raw.sound_reveal, soundPool.load(context, R.raw.sound_reveal, 1));
        soundMap.put(R.raw.sound_contrast, soundPool.load(context, R.raw.sound_contrast, 1));
        soundMap.put(R.raw.sound_dot_clicked, soundPool.load(context, R.raw.sound_dot_clicked, 1));
        soundMap.put(R.raw.sound_box_complete, soundPool.load(context, R.raw.sound_box_complete, 1));
    }

    public static void playSoundAndVibrate(Context context, int soundResId, boolean vibrate, int vibrationDuration) {
        if (context == null || soundPool == null) return;

        if(vibrate) vibrate(context, vibrationDuration);

        if (!isSoundEnabled) return;

        int soundID = soundMap.get(soundResId, -1);
        if (soundID != -1) {
            soundPool.play(soundID, 1f, 1f, 1, 0, 1f);
        }
    }

    public static void loadPreference(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    public static void saveToSharedPreferences(String key, Object value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        }

        editor.apply();
    }

    public static void vibrate(Context context, long milliseconds) {

        if (!isVibrationEnabled) return;

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null && vibrator.hasVibrator()) {
            // For devices running Android O (API 26) and above
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                // For devices below Android O
                vibrator.vibrate(milliseconds);
            }
        }
    }

    public static void animateViewScale(View view, float startScale, float endScale, int Duration) {
        if (view != null) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", startScale, endScale);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", startScale, endScale);

            scaleX.setDuration(Duration);
            scaleY.setDuration(Duration);

            scaleX.setInterpolator(new LinearInterpolator());
            scaleY.setInterpolator(new LinearInterpolator());

            scaleX.start();
            scaleY.start();

        }
    }

    public static void animateViewPulse(View view, float startScale, float endScale, int Duration) {
        animateViewScale(view, startScale, endScale, Duration);
        view.postDelayed(() -> animateViewScale(view,endScale,startScale, Duration), Duration);
    }

    public static void animateViewBounce(View view) {

        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", -15f, 0f);
        animator.setDuration(150);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    public static void animateViewJiggle(View view, int duration) {

        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", -10f, 10f, 0f);
        animator.setDuration(duration);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    public static void animateBlink(View view, int duration, int repeatCount) {
        if (view == null) return;

        ObjectAnimator blink = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f, 1f);
        blink.setDuration(duration);
        blink.setRepeatMode(ValueAnimator.REVERSE);
        blink.setRepeatCount(repeatCount);
        blink.start();
    }


    public static void animateText(View view, float startAngle, float endAngle, int duration) {
        view.setRotation(startAngle);
        view.animate().rotation(endAngle).setDuration(duration).setListener(null);
    }

    public static void changeActivity(Activity fromActivity, Class<?> toActivity) {
        if (toActivity == null) {
            return;
        }
        Intent intent = new Intent(fromActivity, toActivity);
        fromActivity.startActivity(intent);
        fromActivity.finish();
    }

    public static int getColorByID(Context context, int colorResId) {

        return ContextCompat.getColor(context, colorResId);
    }

    public static void loadColors(Context context) {
        BACKGROUND_COLOR = getColorByID(context, R.color.background);
        BLACK_COLOR = getColorByID(context, R.color.black);
        GRAY_COLOR = getColorByID(context, R.color.gray);
        WHITE_COLOR = getColorByID(context, R.color.white);
        BEIGE_COLOR = getColorByID(context, R.color.beige);
        HINT_COLOR = getColorByID(context, R.color.hint);
        DARK_RED_COLOR = getColorByID(context, R.color.dark_red);
        LIGHT_RED_COLOR = getColorByID(context, R.color.light_red);
        YELLOW_COLOR = getColorByID(context, R.color.yellow);
        DARK_GREEN_COLOR = getColorByID(context, R.color.dark_green);
        LIGHT_GREEN_COLOR = getColorByID(context, R.color.light_green);
        BLUE_COLOR = getColorByID(context, R.color.blue);
        CHARCOAL_COLOR = getColorByID(context, R.color.charcoal);
        BROWNISH_GRAY_COLOR = getColorByID(context, R.color.brownish_gray);
        ESPRESSO_COLOR = getColorByID(context, R.color.espresso);
    }

    public static void toggleVisibility(boolean visible, View... views) {
        for (View view : views) {
            if (visible) {
                view.setVisibility(View.VISIBLE);
            } else {
                view.setVisibility(View.GONE);
            }
        }
    }

    public static void changeBackgroundColor(View view, int color) {
        if (view == null) return;

        Drawable background = view.getBackground();
        if (background instanceof GradientDrawable) {
            ((GradientDrawable) background).setColor(color);
        } else {
            view.setBackgroundTintList(ColorStateList.valueOf(color));
        }
    }

    public static int getBackgroundColor(View view) {
        if (view.getBackground() != null) {
            return ((ColorDrawable) view.getBackground()).getColor();
        }
        return Color.TRANSPARENT; // Default if no color is set
    }

}