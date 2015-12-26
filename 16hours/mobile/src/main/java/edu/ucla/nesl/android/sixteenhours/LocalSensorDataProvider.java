package edu.ucla.nesl.android.sixteenhours;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by timestring on 12/24/15.
 */
public class LocalSensorDataProvider extends SensorDataProvider implements SensorEventListener {
    private final int BUFFER_SIZE = 50;

    private SensorManager sensorMgr;

    private long[][] timestamps = new long[2][BUFFER_SIZE];  // sensor type, buffer idx
    private float[][][] data = new float[2][BUFFER_SIZE][3];  // sensor type, buffer idx, xyz
    private int[] bufferLen = {0, 0};

    public LocalSensorDataProvider(ISensorDataReceiver _subscriber, Context appContext) {
        super(_subscriber);

        sensorMgr = (SensorManager) appContext.getSystemService(Context.SENSOR_SERVICE);
        Sensor accSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor gyroSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        sensorMgr.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_UI);
        sensorMgr.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void invalidate() {
        sensorMgr.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = 0;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            type = 0;
        else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
            type = 1;
        else
            return;

        int tlen = bufferLen[type];
        timestamps[type][tlen] = System.currentTimeMillis();
        data[type][tlen][0] = event.values[0];
        data[type][tlen][1] = event.values[1];
        data[type][tlen][2] = event.values[2];
        bufferLen[type]++;
        if (bufferLen[type] == BUFFER_SIZE) {
            subscriber.receiveData(type, timestamps[type], data[type]);
            bufferLen[type] = 0;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
