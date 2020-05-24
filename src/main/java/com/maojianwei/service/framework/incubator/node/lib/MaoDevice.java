package com.maojianwei.service.framework.incubator.node.lib;

public class MaoDevice {



    private MaoDeviceId deviceId;
    private MaoDeviceState state;

    public MaoDevice(MaoDeviceId deviceId) {
        this(deviceId, MaoDeviceState.DEVICE_DOWN);
    }

    public MaoDevice(MaoDeviceId deviceId, MaoDeviceState state) {
        this.deviceId = deviceId;
        this.state = state;
    }

    public MaoDeviceId getDeviceId() {
        return deviceId;
    }

    public MaoDeviceState getState() {
        return state;
    }

    public void setState(MaoDeviceState state) {
        this.state = state;
    }
}
