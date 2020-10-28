package com.maojianwei.service.framework.incubator.node;

import com.maojianwei.service.framework.incubator.message.queue.MaoAbstractListener;
import com.maojianwei.service.framework.incubator.message.queue.event.DeviceEvent;
import com.maojianwei.service.framework.incubator.message.queue.event.DeviceEventType;
import com.maojianwei.service.framework.incubator.message.queue.event.PeerEvent;
import com.maojianwei.service.framework.incubator.network.MaoNetworkCore;
import com.maojianwei.service.framework.incubator.network.lib.MaoPeer;
import com.maojianwei.service.framework.incubator.network.lib.MaoPeerDemand;
import com.maojianwei.service.framework.incubator.node.lib.MaoNode;
import com.maojianwei.service.framework.incubator.node.lib.MaoNodeId;
import com.maojianwei.service.framework.incubator.node.lib.MaoNodeState;
import com.maojianwei.service.framework.lib.MaoAbstractModule;
import com.maojianwei.service.framework.lib.MaoReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DebugNodeManager extends MaoAbstractModule<DeviceEvent, MaoAbstractListener<DeviceEvent>> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @MaoReference
    private MaoNetworkCore maoNetworkCore;

    private Map<MaoNodeId, MaoNode> devices = new HashMap<>();

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
        startSink();
        deviceListener.startListener();
        maoNetworkCore.addListener(deviceListener);
        iAmReady();
    }

    @Override
    public void deactivate() {
        maoNetworkCore.removeListener(deviceListener);
        deviceListener.stopListener();
        stopSink();
        iAmDone();
    }

    public MaoNode getNode(MaoNodeId nodeId) {
        return devices.get(nodeId);
    }



    private class DebugPeerEventListener extends MaoAbstractListener<PeerEvent> {

        @Override
        protected void process(PeerEvent event) {
            switch (event.getType()) {
                case PEER_CONNECT:
                    log.info("peer CONNECT {}, {} {} -> {} {}", event.getPeerId(),
                            event.getMyIp(), event.getMyPort(), event.getPeerIp(), event.getPeerPort());
//                    maoNetworkCore.getPeer(event.getPeerId()).write(String.format("BigMao is Here,request,%d", System.currentTimeMillis()));

                    MaoNodeId newDeviceId = new MaoNodeId(event.getPeerIp(), event.getPeerPort());
                    MaoPeer peer = maoNetworkCore.getPeer(event.getPeerId());
                    MaoNode newDevice;
                    if (peer != null) {
                        newDevice = new MaoNode(peer, newDeviceId, MaoNodeState.DEVICE_UP);
                    } else {
                        log.error("Peer not found for {}", newDeviceId);
                        newDevice = new MaoNode(newDeviceId);
                    }
                    devices.put(newDeviceId, newDevice);

                    // not retry, avoid to block the thread of lower layer.
                    postEvent(new DeviceEvent(DeviceEventType.DEVICE_CONNENCTED, newDeviceId));
                    break;
            }
        }

        @Override
        protected boolean isRelevant(PeerEvent event) {
            switch(event.getType()) {
                case PEER_CONNECT:
                //case PEER_DATA:
                    return true;
                default:
                    return false;
            }
        }
    }
}











