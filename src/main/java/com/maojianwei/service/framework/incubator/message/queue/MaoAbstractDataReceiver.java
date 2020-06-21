package com.maojianwei.service.framework.incubator.message.queue;

import com.maojianwei.service.framework.incubator.message.queue.MaoAbstractListener;
import com.maojianwei.service.framework.incubator.message.queue.event.DeviceEvent;
import com.maojianwei.service.framework.incubator.message.queue.event.DeviceEventType;

public abstract class MaoAbstractDataReceiver extends MaoAbstractListener<DeviceEvent> {

    protected abstract void process(DeviceEvent event);

    protected boolean isRelevant(DeviceEvent event) {
        // DataListener is just allowed to add to DataDispatcher
        return false;
    }

    // provide accessibility to DataDispatcher
    public void postData(DeviceEvent event) {
        passEvent(event);
    }
}
