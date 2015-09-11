package com.example.ryanlpeterman.workouttracker;

/**
 * Created by ryanlpeterman on 7/29/15.
 */
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;


public class SensorService extends Service implements SensorEventListener, DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener   {
    // for use when debug logging
    private static final String TAG = MainActivity.class.getName();

    private final static int SENS_ACCELEROMETER = Sensor.TYPE_ACCELEROMETER;
    private final static int SENS_GYROSCOPE = Sensor.TYPE_GYROSCOPE;
    private final static int SENS_STEP_COUNTER = Sensor.TYPE_STEP_COUNTER;

    SensorManager mSensorManager;

    private PowerManager.WakeLock wakeLock;

    // Data Interface
    private static final int ACCEL_SAMPLE_FREQ = 50;       // 50 Hz
    private static final int LIFT_WINDOW = 1;              // 1 second
    private DataWindow liftWindow = new DataWindow(LIFT_WINDOW, ACCEL_SAMPLE_FREQ);

    // Used to vibrate the wearable
    private Vibrator vibrator;

    private BroadcastReceiver mBroadcastReceiver;

    public class PowerConnectionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            //Checks to see if phone is charging, sends measurements to phone if so
            //if (isCharging)
                //Log.i("Charging", "yes");
                //sendMeasurment();

        }
    }

    private final String PATH_AGREEMENT_WITH_PHONE = "/message_path";
    private GoogleApiClient googleApiClient;
    private int count = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        mBroadcastReceiver = new PowerConnectionReceiver();
        registerReceiver(mBroadcastReceiver, ifilter);

        googleApiClient = new GoogleApiClient.Builder(this)
                        .addApi(Wearable.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();

        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Sensor Dashboard");
        builder.setContentText("Collecting sensor data..");
        // TODO: Make Icon
        // builder.setSmallIcon(R.drawable.ic_launcher);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // unique ID (1) and notification
        startForeground(1, builder.build());

        startMeasurement();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);

        stopMeasurement();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(googleApiClient, this);
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
            Log.d(TAG,"Wearable API is unavailable");
        }
    }

    public void onConnectionSuspended(int cause) { }

    public void onDataChanged(DataEventBuffer dataEvents) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void startMeasurement() {
        Log.d(TAG, "start measurement in wear: SensorService");

       // vibrator.vibrate(1000);

        // Wakelock
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SensorCollector");
        wakeLock.acquire();

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));

        Sensor accelerometerSensor = mSensorManager.getDefaultSensor(SENS_ACCELEROMETER);
        Sensor gyroscopeSensor = mSensorManager.getDefaultSensor(SENS_GYROSCOPE);
        Sensor stepCounterSensor = mSensorManager.getDefaultSensor(SENS_STEP_COUNTER);

        // Register the listener
        if (mSensorManager != null) {
            if (accelerometerSensor != null) {
                mSensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
            } else {
                Log.w(TAG, "No Accel found");
            }
            if (gyroscopeSensor != null) {
                mSensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);
            } else {
                Log.w(TAG, "No Gyro Sensor found");
            }
            if (stepCounterSensor != null) {
                mSensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_FASTEST);
            } else {
                Log.d(TAG, "No Step Count found");
            }
        }
    }

    private void stopMeasurement() {
        vibrator.vibrate(200);

        mSensorManager.unregisterListener(this);
        mSensorManager = null;

        wakeLock.release();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        int type = event.sensor.getType();
        //long timestamp = event.timestamp;

        if (type == SENS_ACCELEROMETER) {

            // if sample could not be added because buffer is full
            if(!liftWindow.addSample(event)){
                // Process Data

                new DataTransferThread(liftWindow.compress()).start();
                liftWindow.emptyBuffer();
            }
        }
    }

    // Class to Encapsulate Feature Calculation once Data is collected from sensors
    private class DataTransferThread extends Thread {
        private byte[] bytes;

        public DataTransferThread(byte[] _bytes){
            bytes = _bytes;
        }

        @Override
        public void run() {
            NodeApi.GetConnectedNodesResult nodes
                    = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
            for (Node node : nodes.getNodes()) {
                MessageApi.SendMessageResult result = Wearable.MessageApi
                        .sendMessage(googleApiClient, node.getId(),
                                     PATH_AGREEMENT_WITH_PHONE, bytes)
                        .await();
                if (result.getStatus().isSuccess()) {
                    // TODO: pop out warning or something?
                }
            }
        }
    }

    // Placeholder
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
