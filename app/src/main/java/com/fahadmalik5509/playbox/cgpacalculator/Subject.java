package com.fahadmalik5509.playbox.cgpacalculator;

public class Subject {

    private String subjectName;
    private int totalMarks;
    private int marksGained;
    private int creditHours;
    private double gpa;

    public Subject(String subjectName, int totalMarks, int marksGained, int creditHours) {
        this.subjectName = subjectName;
        this.marksGained = marksGained;
        this.totalMarks = totalMarks;
        this.creditHours = creditHours;
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

    public double getGpa() {
        return gpa;
    }
}
