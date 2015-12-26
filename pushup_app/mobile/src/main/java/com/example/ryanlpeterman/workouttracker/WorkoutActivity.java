package com.example.ryanlpeterman.workouttracker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class WorkoutActivity extends AppCompatActivity {
    private String date;
    private WorkoutData data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        date = getIntent().getExtras().getString("date");
        data = WorkoutData.load(date);

        ((TextView) findViewById(R.id.date)).setText(date);
        //((TextView) findViewById(R.id.lying_time)).setText(
        //        String.format("Lying %.1f mins", data.getLying_time()));
        //((TextView) findViewById(R.id.sitting_time)).setText(
        //        String.format("Sitting %.1f mins", data.getSitting_time()));
        ((TextView) findViewById(R.id.running_time)).setText(
                String.format("Running %.1f mins", data.getRunning_time()));
        ((TextView) findViewById(R.id.walking_time)).setText(
                String.format("Walking %.1f mins", data.getWalking_time()));
        ((TextView) findViewById(R.id.bench_time_rep)).setText(
                String.format("Bench press %.1f mins, %d reps", data.getBench_time(), data.getBench_rep()));
        ((TextView) findViewById(R.id.deadlift_time_rep)).setText(
                String.format("Deadlift %.1f mins, %d reps", data.getDeadlift_time(), data.getDeadlift_rep()));
        ((TextView) findViewById(R.id.squat_time_rep)).setText(
                String.format("Squat %.1f mins, %d reps", data.getSquat_time(), data.getSquat_rep()));
        ((TextView) findViewById(R.id.pushup_rep)).setText(
                String.format("Pushup %d reps", data.getPushup_rep()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_workout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
