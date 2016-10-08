package com.beamng.remotecontrol;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.zxing.Result;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QRCodeScanner extends Activity
        implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private WifiManager wifiManager;
    private UdpExploreSenderFragment exploreSenderFragment;
    private ProgressDialogFragment progressDialogFragment;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view

        // find the retained fragment on activity restarts
        FragmentManager fm = getFragmentManager();
        exploreSenderFragment = (UdpExploreSenderFragment) fm.findFragmentByTag("exploreSender");
        if (exploreSenderFragment == null) {
            exploreSenderFragment = new UdpExploreSenderFragment();
            fm.beginTransaction().add(exploreSenderFragment, "exploreSender").commit();
        }
        progressDialogFragment = (ProgressDialogFragment) fm.findFragmentByTag("progressDialog");
        if (progressDialogFragment == null) {
            progressDialogFragment = new ProgressDialogFragment();
        }
        progressDialogFragment.setListener(exploreSenderFragment);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);

        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera(); // The camera needs to be started at least once ...
        if (exploreSenderFragment.isRunning()) {
            mScannerView.stopCamera(); // ... even if we stop it immediately
        } else {
            if (progressDialogFragment.isShowing()) {
                progressDialogFragment.dismiss();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!isChangingConfigurations()) {
            exploreSenderFragment.cancelTask();
        }
        mScannerView.stopCamera(); // Stop camera on pause
    }

    @Override
    public void handleResult(Result result) {
        String[] parts = result.getText().split("#");
        if (parts.length != 2) {
            Toast.makeText(this, "Invalid QR code", Toast.LENGTH_LONG).show();
            mScannerView.startCamera();
            return;
        }
        final String securityCode = parts[1];

        // Check for WiFi connectivity
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

        try {
            int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
            String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));

            ((RemoteControlApplication) getApplication()).setIp(ip);

            InetAddress broadcastAddress = getBroadcastAddress(getIpAddress());
            Log.i("Broadcast Address", broadcastAddress.getHostAddress());

            exploreSenderFragment.execute(broadcastAddress, this, ip, securityCode);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            progressDialogFragment.show(ft, "progressDialog");
        } catch(RuntimeException e) {
            Toast.makeText(this, "You need to be connected to a WiFi network.", Toast.LENGTH_LONG).show();
            mScannerView.startCamera();
        }
    }

    private InetAddress getBroadcastAddress(InetAddress inetAddr) {
        NetworkInterface temp;
        InetAddress iAddr = null;
        try {
            temp = NetworkInterface.getByInetAddress(inetAddr);
            List<InterfaceAddress> addresses = temp.getInterfaceAddresses();

            for (InterfaceAddress inetAddress : addresses)

                iAddr = inetAddress.getBroadcast();
            return iAddr;

        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    public InetAddress getIpAddress() {
        InetAddress inetAddress;
        InetAddress myAddress = null;

        try {
            for (Enumeration<NetworkInterface> networkInterface = NetworkInterface
                    .getNetworkInterfaces(); networkInterface.hasMoreElements(); ) {

                NetworkInterface singleInterface = networkInterface.nextElement();

                for (Enumeration<InetAddress> IpAddresses = singleInterface.getInetAddresses(); IpAddresses
                        .hasMoreElements(); ) {
                    inetAddress = IpAddresses.nextElement();

                    if (!inetAddress.isLoopbackAddress() && (singleInterface.getDisplayName()
                            .contains("wlan0") ||
                            singleInterface.getDisplayName().contains("eth0") ||
                            singleInterface.getDisplayName().contains("ap0"))) {

                        myAddress = inetAddress;
                    }
                }
            }

        } catch (SocketException ex) {
        }
        return myAddress;
    }

    public void onError(String message) {
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
        progressDialogFragment.dismiss();
        mScannerView.startCamera();
    }
}