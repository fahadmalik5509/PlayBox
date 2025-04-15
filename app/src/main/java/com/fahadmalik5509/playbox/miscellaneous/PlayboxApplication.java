package com.fahadmalik5509.playbox.miscellaneous;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.initializeSoundPool;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.loadColors;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.loadPreference;

import android.app.Application;
import android.content.Context;

import androidx.room.RoomDatabase;

import com.fahadmalik5509.playbox.gpamanager.AppDatabase;

public class PlayboxApplication extends Application {

    private static Context appContext;


    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;

        loadPreference(appContext);
        loadColors(appContext);
        initializeSoundPool(appContext);
    }

    public static Context getAppContext() {
        return appContext;
    }
}

