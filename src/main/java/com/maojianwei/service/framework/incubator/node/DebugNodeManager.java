package com.maojianwei.service.framework.incubator.node;

import com.maojianwei.service.framework.incubator.message.queue.MaoAbstractListener;
import com.maojianwei.service.framework.incubator.message.queue.event.DeviceEvent;
import com.maojianwei.service.framework.incubator.message.queue.event.DeviceEventType;
import com.maojianwei.service.framework.incubator.message.queue.event.PeerEvent;
import com.maojianwei.service.framework.incubator.message.queue.event.PeerEventType;
import com.maojianwei.service.framework.incubator.network.MaoNetworkCore;
import com.maojianwei.service.framework.incubator.node.lib.MaoDevice;
import com.maojianwei.service.framework.incubator.node.lib.MaoDeviceId;
import com.maojianwei.service.framework.incubator.node.lib.MaoDeviceState;
import com.maojianwei.service.framework.lib.MaoAbstractModule;
import com.maojianwei.service.framework.lib.MaoReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class DebugNodeManager extends MaoAbstractModule {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @MaoReference
    private MaoNetworkCore maoNetworkCore;

    private Map<MaoDeviceId, MaoDevice> devices = new HashMap<>();

    private DebugPeerEventListener deviceListener = new DebugPeerEventListener();

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
//        maoNetworkCore.addPeerNeeds(new MaoPeerDemand("127.0.0.1", 6688));
        deviceListener.startListener();
        maoNetworkCore.addListener(deviceListener);
    }

    @Override
    public void deactivate() {
        maoNetworkCore.removeListener(deviceListener);
        deviceListener.stopListener();
    }

    private class DebugPeerEventListener extends MaoAbstractListener<PeerEvent> {

        @Override
        protected void process(PeerEvent event) {
            switch (event.getType()) {
                case PEER_CONNECT:
                    log.info("peer CONNECT {}, {} {} -> {} {}", event.getPeerId(),
                            event.getMyIp(), event.getMyPort(), event.getPeerIp(), event.getPeerPort());
//                    maoNetworkCore.getPeer(event.getPeerId()).write(String.format("BigMao is Here,request,%d", System.currentTimeMillis()));

                    MaoDeviceId newDeviceId = new MaoDeviceId(event.getPeerIp(), event.getPeerPort());
                    MaoDevice newDevice = new MaoDevice(newDeviceId, MaoDeviceState.DEVICE_UP);
                    devices.put(newDeviceId, newDevice);

                    // not retry, avoid to block the thread of lower layer.
                    postEvent(new DeviceEvent(DeviceEventType.DEVICE_CONNENCTED, newDevice));
                    break;
                case PEER_DATA:
                    log.info("peer DATA {}, {} {} -> {} {}, {}", event.getPeerId(),
                            event.getMyIp(), event.getMyPort(), event.getPeerIp(), event.getPeerPort(),
                            event.getReceivedData());

                    MaoDeviceId deviceId = new MaoDeviceId(event.getPeerIp(), event.getPeerPort());
                    MaoDevice device = devices.get(deviceId);
                    if(device != null) {
                        if (device.getState() == MaoDeviceState.DEVICE_UP) {
                            postEvent(new DeviceEvent(DeviceEventType.DEVICE_DATA_RECEIVED, device, event.getReceivedData()));
                        } else {
                            log.warn("device {} is DOWN", device.getDeviceId());
                        }
                    } else {

                    }


//                    String [] parts = event.getReceivedData().split(",");
//                    switch (parts[1]) {
//                        case "request":
//                            maoNetworkCore.getPeer(event.getPeerId())
//                                    .write(String.format("BigMao is Here,reply,%d", System.currentTimeMillis()));
//                            break;
//                        case "reply":
//                            log.info("E2E Delay: {}ms", System.currentTimeMillis() - Long.parseLong(parts[2]));
//                            try {
//                                Thread.sleep(1000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            maoNetworkCore.getPeer(event.getPeerId()).write(String.format("BigMao is Here,request,%d", System.currentTimeMillis()));
//                            break;
//                    }
                    break;
            }
        }

        @Override
        protected boolean isRelevant(PeerEvent event) {
            switch(event.getType()) {
                case PEER_CONNECT:
                case PEER_DATA:
                    return true;
                default:
                    return false;
            }
        }
    }
}











