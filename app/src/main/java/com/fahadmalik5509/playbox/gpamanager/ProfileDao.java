package com.fahadmalik5509.playbox.gpamanager;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ProfileDao {

    @Insert
    long insertProfile(Profile profile);

    @Update
    void updateProfile(Profile profile);

    @Delete
    void deleteProfile(Profile profile);

    @Query("SELECT * FROM profiles")
    List<Profile> getAllProfiles();

    @Query("SELECT * FROM profiles WHERE profileId = :id")
    Profile getProfileById(int id);
}
