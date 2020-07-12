package com.maojianwei.service.framework.incubator.node.lib;

public class MaoNodeId {

    public static String NODEID_SPLITER = "/";
    private String deviceIdStr;

    public MaoNodeId(String ip, int port) {
        this.deviceIdStr = ip + NODEID_SPLITER + port;
    }

    public String getDeviceIdStr() {
        return deviceIdStr;
    }

    @Override
    public String toString() {
        return getDeviceIdStr();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaoNodeId that = (MaoNodeId) o;
        return deviceIdStr.equals(that.deviceIdStr);
    }

    @Override
    public int hashCode() {
        return deviceIdStr.hashCode();
    }
}
