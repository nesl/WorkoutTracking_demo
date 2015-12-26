package edu.ucla.nesl.android.sixteenhours;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

/**
 * Created by timestring on 12/25/15.
 */
public class WearSensorDataProvider extends SensorDataProvider {

    public WearSensorDataProvider(ISensorDataReceiver _subscriber, Context appContext) {
        super(_subscriber);

        // setup google client to receiveData information from wear
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(appContext).registerReceiver(messageReceiver, messageFilter);
    }

    @Override
    protected void invalidate() {

    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] bytes = intent.getByteArrayExtra("sensor_data");

            Log.i("broadcastRec", "msg=" + bytes.length);

            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                DataInputStream dis = new DataInputStream(bais);
                int sensorType = (int) dis.readByte();
                if (sensorType < 0 || sensorType >= 2)
                    return;

                int batteryLevel = (int) dis.readByte();
                int numSamples = (int) dis.readShort();
                long baseTime = dis.readLong();
                long timestamps[] = new long[numSamples];
                float data[][] = new float[numSamples][3];
                for (int i = 0; i < numSamples; i++) {
                    timestamps[i] = dis.readInt() + baseTime;
                    data[i][0] = dis.readFloat();
                    data[i][1] = dis.readFloat();
                    data[i][2] = dis.readFloat();
                }
                subscriber.receiveData(sensorType, timestamps, data);
                subscriber.receiveBatteryLevel(batteryLevel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
