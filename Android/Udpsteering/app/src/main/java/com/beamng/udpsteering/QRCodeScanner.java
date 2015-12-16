package com.beamng.udpsteering;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
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
import java.util.concurrent.ThreadPoolExecutor;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QRCodeScanner extends Activity
        implements ZXingScannerView.ResultHandler, OnUdpConnected {
    private ZXingScannerView mScannerView;
    private WifiManager wifiManager;
    private ConnectivityManager connManager;
    private Context mContext;
    private UdpExploreSender exploreSender;
    private ProgressDialog ringProgressDialog;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mContext = getApplicationContext();
        ringProgressDialog = new ProgressDialog(this);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
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
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
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
        connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi == null || !mWifi.isConnected()) {
            Toast.makeText(this, "You need to be connected to a WiFi network.", Toast.LENGTH_LONG).show();
            mScannerView.startCamera();
            return;
        }

        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));

        ((RemoteControlApplication)getApplication()).setIp(ip);

        InetAddress broadcastAddress = getBroadcastAddress(getIpAddress());
        Log.i("Broadcastaddress", broadcastAddress.getHostAddress());

        exploreSender = new UdpExploreSender(broadcastAddress, this, this, ip, this);
        exploreSender.execute(securityCode);
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
        InetAddress inetAddress = null;
        InetAddress myAddr = null;

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

                        myAddr = inetAddress;
                    }
                }
            }

        } catch (SocketException ex) {
        }
        return myAddr;
    }

    @Override
    public void onUdpConnected(InetAddress hostAddress) {
        ringProgressDialog.dismiss();
        Intent intent = new Intent(this, MainActivity.class);
        ((RemoteControlApplication)getApplication()).setHostAddress(hostAddress);
        startActivity(intent);
    }
}