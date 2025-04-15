package com.fahadmalik5509.playbox.gpamanager;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = "subjects",
        foreignKeys = @ForeignKey(
                entity = Semester.class,
                parentColumns = "semesterId",
                childColumns = "semester_id",
                onDelete = CASCADE
        ),
        indices = {@Index(value = "semester_id")}
)
public class Subject {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "subject_id")
    private int subjectId;

    @ColumnInfo(name = "subject_name")
    private String subjectName;

    @ColumnInfo(name = "total_marks")
    private int totalMarks;

    @ColumnInfo(name = "marks_gained")
    private int marksGained;

    @ColumnInfo(name = "credit_hours")
    private int creditHours;

    @ColumnInfo(name = "semester_id")
    private int semesterId; // Foreign key reference to Semester

    public Subject(String subjectName, int totalMarks, int marksGained, int creditHours) {
        this.subjectName = subjectName;
        this.totalMarks = totalMarks;
        this.marksGained = marksGained;
        this.creditHours = creditHours;
    }

    public Subject(String subjectName, int totalMarks, int marksGained, int creditHours, int semesterId) {
        this(subjectName, totalMarks, marksGained, creditHours);
        this.semesterId = semesterId;
    }

    // Required empty constructor for Room
    public Subject() {}

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(int totalMarks) {
        this.totalMarks = totalMarks;
    }

    public int getMarksGained() {
        return marksGained;
    }

    public void setMarksGained(int marksGained) {
        this.marksGained = marksGained;
    }

    public int getCreditHours() {
        return creditHours;
    }

    public void setCreditHours(int creditHours) {
        this.creditHours = creditHours;
    }

    public int getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(int semesterId) {
        this.semesterId = semesterId;
    }
}
