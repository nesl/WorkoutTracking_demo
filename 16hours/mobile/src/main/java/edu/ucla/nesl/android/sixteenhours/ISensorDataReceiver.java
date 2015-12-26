package edu.ucla.nesl.android.sixteenhours;

/**
 * Created by timestring on 12/24/15.
 */
public interface ISensorDataReceiver {
    void receiveData(int type, long[] timestamps, float[][] data);
    void receiveBatteryLevel(int level);
}
