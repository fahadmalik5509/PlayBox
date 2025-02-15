package com.fahadmalik5509.playbox;

import static android.content.Context.MODE_PRIVATE;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.content.SharedPreferences;

import androidx.core.content.ContextCompat;

import java.util.Random;

public class ActivityUtils {

    static final int ACTIVITY_TRANSITION_DELAY_MS = 50;
    public static final String PREFS_NAME = "MyAppSettings";
    public static final String SOUND_KEY = "soundEnabled";
    public static final String VIBRATION_KEY = "vibrationEnabled";
    public static final String TTT_DIFFICULTY_KEY = "difficultyLevel";
    public static final String TTT_PLAYERONE_NAME_KEY = "playerOneName";
    public static final String TTT_PLAYERTWO_NAME_KEY = "playerTwoName";
    public static final String WORDLE_STREAK_KEY = "streakNumber";
    public static final String WORDLE_HIGHEST_STREAK_KEY = "streakHighestNumber";
    public static final String WORDLE_EXPLOSION_KEY = "boomKey";
    public static final String WORDLE_CURRENCY_KEY = "currency";
    public static final String WORDLE_BOMB_KEY = "bomb";
    public static final String WORDLE_SKIP_KEY = "skip";
    public static final String WORDLE_HINT_KEY = "hint";
    public static final String PUZZLE_BEST_SCORE = "bestScore";

    public static int BACKGROUND_COLOR;
    public static int BLACK_COLOR;
    public static int GRAY_COLOR;
    public static int WHITE_COLOR;
    public static int BEIGE_COLOR;
    public static int HINT_COLOR;
    public static int RED_COLOR;
    public static int YELLOW_COLOR;
    public static int GREEN_COLOR;

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
        soundMap.put(R.raw.click_board, soundPool.load(context, R.raw.click_board, 1));
        soundMap.put(R.raw.click_ui, soundPool.load(context, R.raw.click_ui, 1));
        soundMap.put(R.raw.click_error, soundPool.load(context, R.raw.click_error, 1));
        soundMap.put(R.raw.draw, soundPool.load(context, R.raw.draw, 1));
        soundMap.put(R.raw.win, soundPool.load(context, R.raw.win, 1));
        soundMap.put(R.raw.key, soundPool.load(context, R.raw.key, 1));
        soundMap.put(R.raw.enter, soundPool.load(context, R.raw.enter, 1));
        soundMap.put(R.raw.backspace, soundPool.load(context, R.raw.backspace, 1));
        soundMap.put(R.raw.explosion, soundPool.load(context, R.raw.explosion, 1));
        soundMap.put(R.raw.explosion2, soundPool.load(context, R.raw.explosion2, 1));
        soundMap.put(R.raw.hint, soundPool.load(context, R.raw.hint, 1));
        soundMap.put(R.raw.skip, soundPool.load(context, R.raw.skip, 1));
        soundMap.put(R.raw.bought, soundPool.load(context, R.raw.bought, 1));
        soundMap.put(R.raw.coin, soundPool.load(context, R.raw.coin, 1));
        soundMap.put(R.raw.register, soundPool.load(context, R.raw.register, 1));
        soundMap.put(R.raw.flamesfx, soundPool.load(context, R.raw.flamesfx, 1));
        soundMap.put(R.raw.acoinflip, soundPool.load(context, R.raw.acoinflip, 1));
        soundMap.put(R.raw.acoinreveal, soundPool.load(context, R.raw.acoinreveal, 1));
        soundMap.put(R.raw.sucess, soundPool.load(context, R.raw.sucess, 1));
        soundMap.put(R.raw.aheartbreak, soundPool.load(context, R.raw.aheartbreak, 1));
        soundMap.put(R.raw.agameover, soundPool.load(context, R.raw.agameover, 1));
        soundMap.put(R.raw.newlevel, soundPool.load(context, R.raw.newlevel, 1));

    }

    public static void playSound(Context context, int soundResId) {
        if (context == null || soundPool == null) return;

        loadPreference(context);
        boolean isSoundEnabled = sharedPreferences.getBoolean(SOUND_KEY, true);

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

        loadPreference(context);
        boolean isVibrationEnabled = sharedPreferences.getBoolean(VIBRATION_KEY, true);

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

    @SuppressLint("ClickableViewAccessibility")
    public static void animateViewPulse(Context context, View view, boolean changeAlpha) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    animateViewScale(view, 1f, 0.9f, 100);
                    if(changeAlpha) view.setAlpha(0.8f);
                    return false;

                case MotionEvent.ACTION_UP:
                    animateViewScale(view, 0.9f, 1f, 100);
                    vibrate(context, 50);
                    if(changeAlpha) view.setAlpha(1f);
                    return false;
            }
            return false;
        });
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

    public static void animateText(View view, float startAngle, float endAngle, int duration) {
        view.setRotation(startAngle);
        view.animate().rotation(endAngle).setDuration(duration).setListener(null);
    }

    public static void changeActivity(Activity fromActivity, Class < ? > toActivity, boolean shouldFinish, boolean animate) {

        Intent intent = new Intent(fromActivity, toActivity);
        fromActivity.startActivity(intent);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        if (animate) fromActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        if (shouldFinish) fromActivity.finish();
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
        RED_COLOR = getColorByID(context, R.color.red);
        YELLOW_COLOR = getColorByID(context, R.color.yellow);
        GREEN_COLOR = getColorByID(context, R.color.green);
    }

    public static int getRandomNumber(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
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
        if (view == null) return ContextCompat.getColor(view.getContext(), android.R.color.transparent);

        Drawable background = view.getBackground();
        if (background instanceof ColorDrawable) {
            return ((ColorDrawable) background).getColor();
        }
        return ContextCompat.getColor(view.getContext(), android.R.color.transparent);
    }
}