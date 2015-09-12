package com.example.ryanlpeterman.workouttracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by tahiyasalam on 8/3/15.
 */
public class SummaryFragment extends Fragment {
    public static final ArrayList<WorkoutData> events = new ArrayList<WorkoutData>();
    private ListView mListView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.activity_summary, container, false);
        mListView = (ListView) view.findViewById(R.id.listView);

        String date = null;
        WorkoutData eventClicked = null;

        if (getArguments() != null) {
            date = getArguments().getString("Date");
        }
        Log.i("GET_DATE", date);

        /*
        for (WorkoutData d : mEvents()) {
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
*/

        ArrayList<WorkoutData> selectedEvent = new ArrayList<WorkoutData>();
        selectedEvent.add(eventClicked);

        MyAdapter arrayAdapter = new MyAdapter(this.getActivity(), R.layout.dataitem, selectedEvent);
        mListView.setAdapter(arrayAdapter);

        return view;
    }

}
