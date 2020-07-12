package com.maojianwei.service.framework.incubator.aaa.lib;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MaoAaaData {

    private InetAddress ip; // IPv4/IPv6
    private int port;


    public MaoAaaData(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public static MaoAaaData getInvalidInstance() {
        try {
            return new MaoAaaData(InetAddress.getByName("::"), 0);
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
