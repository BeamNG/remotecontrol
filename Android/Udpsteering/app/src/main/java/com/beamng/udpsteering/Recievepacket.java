package com.beamng.udpsteering;


import android.util.Log;

import java.nio.ByteBuffer;

public class Recievepacket {
    private short flags;
    private boolean[] flagsarray = new boolean[5];
    private byte gear;
    private String gearStr;
    private float speed;
    private float rpm;
    private float engTemp;
    private float fuel;
    private int odometer;
    private short dashLights;
    private boolean[] dasharray = new boolean[11];
    private short showLights;
    private boolean[] lightsArray = new boolean[11];

    //Constructor
    public Recievepacket(byte[] data){

        ByteBuffer bb = ByteBuffer.wrap(data);

        flags = bb.getShort(0);
        gear = bb.get(1);
        speed = bb.getFloat(2);
        rpm = bb.getFloat(6);
        engTemp = bb.getFloat(10);
        fuel = bb.getFloat(14);
        dashLights = bb.getShort(18);
        showLights = bb.getShort(20);
        odometer = bb.getInt(22);

        bb.clear();

        Log.i("CONSTRUCTOR","flags= " + flags + " gear= "+ gear + " speed= "+speed+ " rpm= "+rpm+" engTemp= "+engTemp+" fuel= "+fuel+" odometer= "+odometer+" dashLights= "+dashLights+" showLights= "+showLights);

    }

    //getters
    public boolean[] getFlagsArray(){
        return flagsarray;
    }

    public boolean[] getDashUsedArr(){
        return dasharray;
    }

    public boolean[] getActiveLightsArr(){
        return lightsArray;
    }

    public String getGear(){
        switch (gear){
            case 00000000:
                gearStr = "R";
            break;
            case 00000001:
                gearStr = "N";
                break;
            case 00000010:
                gearStr = "1";
                break;
            case 00000011:
                gearStr = "2";
                break;
            case 00000100:
                gearStr = "3";
                break;
            case 00000101:
                gearStr = "4";
                break;
            case 00000110:
                gearStr = "5";
                break;
            case 00000111:
                gearStr = "6";
                break;
        }
        //return gearStr;
        return "3";
    }

    public float getSpeed(){
        return 0.5f;
    }

    public float getRPM(){
        return 0.5f;
    }

    public float getEngineTemp(){
        return 0.5f;
    }

    public float getFuel(){
        return 0.5f;
    }

    public int getOdometer() { return 500; }
}
