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
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Random;
import java.nio.channels.DatagramChannel;

public class UdpExploreSender extends AsyncTask<String, String, String> {
    String sendString;
    int hostPORT = 4444;
    int localPORT = hostPORT + 1;
    InetAddress netadress;
    Activity aContext;
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

        DatagramSocket socketS = null;
        DatagramChannel channel = null;
        try {
            socketS = new DatagramSocket();
            channel = DatagramChannel.open();
            DatagramSocket socketR = channel.socket();
            socketR.setReuseAddress(true);
            socketR.bind(new InetSocketAddress(Iadr, localPORT));
            socketR.setSoTimeout(250);
            final String waitingFor = "beamng|" + securityCode;
            byte[] receive_buf = new byte[32];
            DatagramPacket packetR = new DatagramPacket(receive_buf, receive_buf.length);
            DatagramPacket packetS = new DatagramPacket(buffer, buffer.length, netadress, hostPORT);

            int tries = 0;
            while (!isCancelled()) {
                try {
                    socketS.send(packetS);
                    socketR.receive(packetR);
                } catch (IOException e) {
                    if (++tries > 10) {
                        return "Connection timeout.";
                    }
                    //e.printStackTrace();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        break;
                    }
                    continue;
                }
                String message = new String(receive_buf, 0, packetR.getLength());
                hostadress = packetR.getAddress();

                Log.i("UDP SERVER", "Received: " + message + " IP " +
                      packetR.getAddress().getHostAddress() + ":" + packetR.getPort() +
                      " / waiting for: " + waitingFor);
                if (message.equals(waitingFor)) {
                    publishProgress(message);
                    break;
                }
            }
        } catch (Exception e) {
            return e.getLocalizedMessage();
        } finally {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (socketS != null) {
                socketS.close();
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        listener.onUdpConnected(hostadress);
        cancel(true);
    }

    @Override
    protected void onPreExecute() {
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(String errorMessage) {
        listener.onError(errorMessage);
        progressDialog.dismiss();
    }

    @Override
    protected void onCancelled() {
        listener.onError(null);
        progressDialog.dismiss();
    }
}

