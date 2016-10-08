package com.beamng.remotecontrol;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import java.net.InetAddress;

public class UdpExploreSenderFragment extends Fragment implements OnUdpConnected {

    // data object we want to retain
    private UdpExploreSender exploreSender;
    private QRCodeScanner scannerActivity;

    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    public void execute(InetAddress broadcastAddress, QRCodeScanner parent, String ip, String securityCode) {
        assert(exploreSender == null);
        exploreSender = new UdpExploreSender(broadcastAddress, this, ip, parent);
        exploreSender.execute(securityCode);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.scannerActivity = (QRCodeScanner)activity;
    }


    @Override
    public void onUdpConnected(InetAddress hostAddress) {
        Intent intent = new Intent(this.scannerActivity, MainActivity.class);
        ((RemoteControlApplication)scannerActivity.getApplication()).setHostAddress(hostAddress);
        startActivity(intent);
    }

    @Override
    public void onError(String message) {
        scannerActivity.onError(message);
    }

    @Override
    public void onCancel() {
        if (exploreSender != null) {
            cancelTask();
            scannerActivity.onError(null);
        }
    }

    public void cancelTask() {
        if (isRunning()) {
            exploreSender.cancel(true);
        }
    }

    public boolean isRunning() {
        if (exploreSender == null) {
            return false;
        }
        return exploreSender.getStatus() == AsyncTask.Status.RUNNING;
    }
}