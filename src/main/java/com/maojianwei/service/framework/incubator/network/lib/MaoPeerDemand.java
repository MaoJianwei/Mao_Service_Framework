package com.maojianwei.service.framework.incubator.network.lib;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

public class MaoPeerDemand {

    private String ipStr;
    private int port;

    private InetAddress ip; // both ipv4 & ipv6, now is ipv4

    public MaoPeerDemand(String ipStr, int port) {
        this.ip = null;
        this.ipStr = ipStr;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MaoPeerDemand)) return false;
        MaoPeerDemand that = (MaoPeerDemand) o;
        return port == that.port && ip.equals(that.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }

    /**
     *
     * @return Nullable
     */
    public InetAddress getIp() {
        if (ip == null) {
            synchronized (this) {
                if (ip == null) {
                    try {
                        ip = InetAddress.getByName(ipStr);
                    } catch (UnknownHostException e) {
                        System.out.println(String.format("WARN: IP addr/Domain name is invalid, %s", ip));
                    }
                }
            }
        }
        return ip;
    }

    public boolean isValid() {
        return getIp() != null && port < 65536 && port > 0;
    }

    public String getIpStr() {
        return ipStr;
    }

    public int getPort() {
        return port;
    }
}
















