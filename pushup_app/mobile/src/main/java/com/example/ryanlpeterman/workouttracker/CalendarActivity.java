package com.example.ryanlpeterman.workouttracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by tahiyasalam on 7/30/15.
 */
public class CalendarActivity extends AppCompatActivity {
    private final SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        final CaldroidFragment caldroidFragment = new CaldroidFragment();
        Bundle args = new Bundle();

        Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.THEME_RESOURCE, com.caldroid.R.style.CaldroidDefaultDark);
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        caldroidFragment.setArguments(args);

        final FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendar1, caldroidFragment);

        final CaldroidListener listener = new CaldroidListener() {

            @Override
            public void onSelectDate(Date date, View view) {
                Intent intent = new Intent(CalendarActivity.this, WorkoutActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("date", formatter.format(date));
                intent.putExtras(bundle);
                startActivity(intent);
            }

            @Override
            public void onChangeMonth(int month, int year) {

            }
        };
        caldroidFragment.setCaldroidListener(listener);
        t.commit();
    }


}
