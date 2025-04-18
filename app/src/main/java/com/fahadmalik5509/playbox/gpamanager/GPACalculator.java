package com.fahadmalik5509.playbox.gpamanager;

import java.util.List;

public class GPACalculator {

//    public static String getCumulativeGPAStringFromSemesters(List<Semester> semesters) {
//        if (semesters == null || semesters.size() == 0) return "0.0";
//
//        double total = 0.0;
//        for (double sgpa : sgpas) {
//            total += sgpa;
//        }
//        return total / sgpas.size();  // Simple average of all SGPAs
//    }

    public static String getSemesterGPAStringFromSubjects(List<Subject> subjects) {


        double totalQualityPoints = 0;
        int totalCreditHours = 0;

        for (Subject subject : subjects) {
            int creditHours = subject.getCreditHours();
            double percentage = calculatePercentage(subject.getMarksGained(), subject.getTotalMarks());
            double gradePoint = getSubjectGPADoubleFromPercentage(percentage);

            totalQualityPoints += gradePoint * creditHours;
            totalCreditHours += creditHours;
        }

        if (totalCreditHours == 0) return "0.0";


        return String.valueOf(totalQualityPoints / totalCreditHours).replace('.', ',');
    }

    public static Double getSemesterGPADoubleFromSubjects(List<Subject> subjects) {
        double totalQualityPoints = 0;
        int totalCreditHours = 0;

        for (Subject subject : subjects) {
            int creditHours = subject.getCreditHours();
            double percentage = calculatePercentage(subject.getMarksGained(), subject.getTotalMarks());
            double gradePoint = getSubjectGPADoubleFromPercentage(percentage);

            totalQualityPoints += gradePoint * creditHours;
            totalCreditHours += creditHours;
        }

        if (totalCreditHours == 0) return 0.0;

        return totalQualityPoints / totalCreditHours;
    }

    // You can adjust this scale according to your university’s policy
    public static String getSubjectGPAStringFromPercentage(int percentage) {
        if (percentage >= 85) return "4,0";
        else if (percentage >= 80) return "3,75";
        else if (percentage >= 75) return "3,5";
        else if (percentage >= 70) return "3,0";
        else if (percentage >= 65) return "2,5";
        else if (percentage >= 60) return "2,0";
        else if (percentage >= 55) return "1,5";
        else if (percentage >= 50) return "1,0";
        else return "0,0";
    }

    // You can adjust this scale according to your university’s policy
    private static double getSubjectGPADoubleFromPercentage(double percentage) {
        if (percentage >= 85) return 4.0;
        else if (percentage >= 80) return 3.75;
        else if (percentage >= 75) return 3.5;
        else if (percentage >= 70) return 3.0;
        else if (percentage >= 65) return 2.5;
        else if (percentage >= 60) return 2.0;
        else if (percentage >= 55) return 1.5;
        else if (percentage >= 50) return 1.0;
        else return 0.0;
    }

    public static double calculatePercentage(int totalMarks, int marksGained) {
        return (double) marksGained / totalMarks * 100;
    }

}
