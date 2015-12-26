package edu.ucla.nesl.android.sixteenhours;

/**
 * Created by timestring on 12/24/15.
 */
public abstract class SensorDataProvider {
    protected ISensorDataReceiver subscriber;

    public SensorDataProvider(ISensorDataReceiver _subscriber) {
        subscriber = _subscriber;
    }

    protected abstract void invalidate();
}
