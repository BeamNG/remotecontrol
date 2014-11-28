package com.beamng.udpsteering;


import android.util.Log;

import org.apache.commons.io.EndianUtils;

import java.nio.ByteBuffer;

public class Recievepacket {
    private int time;
    private int flags;
    private final short FLAG_KMH = 16384;
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
    private final short FLAG_SHIFTLIGHT = 1;
    private final short FLAG_FULLBEAM = 2;
    private final short FLAG_HANDBREAK = 16;
    private final short FLAG_SIGNAL_L = 32;
    private final short FLAG_SIGNAL_R = 64;
    private final short FLAG_SIGNAL_ANY = 128;
    private final short FLAG_OILWARN = 256;
    private final short FLAG_BATTERY = 512;
    private final short FLAG_ABS = 1024;
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
    float          speed;           // 0-1                                          //12-15
    float          rpm;             // 0-1                                          //16-19
    float          turbo;           // BAR                                          //20-23
    float          engTemp;         // C                                            //24-27
    float          fuel;            // 0 to 1                                       //28-31
    float          oilPressure;     // BAR                                          //32-35
    float          oilTemp;         // C                                            //36-39
    unsigned       dashLights;      // Dash lights available (see DL_x below)       //40-43
    unsigned       showLights;      // Dash lights currently switched on            //44-47
    float          throttle;        // 0 to 1                                       //48-51
    float          brake;           // 0 to 1                                       //52-55
    float          clutch;          // 0 to 1                                       //56-59
    char           display1[16];    // Usually Fuel                                 //60-75
    char           display2[16];    // Usually Settings                             //76-80
    int            id;              // optional - only if OutGauge ID is specified  //81-84
    unsigned	   odometer;	    // distance driven in meters or miles (0-999999)//85-88
    } outgauge_t;
    ,]]

    --[[
    CONSTANTS
    // OG_x - bits for OutGaugePack Flags
    #define OG_SHIFT      1        // key
    #define OG_CTRL       2        // key
    #define OG_TURBO      8192     // show turbo gauge
    #define OG_KM         16384    // if not set - user prefers MILES
    #define OG_BAR        32768    // if not set - user prefers PSI

    // DL_x - bits for OutGaugePack DashLights and ShowLights
    DL_SHIFT,           // bit 0    - shift light
    DL_FULLBEAM,        // bit 1    - full beam
    DL_HANDBRAKE,       // bit 2    - handbrake
    DL_PITSPEED,        // bit 3    - pit speed limiter
    DL_TC,              // bit 4    - TC active or switched off
    DL_SIGNAL_L,        // bit 5    - left turn signal
    DL_SIGNAL_R,        // bit 6    - right turn signal
    DL_SIGNAL_ANY,      // bit 7    - shared turn signal
    DL_OILWARN,         // bit 8    - oil pressure warning
    DL_BATTERY,         // bit 9    - battery warning
    DL_ABS,             // bit 10   - ABS active or switched off
    DL_SPARE,           // bit 11
    ]]--

    */


        ByteBuffer bb = ByteBuffer.wrap(data);

        time = Integer.reverseBytes(bb.getInt(0));

        flags = EndianUtils.readSwappedUnsignedShort(data,8);

        gear = bb.get(10);

        speed = EndianUtils.readSwappedFloat(data,12);

        rpm = EndianUtils.readSwappedFloat(data, 16);

        engTemp = EndianUtils.readSwappedFloat(data, 24);

        fuel = EndianUtils.readSwappedFloat(data, 28);

        dashLights = EndianUtils.readSwappedShort(data, 40);

        showLights = EndianUtils.readSwappedShort(data, 44);

        //Integer.reverseBytes(odometer);

        bb.clear();

        Log.i("CONSTRUCTOR","flags= " + flags + " gear= "+ gear + " speed= "+speed+ " rpm= "+rpm+" engTemp= "+engTemp+" fuel= "+fuel+" odometer= "+odometer+" dashLights= "+dashLights+" showLights= "+showLights);

    }

    //getters
    public boolean[] getFlagsArray(){
        if((flags & FLAG_KMH) == FLAG_KMH)
        {flagsarray[3]= true;}
        return flagsarray;
    }

    public boolean[] getDashUsedArr(){
        return dasharray;
    }

    public boolean[] getActiveLightsArr(){

        if((showLights & FLAG_SHIFTLIGHT) == FLAG_SHIFTLIGHT)
        {lightsArray[0]= true;}

        if((showLights & FLAG_FULLBEAM) == FLAG_FULLBEAM)
        {lightsArray[1]= true;}

        if((showLights & FLAG_HANDBREAK) == FLAG_HANDBREAK)
        {lightsArray[2]= true;}

        if((showLights & FLAG_SIGNAL_L) == FLAG_SIGNAL_L)
        {lightsArray[5]= true;}

        if((showLights & FLAG_SIGNAL_R) == FLAG_SIGNAL_R)
        {lightsArray[6]= true;}

        if((showLights & FLAG_SIGNAL_ANY) == FLAG_SIGNAL_ANY)
        {lightsArray[7]= true;}

        if((showLights & FLAG_OILWARN) == FLAG_OILWARN)
        {lightsArray[8]= true;}

        if((showLights & FLAG_BATTERY) == FLAG_BATTERY)
        {lightsArray[9]= true;}

        if((showLights & FLAG_ABS) == FLAG_ABS)
        {lightsArray[10]= true;}

        return lightsArray;
    }

    public String getGear(){
        switch (gear){
            case 0:
                gearStr = "R";
            break;
            case 1:
                gearStr = "N";
                break;
            case 2:
                gearStr = "1";
                break;
            case 3:
                gearStr = "2";
                break;
            case 4:
                gearStr = "3";
                break;
            case 5:
                gearStr = "4";
                break;
            case 6:
                gearStr = "5";
                break;
            case 7:
                gearStr = "6";
                break;
        }
        return gearStr;
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
