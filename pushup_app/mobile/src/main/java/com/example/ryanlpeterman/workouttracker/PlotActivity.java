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
import android.widget.TextView;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XValueMarker;
import com.androidplot.xy.XYPlot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

public class PlotActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "PlotActivity";
    private static final int HISTORY_SIZE = 250;
    private SensorManager sensorMgr = null;
    private Sensor gyroSensor = null;

    private XYPlot gyroPlot = null;
    private TextView countTextView;
    private SimpleXYSeries xHistorySeries = null;
    private SimpleXYSeries yHistorySeries = null;
    private SimpleXYSeries zHistorySeries = null;
    private int seriesOffset = 0;

    private ArrayList<SensorData> sensorDataBuffer = new ArrayList<>();

    private Handler addSampleHandler = new Handler();

    private final SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
    private final Date date = new Date();
    private final String dateStr = formatter.format(date);

    // List to store all the markers for push-up detection
    private LinkedList<XValueMarker> markerList = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plot);

        // setup the APR History plot:
        gyroPlot = (XYPlot) findViewById(R.id.sensorPlot);
        gyroPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
        gyroPlot.getGraphWidget().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        gyroPlot.getGraphWidget().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
        gyroPlot.getGraphWidget().getRangeSubGridLinePaint().setColor(Color.TRANSPARENT);

        xHistorySeries = new SimpleXYSeries("X");
        xHistorySeries.useImplicitXVals();
        yHistorySeries = new SimpleXYSeries("Y");
        yHistorySeries.useImplicitXVals();
        zHistorySeries = new SimpleXYSeries("Z");
        zHistorySeries.useImplicitXVals();

        gyroPlot.setRangeBoundaries(-20, 20, BoundaryMode.FIXED);
        gyroPlot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.FIXED);
        gyroPlot.addSeries(xHistorySeries, new LineAndPointFormatter(
                Color.rgb(100, 100, 200), Color.rgb(100, 100, 200), null, null));
        gyroPlot.addSeries(yHistorySeries, new LineAndPointFormatter(
                Color.rgb(100, 200, 100), Color.rgb(100, 200, 100), null, null));
        gyroPlot.addSeries(zHistorySeries, new LineAndPointFormatter(
                Color.rgb(200, 100, 100), Color.rgb(200, 100, 100), null, null));
        gyroPlot.setDomainStepValue(6);
        gyroPlot.setRangeStepValue(5);
        gyroPlot.setDomainLabel("Axis");
        gyroPlot.getDomainLabelWidget().pack();
        gyroPlot.setRangeLabel("Rad/s");
        gyroPlot.getRangeLabelWidget().pack();
        gyroPlot.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        countTextView = (TextView) findViewById(R.id.countTextView);

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
        super.onStop();
        stopFlag = true;
    }

    private void cleanup() {
        // unregister with the orientation sensor before exiting:
        sensorMgr.unregisterListener(this);
        finish();
    }

    // Called whenever a new gyroSensor reading is taken.
    // Use the phone sensor data only for testing purposes
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
            seriesOffset++;

            // Adjust the x value of markers while plot moves
            for (XValueMarker marker : markerList) {
                gyroPlot.removeMarker(marker);
                float prevVal = marker.getValue().floatValue();
                if (prevVal > 1) {
                    marker.setValue(prevVal - 1);
                    gyroPlot.addMarker(marker);
                }
            }

            // Remove all markers with a negative x value
            while (markerList.peekFirst() != null && markerList.peekFirst().getValue().floatValue() <= 0) {
                XValueMarker marker = markerList.pollFirst();
                gyroPlot.removeMarker(marker);
                Log.i(TAG, "Removed push up marker, list size=" + markerList.size());
            }
        }

        // add the latest history sample:
        xHistorySeries.addLast(null, x);
        yHistorySeries.addLast(null, y);
        zHistorySeries.addLast(null, z);

        //Log.i("SERIES", "zHis" + zHistorySeries.getY(zHistorySeries.size()-1).floatValue() + ", sensorZ=" + z);

        // redraw the Plots:
        gyroPlot.redraw();

        // push-up detection
        detectPushUp();
    }

    private int lastPushUpCenterSampleIdx = 0;
    private static int countPushUp = 0;

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

            for (int j = 5; j < 20; j++) {
                if (winGyroZ[j] == winMax(winGyroZ, j-2, j+3)) {
                    //Log.i("PUSH_UP", "first:j=" + j);
                    for (int k = j + 1; k < 25; k++) {
                        if (winGyroMag[k] == winMin(winGyroMag, k - 2, k + 3)
                                && winGyroMag[k] <= 0.8) {
                            //Log.i("PUSH_UP", "second:j=" + j + ", k=" + k);
                            for (int l = k + 1; l < 28; l++) {
                                //Log.i("PUSH_UP", "l=" + l + ", val=" + winGyroZ[l]);
                                if (winGyroZ[l] == winMin(winGyroZ, l - 2, l + 3)
                                        && 6 < l - j && l - j < 25
                                        && 1.3 <= winGyroZ[j] && winGyroZ[j] <= 4.0
                                        && -4.0 <= winGyroZ[l] && winGyroZ[l] <= -1.0) {
                                    //Log.i("PUSH_UP", "final:j=" + j + ", k=" + k + ", l=" + l);
                                    int curCenter = seriesOffset + sidx + k;
                                    int curHighPeak = seriesOffset + sidx + j;
                                    if (curCenter != lastPushUpCenterSampleIdx) {
                                        //%push_up_filter = 'o';
                                        //sec = gyro(i+k-1, 1);
                                        //acc_idx = find(acc(:, 1) > sec);
                                        //acc_idx = acc_idx(1);
                                        //PHW = 30;
                                        //cnt = sum(downward_deg((acc_idx-PHW):(acc_idx+PHW)) <= 20);
                                        //ratio = cnt / (PHW * 2 + 1);
                                        //pass = 'o';
                                        //if ratio < 0.7
                                        //pass = '.';
                                        //end
                                        //fprintf('i=%d, j=%d, k=%d, l=%d (%c)\n', ...
                                        //i, i+j-1, i+k-1, i+l-1, pass);
                                        if (curCenter > lastPushUpCenterSampleIdx + 40) {
                                            Log.i("PUSH_UP", "push up detected, curCenter" + curCenter + ", (curCenter - offset)" + (curCenter - seriesOffset));
                                            lastPushUpCenterSampleIdx = curCenter;

                                            // Update the counter
                                            countPushUp++;
                                            countTextView.setText("Push-up: " + countPushUp);

                                            // Add a marker for this push up on the plot
                                            // TODO: Please change this value to more accurately mark the push-ups
                                            XValueMarker marker = new XValueMarker(curHighPeak - seriesOffset, "Push-up");
                                            marker.getTextPaint().setColor(Color.BLACK);
                                            marker.getTextPaint().setTextSize(50);
                                            markerList.offerLast(marker);
                                            gyroPlot.addMarker(marker);
                                            gyroPlot.redraw();

                                            WorkoutData workoutData = WorkoutData.load(dateStr);
                                            workoutData.setPushup_rep(workoutData.getPushup_rep() + 1);
                                            workoutData.save();
                                        }
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
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
            if (arr[i] < minVal)
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
