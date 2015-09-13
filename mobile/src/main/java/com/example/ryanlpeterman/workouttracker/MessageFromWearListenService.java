package com.example.ryanlpeterman.workouttracker;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by timestring on 4/21/15.
 */
public class MessageFromWearListenService extends WearableListenerService {
    private final String PATH_AGREEMENT_WITH_WEAR = "/message_path";

    // message type
    public static final int MESSAGE_TYPE_INERTIAL = 0;
    public static final int MESSAGE_TYPE_NUM_STEP = 1;

    public MessageFromWearListenService() {
        Log.i("WearListenerService", "constructor of ListenerService");
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.i("WearListenerService", "oh yeah got in onMessageReceived()");

        if (messageEvent.getPath().equals(PATH_AGREEMENT_WITH_WEAR)) {
            //Log.i("WearListenerService", "got message: " + message);

            byte[] bytes = messageEvent.getData();
            if ((int)bytes[0] == MESSAGE_TYPE_INERTIAL) {
                // Broadcast message to wearable activity for display
                Intent messageIntent = new Intent();
                messageIntent.setAction(Intent.ACTION_SEND);
                messageIntent.putExtra("sensor_data", messageEvent.getData());
                LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
            } else if ((int)bytes[0] == MESSAGE_TYPE_NUM_STEP) {
                SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
                Date today = new Date();
                String todayStr = formatter.format(today);
                try {
                    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                    DataInputStream dis = new DataInputStream(bais);
                    dis.readByte();  // discard message type
                    int count = dis.readInt();
                    float walkingTime = count * 0.011f;
                    WorkoutData data = WorkoutData.load(todayStr);
                    data.setWalking_time(data.getWalking_time() + walkingTime);
                    data.save();
                } catch (Exception e) {
                }
            }
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        System.out.println("Recevive message3");
    }
}
