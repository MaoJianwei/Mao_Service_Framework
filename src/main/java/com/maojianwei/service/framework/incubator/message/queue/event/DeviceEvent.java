package com.maojianwei.service.framework.incubator.message.queue.event;

import com.maojianwei.service.framework.incubator.node.lib.MaoDevice;

import java.time.LocalDateTime;

public class DeviceEvent {

    private DeviceEventType type;
    private String timestamp;

    private MaoDevice device;

    private final String receivedData;

    public DeviceEvent(DeviceEventType type, MaoDevice device) {
        this(type, device, null);
    }

    public DeviceEvent(DeviceEventType type, MaoDevice device, String receivedData) {
        this(type, device, LocalDateTime.now().toString(), receivedData);
    }

    public DeviceEvent(DeviceEventType type, MaoDevice device, String timestamp, String receivedData) {
        this.type = type;
        this.timestamp = timestamp;
        this.device = device;
        this.receivedData = receivedData;
    }
}
