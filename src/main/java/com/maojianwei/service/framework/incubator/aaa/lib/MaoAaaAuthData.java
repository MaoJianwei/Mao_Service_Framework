package com.maojianwei.service.framework.incubator.aaa.lib;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MaoAaaAuthData {

    private InetAddress ip; // IPv4/IPv6
    private int port;


    public MaoAaaAuthData(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public static MaoAaaAuthData getInvalidInstance() {
        try {
            return new MaoAaaAuthData(InetAddress.getByName("::"), 0);
        } catch (UnknownHostException e) {
            // Never come in
            return null;
        }
    }

    public boolean isValid() {
        return ip.isAnyLocalAddress() && port != 0;
    }

    public InetAddress getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
