package com.beamng.udpsteering;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
    int hostPORT = 4444;
    int localPORT = hostPORT + 1;
    InetAddress netadress;
    DatagramSocket socketS;
    DatagramSocket socketR;
    Activity aContext;
    Boolean bKeepRunning = true;
    private OnUdpConnected listener;
    String Iadr;
    InetAddress hostadress;
    private final ProgressDialog progressDialog;



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



    public UdpExploreSender(InetAddress iadr, Activity activityContext, OnUdpConnected listener, String iadrr, Context ctx) {
        this.netadress = iadr;
        this.aContext = activityContext;
        this.listener = listener;
        this.Iadr = iadrr;

            progressDialog = new ProgressDialog(ctx);
            progressDialog.setMessage("Please select this device in your BeamNG.Drive Game");
            progressDialog.setTitle("Connecting ...");
            progressDialog.setCancelable(true);
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(true);
                }
            });
        }

    @Override
    protected String doInBackground(String... arg0) {


        sendString = "beamng" + getDeviceName();
        Log.e("SendString: ", sendString);
        byte[] buffer = (sendString).getBytes();

        if(socketS == null) {
        try {
            socketS = new DatagramSocket();}catch (SocketException e) {e.printStackTrace();}}
        try {
            packet = new DatagramPacket(buffer, buffer.length, netadress, hostPORT);
            Log.i("UDP","Sending String: "+ new String(buffer));
            Log.i("Socket","created + sending");

            socketS.send(packet);
            //Log.i("UDP","C: Sent.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i("RecieveSocketBinder",Iadr + ":" + localPORT);
        if(socketR == null) {
            try {
                DatagramChannel channel = DatagramChannel.open();
                socketR = channel.socket();
                socketR.setReuseAddress(true);
            socketR.bind(new InetSocketAddress(Iadr , localPORT));
            }catch (Exception e) {e.printStackTrace();}}

        byte[] buf = new byte[128];
        packetr = new DatagramPacket(buf, buf.length);
        while (bKeepRunning){
            try {
                socketR.receive(packetr);
            }catch (IOException e) {
                e.printStackTrace();
            }
            String message = new String(buf, 0, packetr.getLength());
            hostadress = packetr.getAddress();
            //Log.i("UDP SERVER","Received: " + message + " IP " + packetr.getAddress().getHostAddress() + ":" + packetr.getPort());
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
            progressDialog.dismiss();
        }else {
            bKeepRunning = true;
        }
    }

    @Override
    protected void onPreExecute() {
        progressDialog.show();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        socketR.disconnect();
        socketR.close();
        socketS.close();
        bKeepRunning=false;
        progressDialog.dismiss();
    }
}

