package edu.ucla.nesl.android.sixteenhours;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
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

import java.util.ArrayList;

public class BGDataCollectionService extends Service implements SensorEventListener,
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "BGService";
    private final IBinder mBinder = new MyBinder();

    // Make everything static so that each bind will get
    // the same SensorManager, PrintWriter, and Wakelock
    private static SensorManager mSensorManager;
    private static ArrayList<Sensor> sensors = null;

    private static Vibrator vibrator;
    private static BGDataCollectionService mContext;

    private static PowerManager.WakeLock wakeLock;

    private static PacketMaker[] packetMakers = new PacketMaker[2];  // for looking-up

    private static final String PATH_AGREEMENT_WITH_PHONE = "/message_path";
    private static GoogleApiClient googleApiClient;

    private static AlarmReceiver alarm = new AlarmReceiver();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + ": " + intent);
        mContext = this;

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();

        return START_STICKY;
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // for recording the time offset
        int sensorType = sensorEvent.sensor.getType();
        int sensorMessageType;
        if (sensorType == Sensor.TYPE_ACCELEROMETER)
            sensorMessageType = 0;
        else if (sensorType == Sensor.TYPE_GYROSCOPE)
            sensorMessageType = 1;
        else
            return;

        PacketMaker curMaker = packetMakers[sensorMessageType];
        byte[] retMessage = curMaker.inputData(sensorEvent.timestamp, sensorEvent.values);

        if (retMessage != null) {
            Intent batteryIntent = getApplicationContext().registerReceiver(null,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int batteryLevel = batteryIntent.getIntExtra("level", -1);
            retMessage[1] = (byte) batteryLevel;
            Log.i("Report", "type=" + sensorMessageType + ", size=" + retMessage.length);
            new InertialDataTransferThread(retMessage).start();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public class MyBinder extends Binder {
        public BGDataCollectionService getService() {
            return BGDataCollectionService.this;
        }
    }

    public static void startRecording(String timestring) {
        Log.i(TAG, "start recording");

        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, "MyWakelook");
        wakeLock.acquire();

        sensors = new ArrayList<>();

        mSensorManager = ((SensorManager) mContext.getSystemService(SENSOR_SERVICE));
        //sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY));
        sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));

        for (int i = 0; i < packetMakers.length; i++)
            packetMakers[i] = new PacketMaker((byte) i);

        vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);

        //String folder_prefix = "/sdcard/wear_" + timestring + "_";

        // register sensors
        registerAllSensors();
        vibrator.vibrate(100);

        alarm.setAlarm(mContext);
    }

    public static void stopRecording() {
        Log.i(TAG, "stop recording");

        unregisterAllSensors();
        mSensorManager = null;

        sensors.clear();
        sensors = null;

        if (wakeLock != null) {
            wakeLock.release();
        }

        vibrator.vibrate(300);
    }

    private static void registerAllSensors() {
        for (Sensor sensor: sensors) {
            mSensorManager.registerListener(mContext, sensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    private static void unregisterAllSensors() {
        for (Sensor sensor: sensors) {
            mSensorManager.unregisterListener(mContext, sensor);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return mBinder;
    }

    @Override
    public void onDestroy () {
        super.onDestroy();
        if (mSensorManager != null) {
            stopRecording();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(googleApiClient, this);
        Log.d(TAG, "Wearable API is connected");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Wearable API is connection failed, code=" + connectionResult.getErrorCode());
        if (connectionResult.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
            Log.d(TAG,"Wearable API is unavailable");
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

    }

    private class InertialDataTransferThread extends Thread {
        private byte[] bytes;

        public InertialDataTransferThread(byte[] _bytes){
            bytes = _bytes;
        }

        @Override
        public void run() {
            //Log.i("Report", "begin to run");
            NodeApi.GetConnectedNodesResult nodes
                    = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
            //Log.i("Report", "being to send");
            for (Node node : nodes.getNodes()) {
                MessageApi.SendMessageResult result = Wearable.MessageApi
                        .sendMessage(googleApiClient, node.getId(),
                                PATH_AGREEMENT_WITH_PHONE, bytes)
                        .await();
                if (result.getStatus().isSuccess()) {
                    // TODO: pop out warning or something?
                }
                //Log.i("Report", "should sent");
            }
        }
    }
}
