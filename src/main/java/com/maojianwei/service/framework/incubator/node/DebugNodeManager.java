package com.maojianwei.service.framework.incubator.node;

import com.maojianwei.service.framework.incubator.message.queue.MaoAbstractListener;
import com.maojianwei.service.framework.incubator.message.queue.event.PeerEvent;
import com.maojianwei.service.framework.incubator.message.queue.event.PeerEventType;
import com.maojianwei.service.framework.incubator.network.MaoNetworkCore;
import com.maojianwei.service.framework.incubator.network.lib.MaoPeerDemand;
import com.maojianwei.service.framework.lib.MaoAbstractModule;
import com.maojianwei.service.framework.lib.MaoReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class DebugNodeManager extends MaoAbstractModule {

    private final Logger log = LoggerFactory.getLogger(getClass());

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
            switch (event.getType()) {
                case DEVICE_CONNENCTED:
                    maoNetworkCore.getPeer(event.getPeerId()).write(String.format("BigMao is Here,request,%d", System.currentTimeMillis()));
                    break;
                case DEVICE_DATA_RECEIVED:
                    String [] parts = event.getReceivedData().split(",");
                    switch (parts[1]) {
                        case "request":
                            maoNetworkCore.getPeer(event.getPeerId())
                                    .write(String.format("BigMao is Here,reply,%d", System.currentTimeMillis()));
                            break;
                        case "reply":
                            log.info("E2E Delay: {}ms", System.currentTimeMillis() - Long.parseLong(parts[2]));
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            maoNetworkCore.getPeer(event.getPeerId()).write(String.format("BigMao is Here,request,%d", System.currentTimeMillis()));
                            break;
                    }
                    break;
            }
        }
    }
}











