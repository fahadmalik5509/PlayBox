package com.fahadmalik5509.playbox.miscellaneous;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.initializeSoundPool;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.loadColors;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.loadPreference;

import android.app.Application;

public class PlayboxApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        loadPreference(this);
        loadColors(this);
        initializeSoundPool(this);
    }
}
