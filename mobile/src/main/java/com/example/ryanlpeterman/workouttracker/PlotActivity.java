package com.example.ryanlpeterman.workouttracker;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import com.androidplot.util.PlotStatistics;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

public class PlotActivity extends AppCompatActivity implements SensorEventListener {
    private static final int HISTORY_SIZE = 50;
    private SensorManager sensorMgr = null;
    private Sensor gyroSensor = null;

    private XYPlot aprHistoryPlot = null;
    private CheckBox showFpsCb;
    private SimpleXYSeries xHistorySeries = null;
    private SimpleXYSeries yHistorySeries = null;
    private SimpleXYSeries zHistorySeries = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plot);

        // setup the APR History plot:
        aprHistoryPlot = (XYPlot) findViewById(R.id.aprHistoryPlot);

        xHistorySeries = new SimpleXYSeries("X");
        xHistorySeries.useImplicitXVals();
        yHistorySeries = new SimpleXYSeries("Y");
        yHistorySeries.useImplicitXVals();
        zHistorySeries = new SimpleXYSeries("Z");
        zHistorySeries.useImplicitXVals();

        aprHistoryPlot.setRangeBoundaries(-10, 10, BoundaryMode.FIXED);
        aprHistoryPlot.setDomainBoundaries(0, 50, BoundaryMode.FIXED);
        aprHistoryPlot.addSeries(xHistorySeries, new LineAndPointFormatter(Color.rgb(100, 100, 200), Color.rgb(100, 100, 200), null, null));
        aprHistoryPlot.addSeries(yHistorySeries, new LineAndPointFormatter(Color.rgb(100, 200, 100), Color.rgb(100, 200, 100), null, null));
        aprHistoryPlot.addSeries(zHistorySeries, new LineAndPointFormatter(Color.rgb(200, 100, 100), Color.rgb(200, 100, 100), null, null));
        aprHistoryPlot.setDomainStepValue(5);
        aprHistoryPlot.setTicksPerRangeLabel(3);
        aprHistoryPlot.setDomainLabel("Axis");
        aprHistoryPlot.getDomainLabelWidget().pack();
        aprHistoryPlot.setRangeLabel("Rad/s");
        aprHistoryPlot.getRangeLabelWidget().pack();
        aprHistoryPlot.setLayerType(View.LAYER_TYPE_NONE, null);

        // setup checkboxes:
        final PlotStatistics levelStats = new PlotStatistics(1000, false);
        final PlotStatistics histStats = new PlotStatistics(1000, false);
        aprHistoryPlot.addListener(histStats);

        showFpsCb = (CheckBox) findViewById(R.id.showFpsCb);
        showFpsCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                levelStats.setAnnotatePlotEnabled(b);
                histStats.setAnnotatePlotEnabled(b);
            }
        });

        // register for orientation sensor events:
        sensorMgr = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        gyroSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroSensor == null) {
            System.out.println("Failed to attach to gyroSensor.");
            cleanup();
        }

        sensorMgr.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_UI);
    }

    private void cleanup() {
        // unregister with the orientation sensor before exiting:
        sensorMgr.unregisterListener(this);
        finish();
    }

    // Called whenever a new gyroSensor reading is taken.
    @Override
    public synchronized void onSensorChanged(SensorEvent sensorEvent) {
        // get rid the oldest sample in history:
        if (zHistorySeries.size() > HISTORY_SIZE) {
            zHistorySeries.removeFirst();
            yHistorySeries.removeFirst();
            xHistorySeries.removeFirst();
        }

        // add the latest history sample:
        xHistorySeries.addLast(null, sensorEvent.values[0]);
        yHistorySeries.addLast(null, sensorEvent.values[1]);
        zHistorySeries.addLast(null, sensorEvent.values[2]);

        // redraw the Plots:
        aprHistoryPlot.redraw();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_plot, menu);
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
