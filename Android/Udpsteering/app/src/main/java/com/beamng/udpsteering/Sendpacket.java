package com.beamng.udpsteering;

import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;

public class Sendpacket {
    private float steeringAngle;
    private float throttle;
    private float brakes;

    public Sendpacket(float steeringAngle, float throttle, float brakes){
        this.steeringAngle = steeringAngle;
        this.throttle = throttle;
        this.brakes = brakes;

        /*
        float 		steeringangle      //-1 to +1   //0-3

        float		throttle	   // -1 to +1      //4-7

        float		breaks		   // -1 to +1      // 8-11
        */
    }

    public void setBreaks(float brakes) {
        this.brakes = brakes;
    }

    public void setSteeringAngle(float steeringAngle) {
        this.steeringAngle = steeringAngle;
    }

    public void setThrottle(float throttle) {
        this.throttle = throttle;
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

        byte[] res = new byte[12];
        System.arraycopy(s, 0, res, 0, 4);
        System.arraycopy(t, 0, res, 4, 4);
        System.arraycopy(b, 0, res, 8, 4);

        return res;
    }
}
