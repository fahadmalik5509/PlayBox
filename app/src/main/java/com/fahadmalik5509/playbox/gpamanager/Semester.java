package com.fahadmalik5509.playbox.gpamanager;

public class Semester {

    private String semesterName;
    private int semesterNumber;

    public Semester(String semesterName, int semeesterNumber) {
        this.semesterName = semesterName;
        this.semesterNumber = semeesterNumber;
    }

    public String getSemesterName() {
        return semesterName;
    }

    public void setSemesterName(String semesterName) {
        this.semesterName = semesterName;
    }

    public int getSemesterNumber() {
        return semesterNumber;
    }

    public void setSemesterNumber(int semesterNumber) {
        this.semesterNumber = semesterNumber;
    }
}
