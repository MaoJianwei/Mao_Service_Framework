package com.maojianwei.service.framework.incubator.node.lib;

import java.util.Objects;

public class MaoDeviceId {

    private static String SPLITER = "/";
    private String deviceIdStr;

    public MaoDeviceId(String ip, int port) {
        this.deviceIdStr = ip + SPLITER + port;
    }

    public String getDeviceIdStr() {
        return deviceIdStr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaoDeviceId that = (MaoDeviceId) o;
        return deviceIdStr.equals(that.deviceIdStr);
    }

    @Override
    public int hashCode() {
        return deviceIdStr.hashCode();
    }
}
