package com.fahadmalik5509.playbox.gpamanager;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SubjectDao {

    @Insert
    long insertSubject(Subject subject);

    @Update
    void updateSubject(Subject subject);

    @Delete
    void deleteSubject(Subject subject);

    @Query("SELECT * FROM subjects WHERE semester_id = :semesterId")
    List<Subject> getSubjectsForSemester(int semesterId);

    @Query("SELECT * FROM subjects WHERE subject_id = :id")
    Subject getSubjectById(int id);
}
