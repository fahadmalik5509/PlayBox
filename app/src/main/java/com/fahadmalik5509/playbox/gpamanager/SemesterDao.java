package com.fahadmalik5509.playbox.gpamanager;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SemesterDao {

    @Insert
    long insertSemester(Semester semester);

    @Update
    void updateSemester(Semester semester);

    @Delete
    void deleteSemester(Semester semester);

    @Query("SELECT * FROM semesters WHERE profile_id = :profileId")
    List<Semester> getSemestersForProfile(int profileId);

    @Query("SELECT * FROM semesters WHERE semesterId = :id")
    Semester getSemesterById(int id);
}
