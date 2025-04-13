package com.fahadmalik5509.playbox.cgpacalculator;

public class Subject {

    private String subjectName;
    private int totalMarks;
    private int marksObtained;
    private int creditHours;
    private double gpa;

    public Subject(int marksObtained, String subjectName, int totalMarks, int creditHours, double gpa) {
        this.marksObtained = marksObtained;
        this.subjectName = subjectName;
        this.totalMarks = totalMarks;
        this.creditHours = creditHours;
        this.gpa = gpa;
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

    public int getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(int marksObtained) {
        this.marksObtained = marksObtained;
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
