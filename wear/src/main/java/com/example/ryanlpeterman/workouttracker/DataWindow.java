package com.example.ryanlpeterman.workouttracker;

import android.hardware.SensorEvent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Created by ryanlpeterman on 7/31/15.
 * Data Structure to encapsulate all data manipulation
 */
public class DataWindow {

    private int mCount;                 // Current num samples in array
    private int mCapacity;              // Max number of samples in window
    private SensorData[] mData;         // Reference to the Data

    // Constructor calculates count per window
    public DataWindow(int seconds, int frequency){

        mCapacity = seconds * frequency;

        mData = new SensorData[mCapacity];

        for(int i = 0; i < mCapacity; i++){
            mData[i] = new SensorData();
        }

    }
    // reset counting variable so we read in new data
    public void emptyBuffer(){
        mCount = 0;
    }

    public boolean isFull() {
        if (mCount < mCapacity)
            return false;
        else
            return true;
    }

    public boolean addSample(SensorEvent event) {
        return addSample(event.values[0], event.values[1], event.values[2]);
    }

    public boolean addSample(double x, double y, double z) {
        // Buffer is full data must be processed
        if (!(mCount < mCapacity))
            return false;

        // Store Sample
        mData[mCount].x_acc = x;
        mData[mCount].y_acc = y;
        mData[mCount].z_acc = z;

        // Increment Counter
        mCount++;

        return true;
    }

    public SensorData[] getData() {
        return mData;
    }

    public int getCount(){
        return mCount;
    }

    public byte[] compress() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream w = new DataOutputStream(baos);

        try {
            w.writeInt(mCount);
            w.writeInt(mCapacity);
            for (int i = 0; i < mCount; i++) {
                w.writeDouble(mData[i].x_acc);
                w.writeDouble(mData[i].y_acc);
                w.writeDouble(mData[i].z_acc);
            }
            w.flush();

            return baos.toByteArray();
        } catch (Exception e) {
        }
        return null;
    }

    public static DataWindow uncompress(byte[] data) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            int count = dis.readInt();
            int capacity = dis.readInt();
            DataWindow window = new DataWindow(1, capacity);
            for (int i = 0; i < count; i++)
                window.addSample(dis.readDouble(), dis.readDouble(), dis.readDouble());
            return window;
        } catch (Exception e) {
        }
        return null;
    }
}