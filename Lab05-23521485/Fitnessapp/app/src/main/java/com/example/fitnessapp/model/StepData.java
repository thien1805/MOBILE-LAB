package com.example.fitnessapp.model;

public class StepData {
    private int totalSteps;
    private double totalCalories;
    private int totalPoints;
    private String motivationalQuote;
    private int todaySteps;

    public StepData(int totalSteps, double totalCalories, int totalPoints) {
        this.totalSteps = totalSteps;
        this.totalCalories = totalCalories;
        this.totalPoints = totalPoints;
    }

    public int getTotalSteps() { return totalSteps; }
    public void setTotalSteps(int totalSteps) { this.totalSteps = totalSteps; }

    public double getTotalCalories() { return totalCalories; }
    public void setTotalCalories(double totalCalories) { this.totalCalories = totalCalories; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public String getMotivationalQuote() { return motivationalQuote; }
    public void setMotivationalQuote(String motivationalQuote) { this.motivationalQuote = motivationalQuote; }

    public int getTodaySteps() { return todaySteps; }
    public void setTodaySteps(int todaySteps) { this.todaySteps = todaySteps; }
}
