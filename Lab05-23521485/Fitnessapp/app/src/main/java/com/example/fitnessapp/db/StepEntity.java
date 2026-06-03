package com.example.fitnessapp.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "steps")
public class StepEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "steps")
    private int steps;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    @ColumnInfo(name = "calories")
    private double calories;

    @ColumnInfo(name = "points")
    private int points;

    public StepEntity(int steps, long timestamp, double calories, int points) {
        this.steps = steps;
        this.timestamp = timestamp;
        this.calories = calories;
        this.points = points;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSteps() { return steps; }
    public void setSteps(int steps) { this.steps = steps; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getCalories() { return calories; }
    public void setCalories(double calories) { this.calories = calories; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
}
