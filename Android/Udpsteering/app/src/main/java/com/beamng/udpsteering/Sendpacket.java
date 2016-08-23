package com.beamng.udpsteering;

import android.util.Log;

import java.nio.ByteBuffer;

public class Sendpacket {
    private float steeringAngle; // 0 to 1, bytes 0-3
    private float throttle;      // 0 or 1, bytes 4-7
    private float brakes;        // 0 or 1, bytes 8-11
    private float id;        // 0+, bytes 12-15

    public void setBreaks(float brakes) {
        this.brakes = brakes;
    }

    public void setSteeringAngle(float steeringAngle) {
        this.steeringAngle = steeringAngle;
    }

    public void setThrottle(float throttle) {
        this.throttle = throttle;
    }

    public void setID(float id) {
        this.id = id;
    }

    public static byte [] float2ByteArray (float value)
    {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    public byte[] getSendingByteArray(){

        //Log.i("Sendpacket", "steer: "+ steeringAngle+", throttle: "+throttle+", brake: "+brakes);


        byte s[] = ByteBuffer.allocate(4).order(java.nio.ByteOrder.BIG_ENDIAN).putFloat(steeringAngle).array();
        byte t[] = ByteBuffer.allocate(4).order(java.nio.ByteOrder.BIG_ENDIAN).putFloat(throttle).array();
        byte b[] = ByteBuffer.allocate(4).order(java.nio.ByteOrder.BIG_ENDIAN).putFloat(brakes).array();
        byte i[] = ByteBuffer.allocate(4).order(java.nio.ByteOrder.BIG_ENDIAN).putFloat(id).array();

        byte[] res = new byte[16];
        System.arraycopy(s, 0, res, 0, 4);
        System.arraycopy(t, 0, res, 4, 4);
        System.arraycopy(b, 0, res, 8, 4);
        System.arraycopy(i, 0, res, 12, 4);

        return res;
    }
}
