package com.maojianwei.service.framework.incubator.node.lib;

import com.maojianwei.service.framework.incubator.network.lib.MaoPeer;

public class MaoNode {

    private MaoPeer peer;
    private MaoNodeId deviceId;
    private MaoNodeState state;

    public MaoNode(MaoNodeId deviceId) {
        this(null, deviceId, MaoNodeState.DEVICE_DOWN);
    }

    public MaoNode(MaoPeer peer, MaoNodeId deviceId, MaoNodeState state) {
        this.peer = peer;
        this.deviceId = deviceId;
        this.state = state;
    }

    public MaoNodeId getDeviceId() {
        return deviceId;
    }

    public int getPeerId() {
        // FIXME
        return peer.getId();
    }

    public MaoNodeState getState() {
        return state;
    }

    public void setState(MaoNodeState state) {
        this.state = state;
    }

    public void write(String data) {
        if(peer != null) {
            peer.write(data);
        }
    }
}


























