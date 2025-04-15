package com.fahadmalik5509.playbox.gpamanager;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {Profile.class, Semester.class, Subject.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ProfileDao profileDao();
    public abstract SemesterDao semesterDao();
    public abstract SubjectDao subjectDao();

    private static volatile AppDatabase INSTANCE;

    // Singleton pattern to ensure only one instance of the database is created
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "gpa_manager_database")
                            .fallbackToDestructiveMigration() // Optional, depends on whether you're handling migrations
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
