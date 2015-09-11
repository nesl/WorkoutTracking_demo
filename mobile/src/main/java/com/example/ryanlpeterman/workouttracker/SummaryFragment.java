package com.example.ryanlpeterman.workouttracker;

import android.support.v4.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by tahiyasalam on 8/3/15.
 */
public class SummaryFragment extends Fragment {
    public static final ArrayList<Data> events = new ArrayList<Data>();
    private ListView mListView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.activity_summary, container, false);
        mListView = (ListView) view.findViewById(R.id.listView);

        String date = null;
        Data eventClicked = null;

        if (getArguments() != null) {
            date = getArguments().getString("Date");
        }

        for (Data d : mEvents()) {
            if(d.getDate() != null && d.getDate().equals(date)) {
                eventClicked = d;
                Toast.makeText(getActivity().getApplicationContext(),
                        "Activity for date available",
                        Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getActivity().getApplicationContext(),
                        "Activity for date unavailable",
                        Toast.LENGTH_SHORT).show();
            }
        }

        ArrayList<Data> selectedEvent = new ArrayList<Data>();
        selectedEvent.add(eventClicked);

        MyAdapter arrayAdapter = new MyAdapter(this.getActivity(), R.layout.dataitem, selectedEvent);
        mListView.setAdapter(arrayAdapter);

        return view;
    }

    public ArrayList<Data> mEvents() {
        final SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");

        //Data structure for holding information related to activities/days

        //Dummy data
        Data newExercise = new Data();
        newExercise.setDate("03 Aug 2015");
        newExercise.setBench_rep(30);
        newExercise.setDeadlift_rep(30);
        newExercise.setSquat_rep(30);
        newExercise.setBench_time(12);
        newExercise.setDeadlift_time(8);
        newExercise.setSquat_time(13);
        newExercise.setLying_time(40);

        Data newExercise2 = new Data();
        newExercise2.setDate("05 Aug 2015");
        newExercise2.setBench_rep(45);
        newExercise2.setDeadlift_rep(45);
        newExercise2.setSquat_rep(45);
        newExercise2.setBench_time(20);
        newExercise2.setDeadlift_time(30);
        newExercise2.setSquat_time(20);
        newExercise2.setWalking_time(12);
        newExercise2.setRunning_time(20);

        Data newExercise3 = new Data();
        newExercise3.setDate("06 Aug 2015");
        newExercise3.setBench_rep(30);
        newExercise3.setDeadlift_rep(30);
        newExercise3.setSquat_rep(30);
        newExercise3.setBench_time(10);
        newExercise3.setDeadlift_time(10);
        newExercise3.setSquat_time(10);
        newExercise3.setWalking_time(90);
        newExercise3.setRunning_time(80);

        events.add(newExercise);
        events.add(newExercise2);
        events.add(newExercise3);

        return events;
    }


}
