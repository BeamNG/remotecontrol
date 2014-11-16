package com.beamng.udpsteering;

import android.util.Log;

import java.nio.ByteBuffer;

public class Sendpacket {
    private float steeringAngle;
    private boolean throttle;
    private boolean breaks;

    public Sendpacket(float steeringAngle, boolean throttle, boolean breaks){
        this.steeringAngle = steeringAngle;
        this.throttle = throttle;
        this.breaks = breaks;
    }

    public void setBreaks(boolean breaks) {
        this.breaks = breaks;
    }

    public void setSteeringAngle(float steeringAngle) {
        this.steeringAngle = steeringAngle;
    }

    public void setThrottle(boolean throttle) {
        this.throttle = throttle;
    }

    public byte[] getSendingByteArray(){
        Log.i("Sendpacket","Float: "+ steeringAngle+" gas: "+throttle+" bremse "+breaks);
        byte[] floatbytes = ByteBuffer.allocate(4).putFloat(steeringAngle).array();
        byte[] sendingBytes = new byte[6];
        for(int i=0; i<4; i++){sendingBytes[i] = floatbytes[i];}
        sendingBytes[4] = new byte[]{(byte) (throttle?1:0)}[0];
        sendingBytes[5] = new byte[]{(byte) (breaks?1:0)}[0];
        return sendingBytes;
    }
}
