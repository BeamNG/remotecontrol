package com.beamng.udpsteering;


import android.util.Log;

import org.apache.commons.io.EndianUtils;

import java.nio.ByteBuffer;

public class Recievepacket {
    private int time;
    private int flags;
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

    /*ffi.cdef[[
    typedef struct outgauge_t  {
    unsigned       time;            // time in milliseconds (to check order)        //0-3
    char           car[4];          // Car name                                     //4-7
    unsigned short flags;           // Info (see OG_x below)                        //8-9
    char           gear;            // Reverse:0, Neutral:1, First:2...             //10
    char           plid;            // Unique ID of viewed player (0 = none)        //11
    float          speed;           // M/S                                          //9-12////!/!/!/!/!//!//!/!/!/!/!/!/
    float          rpm;             // RPM                                          //13-16
    float          turbo;           // BAR                                          //17-20
    float          engTemp;         // C                                            //21-24
    float          fuel;            // 0 to 1                                       //25-28
    float          oilPressure;     // BAR                                          //29-32
    float          oilTemp;         // C                                            //33-36
    unsigned       dashLights;      // Dash lights available (see DL_x below)       //37-40
    unsigned       showLights;      // Dash lights currently switched on            //41-44
    float          throttle;        // 0 to 1                                       //45-48
    float          brake;           // 0 to 1                                       //49-52
    float          clutch;          // 0 to 1                                       //53-56
    char           display1[16];    // Usually Fuel                                 //57
    char           display2[16];    // Usually Settings                             //58
    int            id;              // optional - only if OutGauge ID is specified  //59-62
    unsigned	   odometer;	    // distance driven in meters or miles (0-999999)//63-66
    } outgauge_t;
    ,]]*/


        ByteBuffer bb = ByteBuffer.wrap(data);

        speed = EndianUtils.readSwappedFloat(data,0);
        //bb.getFloat(0);


        time = Integer.reverseBytes(bb.getInt(0));

        flags = EndianUtils.readSwappedUnsignedShort(data,5);

        gear = bb.get(7);

        speed = EndianUtils.readSwappedFloat(data,8);

        rpm = EndianUtils.readSwappedFloat(data, 13);

        engTemp = EndianUtils.readSwappedFloat(data, 21);

        fuel = EndianUtils.readSwappedFloat(data, 25);

        dashLights = EndianUtils.readSwappedShort(data, 37);

        showLights = EndianUtils.readSwappedShort(data, 41);

        //odometer = bb.getInt(63);
        //Integer.reverseBytes(odometer);

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
        return gearStr;
        //return "3";
    }

    public float getSpeed(){
        return speed;
    }

    public float getRPM(){
        return rpm;
    }

    public float getEngineTemp(){
        return engTemp;
    }

    public float getFuel(){
        return fuel;
    }

    public int getOdometer() { return 500; }
}
