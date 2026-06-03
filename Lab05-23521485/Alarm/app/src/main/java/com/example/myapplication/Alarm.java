package com.example.myapplication;

public class Alarm {

    private int hour;
    private int minute;
    private String label;
    private boolean[] days;

    public Alarm(
            int hour,
            int minute,
            String label,
            boolean[] days) {

        this.hour = hour;
        this.minute = minute;
        this.label = label;
        this.days = days;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public String getLabel() {
        return label;
    }

    public boolean[] getDays() {
        return days;
    }
}