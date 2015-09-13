package com.example.ryanlpeterman.workouttracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import java.util.ArrayList;

public class PlotActivity extends AppCompatActivity implements SensorEventListener {
    private static final int HISTORY_SIZE = 250;
    private SensorManager sensorMgr = null;
    private Sensor gyroSensor = null;

    private XYPlot gyroPlot = null;
    private CheckBox showFpsCb;
    private SimpleXYSeries xHistorySeries = null;
    private SimpleXYSeries yHistorySeries = null;
    private SimpleXYSeries zHistorySeries = null;

    private ArrayList<SensorData> sensorDataBuffer = new ArrayList<>();

    private Handler addSampleHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plot);

        // setup the APR History plot:
        gyroPlot = (XYPlot) findViewById(R.id.sensorPlot);
        gyroPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);

        xHistorySeries = new SimpleXYSeries("X");
        xHistorySeries.useImplicitXVals();
        yHistorySeries = new SimpleXYSeries("Y");
        yHistorySeries.useImplicitXVals();
        zHistorySeries = new SimpleXYSeries("Z");
        zHistorySeries.useImplicitXVals();

        gyroPlot.setRangeBoundaries(-10, 10, BoundaryMode.FIXED);
        gyroPlot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.FIXED);
        gyroPlot.addSeries(xHistorySeries, new LineAndPointFormatter(
                Color.rgb(100, 100, 200), Color.rgb(100, 100, 200), null, null));
        gyroPlot.addSeries(yHistorySeries, new LineAndPointFormatter(
                Color.rgb(100, 200, 100), Color.rgb(100, 200, 100), null, null));
        gyroPlot.addSeries(zHistorySeries, new LineAndPointFormatter(
                Color.rgb(200, 100, 100), Color.rgb(200, 100, 100), null, null));
        gyroPlot.setDomainStepValue(6);
        gyroPlot.setTicksPerRangeLabel(3);
        gyroPlot.setDomainLabel("Axis");
        gyroPlot.getDomainLabelWidget().pack();
        gyroPlot.setRangeLabel("Rad/s");
        gyroPlot.getRangeLabelWidget().pack();
        gyroPlot.setLayerType(View.LAYER_TYPE_SOFTWARE, null);


        // setup checkboxes:
        final PlotStatistics levelStats = new PlotStatistics(1000, false);
        final PlotStatistics histStats = new PlotStatistics(1000, false);
        gyroPlot.addListener(histStats);

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

        // setup google client to receive information from wear
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);

        // execute adding sample proxy
        addSampleHandler.postDelayed(new DisplayOneSample(), 20L);
    }

    private boolean stopFlag = false;

    @Override
    public void onStop() {
        stopFlag = true;
    }

    private void cleanup() {
        // unregister with the orientation sensor before exiting:
        sensorMgr.unregisterListener(this);
        finish();
    }

    // Called whenever a new gyroSensor reading is taken.
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //addSampleOnPlot(sensorEvent.values[0], /* x-value */
        //                sensorEvent.values[1], /* y-value */
        //                sensorEvent.values[2]  /* z-value */);
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

    public void addSampleOnPlot(float x, float y, float z) {
        // get rid the oldest sample in history:
        if (zHistorySeries.size() > HISTORY_SIZE) {
            zHistorySeries.removeFirst();
            yHistorySeries.removeFirst();
            xHistorySeries.removeFirst();
        }

        // add the latest history sample:
        xHistorySeries.addLast(null, x);
        yHistorySeries.addLast(null, y);
        zHistorySeries.addLast(null, z);

        // redraw the Plots:
        gyroPlot.redraw();

        // push-up detection
        detectPushUp();
    }

    private void detectPushUp() {
        if (xHistorySeries.size() >= 30) {
            float[] winGyroZ = new float[30];
            float[] winGyroMag = new float[30];
            int sidx = xHistorySeries.size() - 30;
            for (int i = 0; i < 30; i++) {
                float tx = xHistorySeries.getY(sidx + i).floatValue();
                float ty = yHistorySeries.getY(sidx + i).floatValue();
                float tz = zHistorySeries.getY(sidx + i).floatValue();
                winGyroZ[i] = tz;
                winGyroMag[i] = (float)Math.sqrt(tx * tx + ty * ty + tz * tz);
            }
/*
            for (int j = 5; j < 20; j++) {
                if winGyroZ[j] == max(win_gyro_z((j-2):(j+2)))
            %fprintf('get j=%d\n', j);
            for k = (j+1):(W-5)
            if win_gyro_mag(k) == min(win_gyro_mag((k-2):(k+2))) ...
            && win_gyro_mag(k) <= 0.8
                    %fprintf('get j=%d k=%d\n', j, k);
            for l = (k+1):(W-2)
            if win_gyro_z(l) == min(win_gyro_z((l-2):(l+2))) ...
            && 6 < l - j && l - j < 15 ...
            && 1.3 <= win_gyro_z(j) && win_gyro_z(j) <= 4 ...
            && -4 <= win_gyro_z(l) && win_gyro_z(l) <= -1
            cur_center = i+k-1;
            if cur_center ~= last_center_point
                    %push_up_filter = 'o';
            sec = gyro(i+k-1, 1);
            acc_idx = find(acc(:, 1) > sec);
            acc_idx = acc_idx(1);
            PHW = 30;
            cnt = sum(downward_deg((acc_idx-PHW):(acc_idx+PHW)) <= 20);
            ratio = cnt / (PHW * 2 + 1);
            pass = 'o';
            if ratio < 0.7
            pass = '.';
            end
            fprintf('i=%d, j=%d, k=%d, l=%d (%c)\n', ...
            i, i+j-1, i+k-1, i+l-1, pass);
            last_center_point = cur_center;
            end
                    j = W;
            k = W;
            l = W;
            end
                    end
            end
                    end
            end
                    end*/
        }
    }
    private float winMax(float[] arr, int sidx, int eidx) {
        float maxVal = arr[sidx];
        for (int i = sidx + 1; i < eidx; i++)
            if (arr[i] > maxVal)
                maxVal = arr[i];
        return maxVal;
    }

    private float winMin(float[] arr, int sidx, int eidx) {
        float minVal = arr[sidx];
        for (int i = sidx + 1; i < eidx; i++)
            if (arr[i] > minVal)
                minVal = arr[i];
        return minVal;
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] bytes = intent.getByteArrayExtra("sensor_data");

            Log.i("CRAZY_READ", "msg=" + bytes.length);

            DataWindow window = DataWindow.uncompress(bytes);

            /*for (int i = 0; i < window.getCount(); i++) {
                SensorData data = window.getData()[i];
                addSampleOnPlot(data.x, data.y, data.z);

                // TODO: add sample every 20 ms
            }*/

            synchronized (sensorDataBuffer) {
                for (int i = 0; i < window.getCount(); i++)
                    sensorDataBuffer.add(window.getData()[i]);
            }
        }
    }

    private static final int MAX_NUM_SAMPLES_TO_PLOT = 15;
    private long lastTimeAddSample = System.currentTimeMillis();
    private class DisplayOneSample implements Runnable {
        @Override
        public void run() {
            long now = System.currentTimeMillis();
            int numSamplesToAdd = (int)((now - lastTimeAddSample) / 20);
            if (numSamplesToAdd > MAX_NUM_SAMPLES_TO_PLOT) {
                lastTimeAddSample = now;
                numSamplesToAdd = MAX_NUM_SAMPLES_TO_PLOT;
            }
            else {
                lastTimeAddSample += numSamplesToAdd * 20;
            }
            synchronized (sensorDataBuffer) {
                while (numSamplesToAdd > 0) {
                    if (sensorDataBuffer.size() > 0) {
                        SensorData data = sensorDataBuffer.get(0);
                        sensorDataBuffer.remove(0);
                        addSampleOnPlot(data.x, data.y, data.z);
                    }
                    numSamplesToAdd--;
                }
            }

            if (!stopFlag)
                addSampleHandler.postDelayed(new DisplayOneSample(), 20L);
        }
    }
}
