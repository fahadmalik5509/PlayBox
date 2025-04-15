package com.fahadmalik5509.playbox.gpamanager;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = "semesters",
        foreignKeys = @ForeignKey(
                entity = Profile.class,
                parentColumns = "profileId",
                childColumns = "profile_id",
                onDelete = CASCADE
        ),
        indices = {@Index(value = "profile_id")}
)
public class Semester {

    @PrimaryKey(autoGenerate = true)
    private int semesterId;

    @ColumnInfo(name = "semester_name")
    private String semesterName;

    @ColumnInfo(name = "profile_id")
    private int profileId;

    public Semester(String semesterName, int profileId) {
        this.semesterName = semesterName;
        this.profileId = profileId;
    }

    public int getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(int semesterId) {
        this.semesterId = semesterId;
    }

    public String getSemesterName() {
        return semesterName;
    }

    public void setSemesterName(String semesterName) {
        this.semesterName = semesterName;
    }

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }
}
