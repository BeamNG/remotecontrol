package com.beamng.udpsteering;

import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;

public class Sendpacket {
    private float steeringAngle;
    private float throttle;
    private float breaks;

    public Sendpacket(float steeringAngle, float throttle, float breaks){
        this.steeringAngle = steeringAngle;
        this.throttle = throttle;
        this.breaks = breaks;

        /*
        float 		steeringangle      //-1 to +1   //0-3

        float		throttle	   // -1 to +1      //4-7

        float		breaks		   // -1 to +1      // 8-11
        */
    }

    public void setBreaks(float breaks) {
        this.breaks = breaks;
    }

    public void setSteeringAngle(float steeringAngle) {
        this.steeringAngle = steeringAngle;
    }

    public void setThrottle(float throttle) {
        this.throttle = throttle;
    }

    public byte[] getSendingByteArray(){
        Log.i("Sendpacket","Float: "+ steeringAngle+" gas: "+throttle+" bremse "+breaks);
        byte[] floatbytes1 = ByteBuffer.allocate(4).putFloat(steeringAngle).array();
        ArrayUtils.reverse(floatbytes1);
        byte[] floatbytes2 = ByteBuffer.allocate(4).putFloat(throttle).array();
        ArrayUtils.reverse(floatbytes2);
        byte[] floatbytes3 = ByteBuffer.allocate(4).putFloat(breaks).array();
        ArrayUtils.reverse(floatbytes3);
        byte[] sendingBytes = new byte[12];
        for(int i=0; i<4; i++)
        {
            sendingBytes[i] = floatbytes1[i];
            sendingBytes[i+4] = floatbytes2[i];
            sendingBytes[i+8] = floatbytes3[i];
        }
        return sendingBytes;
    }
}
