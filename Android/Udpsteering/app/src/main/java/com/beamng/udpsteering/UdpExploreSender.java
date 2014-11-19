package com.beamng.udpsteering;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;

public class UdpExploreSender extends AsyncTask<String, String, String> {
    DatagramPacket packet;
    DatagramPacket packetr;
    String sendString;
    int PORT = 4444;
    InetAddress netadress;
    DatagramSocket socketS;
    DatagramSocket socketR;
    Activity aContext;
    Boolean bKeepRunning = true;
    private OnUdpConnected listener;
    String Iadr;
    InetAddress hostadress;



    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }



    public UdpExploreSender(InetAddress iadr, Activity activityContext, OnUdpConnected listener, String iadrr) {
        this.netadress = iadr;
        this.aContext = activityContext;
        this.listener = listener;
        this.Iadr = iadrr;
    }

    @Override
    protected String doInBackground(String... arg0) {


        sendString = getDeviceName();
        Log.e("SendString: ", sendString);
        byte[] buffer = (sendString).getBytes();

        if(socketS == null) {
        try {
            socketS = new DatagramSocket();}catch (SocketException e) {e.printStackTrace();}}
        try {
            packet = new DatagramPacket(buffer, buffer.length, netadress, PORT);
            Log.i("UDP","Sending String: "+ new String(buffer));
            Log.i("Socket","created + sending");

            socketS.send(packet);
            Log.i("UDP","C: Sent.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i("RecieveSocketBinder",Iadr + ":4444");
        if(socketR == null) {
            try {
                DatagramChannel channel = DatagramChannel.open();
                socketR = channel.socket();
            socketR.bind(new InetSocketAddress(Iadr , PORT));
            }catch (Exception e) {e.printStackTrace();}}

        byte[] buf = new byte[128];
        packetr = new DatagramPacket(buf, buf.length);
        while (bKeepRunning){
        try {socketR.receive(packetr);}catch (IOException e) {e.printStackTrace();}
        String message = new String(buf, 0, packetr.getLength());
         hostadress = packetr.getAddress();
        Log.i("UDP SERVER","Recieved: " + message + " IP " + packetr.getAddress().toString() + ":" + packetr.getPort());
        publishProgress(message);
            }
        return null;
    }
    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        if (values[0].equals("beamng")){
            bKeepRunning = false;
            listener.onUdpConnected(hostadress);
            cancel(true);
            socketR.close();
        }else {
            bKeepRunning = true;
        }
    }


}

