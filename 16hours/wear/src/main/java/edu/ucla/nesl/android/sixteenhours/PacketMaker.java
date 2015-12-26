package edu.ucla.nesl.android.sixteenhours;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

/**
 * Created by timestring on 12/25/15.
 */
public class PacketMaker {
    private final int BUFFER_SIZE = 50;

    private byte type;

    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private DataOutputStream dos = new DataOutputStream(baos);

    private int numSamples = 0;
    private long baseSensorTimestamp;

    public PacketMaker(byte _type) {
        type = _type;
    }

    public byte[] inputData(long time, float[] data) {
        try {
            if (numSamples == 0) {
                dos.writeByte(type);
                dos.writeByte(0);
                dos.writeShort(BUFFER_SIZE);
                dos.writeLong(System.currentTimeMillis());
                baseSensorTimestamp = time;
            }

            dos.writeInt((int) ((time - baseSensorTimestamp) / 1000000));
            dos.writeFloat(data[0]);
            dos.writeFloat(data[1]);
            dos.writeFloat(data[2]);
            numSamples++;

            byte[] retBytes = null;
            if (numSamples == BUFFER_SIZE) {
                numSamples = 0;
                retBytes = baos.toByteArray();
                baos.reset();
            }
            return retBytes;
        } catch (Exception e) {
        }
        return null;
    }
}
