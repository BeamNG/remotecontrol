package com.beamng.udpsteering;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.text.Html;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Random;
import java.nio.channels.DatagramChannel;

public class UdpExploreSender extends AsyncTask<String, String, String> {
    DatagramPacket packetS;
    DatagramPacket packetR;
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
        progressDialog.setMessage("Connecting to BeamNG.drive");
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancel(true);
            }
            });
    }

    @Override
    protected String doInBackground(String... args) {
        String securityCode = args[0];

        sendString = "beamng|" + getDeviceName() + "|" + securityCode;
        Log.i("SendString: ", sendString);
        byte[] buffer = (sendString).getBytes();

        if(socketS == null) {
            try {
                socketS = new DatagramSocket();
            }catch (SocketException e) {
                e.printStackTrace();
            }
        }

        //Log.i("ReceiveSocketBinder",Iadr + ":" + localPORT);
        if(socketR == null) {
            try {
                DatagramChannel channel = DatagramChannel.open();
                socketR = channel.socket();
                socketR.setReuseAddress(true);
                socketR.bind(new InetSocketAddress(Iadr , localPORT));
                socketR.setSoTimeout(250);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        //Log.i("UDP","Sending String: "+ new String(buffer));
        String waitingFor = "beamng|" + securityCode;
        byte[] receive_buf = new byte[32];
        try {
            packetR = new DatagramPacket(receive_buf, receive_buf.length);
            packetS = new DatagramPacket(buffer, buffer.length, netadress, hostPORT);
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (bKeepRunning){
            try {
                socketS.send(packetS);
                socketR.receive(packetR);
            }catch (IOException e) {
                //e.printStackTrace();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                continue;
            }
            String message = new String(receive_buf, 0, packetR.getLength());
            hostadress = packetR.getAddress();

            Log.i("UDP SERVER","Received: " + message + " IP " + packetR.getAddress().getHostAddress() + ":" + packetR.getPort() + " / waiting for: " + waitingFor);
            if(message.equals(waitingFor)) {
                publishProgress(message);
            }
        }
        return null;
    }
    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        bKeepRunning = false;
        listener.onUdpConnected(hostadress);
        cancel(true);
        socketR.close();
        progressDialog.dismiss();
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

