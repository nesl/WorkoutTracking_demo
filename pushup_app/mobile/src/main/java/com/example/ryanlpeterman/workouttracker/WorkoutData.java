package com.example.ryanlpeterman.workouttracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Created by tahiyasalam on 8/3/15.
 */

public class WorkoutData {
    private static final String folderPath = "sdcard/buildsys15/";

    private String date;

    private float lying_time;
    private float running_time;
    private float sitting_time;
    private float walking_time;

    private float bench_time;
    private int bench_rep;

    private float squat_time;
    private int squat_rep;

    private float deadlift_time;
    private int deadlift_rep;

    private float pushup_time;
    private int pushup_rep;

    public String getDate() {
        return date;
    }

    public WorkoutData setDate(String date) {
        this.date = date;
        return this;
    }

    public float getLying_time() {
        return lying_time;
    }

    public WorkoutData setLying_time(float lying_time) {
        this.lying_time = lying_time;
        return this;
    }

    public float getRunning_time() {
        return running_time;
    }

    public WorkoutData setRunning_time(float running_time) {
        this.running_time = running_time;
        return this;
    }

    public float getSitting_time() {
        return sitting_time;
    }

    public WorkoutData setSitting_time(float sitting_time) {
        this.sitting_time = sitting_time;
        return this;
    }

    public float getWalking_time() {
        return walking_time;
    }

    public WorkoutData setWalking_time(float walking_time) {
        this.walking_time = walking_time;
        return this;
    }

    public float getBench_time() {
        return bench_time;
    }

    public WorkoutData setBench_time(float bench_time) {
        this.bench_time = bench_time;
        return this;
    }

    public int getBench_rep() {
        return bench_rep;
    }

    public WorkoutData setBench_rep(int bench_rep) {
        this.bench_rep = bench_rep;
        return this;
    }

    public float getSquat_time() {
        return squat_time;
    }

    public WorkoutData setSquat_time(float squat_time) {
        this.squat_time = squat_time;
        return this;
    }

    public int getSquat_rep() {
        return squat_rep;
    }

    public WorkoutData setSquat_rep(int squat_rep) {
        this.squat_rep = squat_rep;
        return this;
    }

    public float getDeadlift_time() {
        return deadlift_time;
    }

    public WorkoutData setDeadlift_time(float deadlift_time) {
        this.deadlift_time = deadlift_time;
        return this;
    }

    public int getDeadlift_rep() {
        return deadlift_rep;
    }

    public WorkoutData setDeadlift_rep(int deadlift_rep) {
        this.deadlift_rep = deadlift_rep;
        return this;
    }

    public float getPushup_time() {
        return pushup_time;
    }

    public WorkoutData setPushup_time(float pushup_time) {
        this.pushup_time = pushup_time;
        return this;
    }

    public int getPushup_rep() {
        return pushup_rep;
    }

    public WorkoutData setPushup_rep(int pushup_rep) {
        this.pushup_rep = pushup_rep;
        return this;
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

    public static void makeDataFolder() {
        try {
            new File(folderPath).mkdir();
        } catch (Exception e) {
        }
    }

    public void save() {
        save(true);
    }

    public void save(boolean override) {
        File file = new File(folderPath, date);
        if (!file.exists() || override) {
            try {
                PrintWriter writer = new PrintWriter(file);
                writer.print(
                        date + ","
                        + lying_time + ","
                        + running_time + ","
                        + sitting_time + ","
                        + walking_time + ","
                        + bench_time + ","
                        + bench_rep + ","
                        + squat_time + ","
                        + squat_rep + ","
                        + deadlift_time + ","
                        + deadlift_rep + ","
                        + pushup_time + ","
                        + pushup_rep
                );
                writer.close();
            } catch (Exception e) {
            }
        }
    }

    public static WorkoutData load(String date) {
        return load(date, true);
    }

    public static WorkoutData load(String date, boolean flagCreate) {
        /** Args:
         *    date: workout date
         *    flagCreate: When enabled, when the data is not existed, it generate a new one with
         *                all the counter initialized with zeros.
         */
        WorkoutData data = new WorkoutData();
        File file = new File(folderPath, date);
        if (!file.exists()) {
            if (flagCreate) {
                data.setDate(date);
                return data;
            } else {
                return null;
            }
        }
        try {
            InputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            String line = br.readLine();
            if (line == null)
                return null;
            String[] terms = line.split(",");
            if (terms.length != 13)
                return null;
            data.date = terms[0];
            data.lying_time = Float.parseFloat(terms[1]);
            data.running_time = Float.parseFloat(terms[2]);
            data.sitting_time = Float.parseFloat(terms[3]);
            data.walking_time = Float.parseFloat(terms[4]);
            data.bench_time = Float.parseFloat(terms[5]);
            data.bench_rep = Integer.parseInt(terms[6]);
            data.squat_time = Float.parseFloat(terms[7]);
            data.squat_rep = Integer.parseInt(terms[8]);
            data.deadlift_time = Float.parseFloat(terms[9]);
            data.deadlift_rep = Integer.parseInt(terms[10]);
            data.pushup_time = Float.parseFloat(terms[11]);
            data.pushup_rep = Integer.parseInt(terms[12]);

            br.close();
            isr.close();
            fis.close();

            return data;
        } catch (Exception e) {
        }
        return null;
    }
}
