package com.example.ryanlpeterman.workouttracker;

/**
 * Created by tahiyasalam on 8/3/15.
 */

public class Data {
    private String date;

    private float lying_time;
    private float running_time;
    private float sitting_time;
    private float walking_time;

    private float bench_time;
    private float squat_time;
    private float deadlift_time;

    private int bench_rep;
    private int squat_rep;
    private int deadlift_rep;


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getLying_time() {
        return lying_time;
    }

    public void setLying_time(float lying_time) {
        this.lying_time = lying_time;
    }

    public float getRunning_time() {
        return running_time;
    }

    public void setRunning_time(float running_time) {
        this.running_time = running_time;
    }

    public float getSitting_time() {
        return sitting_time;
    }

    public void setSitting_time(float sitting_time) {
        this.sitting_time = sitting_time;
    }

    public float getWalking_time() {
        return walking_time;
    }

    public void setWalking_time(float walking_time) {
        this.walking_time = walking_time;
    }

    public float getBench_time() {
        return bench_time;
    }

    public void setBench_time(float bench_time) {
        this.bench_time = bench_time;
    }

    public float getSquat_time() {
        return squat_time;
    }

    public void setSquat_time(float squat_time) {
        this.squat_time = squat_time;
    }

    public float getDeadlift_time() {
        return deadlift_time;
    }

    public void setDeadlift_time(float deadlift_time) {
        this.deadlift_time = deadlift_time;
    }

    public int getBench_rep() {
        return bench_rep;
    }

    public void setBench_rep(int bench_rep) {
        this.bench_rep = bench_rep;
    }

    public int getSquat_rep() {
        return squat_rep;
    }

    public void setSquat_rep(int squat_rep) {
        this.squat_rep = squat_rep;
    }

    public int getDeadlift_rep() {
        return deadlift_rep;
    }

    public void setDeadlift_rep(int deadlift_rep) {
        this.deadlift_rep = deadlift_rep;
    }

    public float getCardioTotal_time() {
        return (getRunning_time() + getWalking_time());
    }

    public float getWeightlightingTotal_time() {
        return (getBench_time() + getDeadlift_time() + getSquat_time());
    }

    public float getSedentaryTotal_time() {
        return (getLying_time() + getSitting_time());
    }

}
