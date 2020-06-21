package com.maojianwei.service.framework.incubator.message.queue.event;

import com.maojianwei.service.framework.incubator.node.lib.MaoNodeId;

import java.time.LocalDateTime;

public class DeviceEvent {

    private DeviceEventType type;
    private String timestamp;

    private MaoNodeId deviceId;

    private final String receivedData;

    public DeviceEvent(DeviceEventType type, MaoNodeId deviceId) {
        this(type, deviceId, null);
    }

    public DeviceEvent(DeviceEventType type, MaoNodeId deviceId, String receivedData) {
        this(type, deviceId, LocalDateTime.now().toString(), receivedData);
    }

    public DeviceEvent(DeviceEventType type, MaoNodeId deviceId, String timestamp, String receivedData) {
        this.type = type;
        this.timestamp = timestamp;
        this.deviceId = deviceId;
        this.receivedData = receivedData;
    }

    public DeviceEventType getType() {
        return type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public MaoNodeId getDeviceId() {
        return deviceId;
    }

    public String getReceivedData() {
        return receivedData;
    }
}
