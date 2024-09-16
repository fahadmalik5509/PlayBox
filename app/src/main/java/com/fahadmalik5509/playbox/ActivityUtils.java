package com.fahadmalik5509.playbox;

import static android.content.Context.MODE_PRIVATE;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
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

public class ActivityUtils {

    public static final int GREEN_COLOR = Color.parseColor("#11e889");
    public static final int YELLOW_COLOR = Color.parseColor("#ffc107");
    public static final int RED_COLOR = Color.parseColor("#7c2e2e");
    public static final int BACKGROUND_COLOR = Color.parseColor("#1a1a1a");
    public static final int BEIGE_COLOR = Color.parseColor("#F5F5DC");
    public static final int GRAY_COLOR = Color.parseColor("#2a2a2a");
    static final int ACTIVITY_TRANSITION_DELAY_MS = 50;
    public static final String PREFS_NAME = "MyAppSettings";
    public static final String SOUND_KEY = "soundEnabled";
    public static final String VIBRATION_KEY = "vibrationEnabled";
    public static final String DIFFICULTY_KEY = "difficultyLevel";
    public static final String WORDLE_STREAK = "streakNumber";
    public static final String WORDLE_HIGHEST_STREAK = "streakHighestNumber";
    public static boolean isVsAi = false;
    private static SoundPool soundPool;
    public static int fun_openURL = 0;
    private static final SparseIntArray soundMap = new SparseIntArray();
    public static SharedPreferences sharedPreferences;

    private ActivityUtils() { throw new UnsupportedOperationException("Cannot instantiate utility class."); }

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


    public static void animateViewScale(View view, float startScale, float endScale,int Duration) {
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
    public static void animateViewPulse(Context context, View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    animateViewScale(view, 1f, 0.9f,100);
                    view.setAlpha(0.8f);
                    return false;

                case MotionEvent.ACTION_UP:
                    animateViewScale(view, 0.9f, 1f,100);
                    vibrate(context,50);
                    view.setAlpha(1f);
                    return false;
            }
            return false;
        });
    }

    public static void animateViewBounce(View view) {

        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", -10f, 0f);
        animator.setDuration(150);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    public static void animateViewJiggle(View view) {

        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", -10f, 10f, 0f);
        animator.setDuration(150); // Slightly longer to account for both directions
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    public static void changeActivity(Activity fromActivity, Class<?> toActivity, boolean shouldFinish, boolean animate) {

        Intent intent = new Intent(fromActivity, toActivity);
        fromActivity.startActivity(intent);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        if (animate) fromActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        if (shouldFinish) fromActivity.finish();
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
}