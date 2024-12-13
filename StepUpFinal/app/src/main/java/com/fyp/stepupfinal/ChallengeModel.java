package com.fyp.stepupfinal;

public class ChallengeModel {
    private String Title;
    private int Points;

    private int Duration;
    private String Description;
    private int Timer;

    public ChallengeModel() {

    }

    public ChallengeModel(String title, int points, String description, int duration, int timer) {
        this.Title = title;
        this.Points = points;
        this.Description = description;
        this.Duration = duration;
        this.Timer = timer;
    }

    public String getDescription() {
        return Description;
    }
    public void setDescription(String description) {
        this.Description = description;
    }
    public int getDuration(){ return Duration; }
    public void setDuration(int duration){ this.Duration = duration; }
    public int getTimer(){return Timer;}
    public void setTimer(int timer){this.Timer = timer;}
    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        this.Title = title;
    }

    public int getPoints() {
        return Points;
    }

    public void setPoints(int points) {
        this.Points = points;
    }
}
