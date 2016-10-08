package com.beamng.remotecontrol;

import android.app.Application;

import java.net.InetAddress;

public class RemoteControlApplication extends Application {
    private InetAddress hostAddress;
    private String ip;

    public void setHostAddress(InetAddress i) {
        hostAddress = i;
    }

    public InetAddress getHostAddress() {
        return hostAddress;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }
}
