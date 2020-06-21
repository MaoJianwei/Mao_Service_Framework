package com.maojianwei.service.framework.incubator.node.lib;

public class MaoNode {

    private MaoNodeId deviceId;
    private MaoNodeState state;

    public MaoNode(MaoNodeId deviceId) {
        this(deviceId, MaoNodeState.DEVICE_DOWN);
    }

    public MaoNode(MaoNodeId deviceId, MaoNodeState state) {
        this.deviceId = deviceId;
        this.state = state;
    }

    public MaoNodeId getDeviceId() {
        return deviceId;
    }

    public MaoNodeState getState() {
        return state;
    }

    public void setState(MaoNodeState state) {
        this.state = state;
    }
}
