package com.maojianwei.service.framework.incubator.app.NodePairQoSMonitor;

import com.maojianwei.service.framework.incubator.message.queue.MaoAbstractListener;
import com.maojianwei.service.framework.incubator.message.queue.event.DeviceEvent;
import com.maojianwei.service.framework.lib.MaoAbstractModule;

public class NodePairQoSManager extends MaoAbstractModule<DeviceEvent, MaoAbstractListener<DeviceEvent>> {

    public NodePairQoSManager() {
        super("");
    }

    @Override
    public void activate() {

    }

    @Override
    public void deactivate() {

    }
}
