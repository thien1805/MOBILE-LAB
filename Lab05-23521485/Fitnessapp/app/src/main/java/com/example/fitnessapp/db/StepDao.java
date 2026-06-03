package com.example.fitnessapp.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface StepDao {

    @Insert
    void insert(StepEntity step);

    @Query("SELECT * FROM steps ORDER BY timestamp DESC LIMIT 1")
    StepEntity getLatestStep();

    @Query("SELECT SUM(steps) FROM steps")
    int getTotalSteps();

    @Query("SELECT SUM(calories) FROM steps")
    double getTotalCalories();

    @Query("SELECT SUM(points) FROM steps")
    int getTotalPoints();

    @Query("SELECT * FROM steps ORDER BY timestamp DESC")
    List<StepEntity> getAllSteps();

    @Query("SELECT * FROM steps WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    List<StepEntity> getStepsBetween(long start, long end);

    @Query("DELETE FROM steps")
    void clearAll();
}
