package com.example.ryanlpeterman.workouttracker;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by timestring on 4/21/15.
 */
public class MessageFromWearListenService extends WearableListenerService {
    private final String PATH_AGREEMENT_WITH_WEAR = "/message_path";

    public MessageFromWearListenService() {
        Log.i("WearListenerService", "constructor of ListenerService");
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.i("WearListenerService", "oh yeah got in onMessageReceived()");

        if (messageEvent.getPath().equals(PATH_AGREEMENT_WITH_WEAR)) {
            //Log.i("WearListenerService", "got message: " + message);

            // Broadcast message to wearable activity for display
            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra("sensor_data", messageEvent.getData());
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
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
