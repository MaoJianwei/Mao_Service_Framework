package com.maojianwei.service.framework.incubator.node;

import com.maojianwei.service.framework.incubator.message.queue.MaoAbstractListener;
import com.maojianwei.service.framework.incubator.message.queue.event.PeerEvent;
import com.maojianwei.service.framework.incubator.network.MaoNetworkCore;
import com.maojianwei.service.framework.incubator.network.lib.MaoPeerDemand;
import com.maojianwei.service.framework.lib.MaoAbstractModule;
import com.maojianwei.service.framework.lib.MaoReference;

import java.time.LocalDateTime;

public class DebugNodeManager extends MaoAbstractModule {

    @MaoReference
    private MaoNetworkCore maoNetworkCore;

    private DebugDeviceAbstractListener deviceListener = new DebugDeviceAbstractListener();

    private DebugNodeManager() {
        super("DebugNodeManager");
    }
    private static DebugNodeManager singletonInstance;
    public static DebugNodeManager getInstance() {
        if (singletonInstance == null) {
            synchronized (DebugNodeManager.class) {
                if (singletonInstance == null) {
                    singletonInstance = new DebugNodeManager();
                }
            }
        }
        return singletonInstance;
    }

    @Override
    public void activate() {
        maoNetworkCore.addPeerNeeds(new MaoPeerDemand("127.0.0.1", 6688));
        deviceListener.startListener();
        maoNetworkCore.addListener(deviceListener);
    }

    @Override
    public void deactivate() {
        maoNetworkCore.removeListener(deviceListener);
        deviceListener.stopListener();
    }

    private class DebugDeviceAbstractListener extends MaoAbstractListener<PeerEvent> {

        @Override
        protected void process(PeerEvent event) {
            System.out.println(event.toString());
        }
    }
}











