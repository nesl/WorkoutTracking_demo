package com.example.ryanlpeterman.workouttracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by tahiyasalam on 8/3/15.
 * Customized adapter to display fields of data class
 * Fields of data class represent activity completed
 */
public class MyAdapter extends ArrayAdapter<WorkoutData> {
    private ArrayList<WorkoutData> lData;

    private static LayoutInflater inflater = null;

    public MyAdapter(Context context, int resource,  ArrayList<WorkoutData> lData) {
        super(context, resource, lData);
        this.lData = lData;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if(v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.dataitem, null);
        }
        WorkoutData data = lData.get(position);

        if(data != null) {
            TextView date = (TextView) v.findViewById(R.id.date);

            TextView lying = (TextView) v.findViewById(R.id.lying_time);
            /*TextView running = (TextView) v.findViewById(R.id.running_time);
            TextView sitting = (TextView) v.findViewById(R.id.sitting_time);
            TextView walking = (TextView) v.findViewById(R.id.walking_time);

            TextView bench_time = (TextView) v.findViewById(R.id.bench_time);
            TextView deadlift_time = (TextView) v.findViewById(R.id.deadlift_time);
            TextView squat_time = (TextView) v.findViewById(R.id.squat_time);

            TextView bench_rep = (TextView) v.findViewById(R.id.bench_rep);
            TextView deadlift_rep = (TextView) v.findViewById(R.id.deadlift_rep);
            TextView squat_rep = (TextView) v.findViewById(R.id.squat_rep);

            //Displays all of the data for given time
            if(date != null) {
                date.setText("Date: " + data.getDate());
            }

            if(lying != null) {
                lying.setText("\t\tLying time: " + data.getLying_time() + " minutes");
            }

            if(running != null) {
                running.setText("\t\tRunning Time: " + data.getRunning_time() + " minutes");
            }

            if(sitting != null) {
                sitting.setText("\t\tSitting Time: " + data.getSitting_time() + " minutes");
            }

            if(walking != null) {
                walking.setText("\t\tWalking Time: " + data.getWalking_time() + " minutes");
            }

            if(bench_rep != null) {
                bench_rep.setText("\t\tBench Repetitions: " + data.getBench_rep() + " repetitions");
            }

            if(bench_time != null) {
                bench_time.setText("\t\tBench Time: " + data.getBench_time() + " minutes");
            }


            if(deadlift_rep != null) {
                deadlift_rep.setText("\t\tDeadlift Repetitions: " + data.getDeadlift_rep() + " repetitions");
            }

            if(deadlift_time != null) {
                deadlift_time.setText("\t\tDeadlift Time: " + data.getDeadlift_time() + " minutes");
            }

            if(squat_rep != null) {
                squat_rep.setText("\t\tSquat Repetitions: " + data.getSquat_rep() + " repetitions");
            }

            if(squat_time != null) {
                squat_time.setText("\t\tSquat Time: " + data.getSquat_time() + " minutes");
            }

*/

        }

        return v;

    }


    public int getCount() {
        return lData.size();
    }

    public long getItemId(int position) {
        return position;
    }


}
