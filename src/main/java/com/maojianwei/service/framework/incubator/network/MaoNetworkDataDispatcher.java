package com.maojianwei.service.framework.incubator.network;

import com.maojianwei.service.framework.incubator.message.queue.MaoAbstractDataReceiver;
import com.maojianwei.service.framework.incubator.message.queue.MaoAbstractListener;
import com.maojianwei.service.framework.incubator.message.queue.event.DeviceEvent;
import com.maojianwei.service.framework.incubator.message.queue.event.DeviceEventType;
import com.maojianwei.service.framework.incubator.message.queue.event.PeerEvent;
import com.maojianwei.service.framework.incubator.message.queue.event.PeerEventType;
import com.maojianwei.service.framework.incubator.node.DebugNodeManager;
import com.maojianwei.service.framework.incubator.node.lib.MaoNodeId;
import com.maojianwei.service.framework.incubator.node.lib.MaoNodeState;
import com.maojianwei.service.framework.lib.MaoAbstractModule;
import com.maojianwei.service.framework.lib.MaoReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import static com.maojianwei.service.framework.incubator.network.lib.MaoDataType.AAA;

public class MaoNetworkDataDispatcher extends MaoAbstractModule {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String TYPE_SPLITER = ",";
    private static final int TYPE_NUMBER = 10;
    private static final int SUB_TYPE_NUMBER = 10;

    @MaoReference
    private MaoNetworkCore networkCore;

    @MaoReference
    private DebugNodeManager nodeManager;

    private PeerDataListener peerDataListener = new PeerDataListener();

    private ReentrantLock[][] receiverLocks = new ReentrantLock[TYPE_NUMBER][SUB_TYPE_NUMBER];
    private Set<MaoAbstractDataReceiver>[][] receiverSets = new HashSet[TYPE_NUMBER][SUB_TYPE_NUMBER];


    private static MaoNetworkDataDispatcher singletonInstance;
    private MaoNetworkDataDispatcher(){
        super("MaoNetworkDataDispatcher");
    }
    public static MaoNetworkDataDispatcher getInstance() {
        if (singletonInstance == null) {
            synchronized(MaoNetworkDataDispatcher.class) {
                if (singletonInstance == null) {
                    singletonInstance = new MaoNetworkDataDispatcher();
                }
            }
        }
        return singletonInstance;
    }


    @Override
    public void activate() {
        peerDataListener.startListener();
        networkCore.addListener(peerDataListener);
    }

    @Override
    public void deactivate() {
        networkCore.removeListener(peerDataListener);
        peerDataListener.stopListener();
    }

    public void registerReceiver(MaoAbstractDataReceiver receiver, int type, int subType) {
        if(type > 0 && type <= TYPE_NUMBER && subType > 0 && subType <= SUB_TYPE_NUMBER) {
            receiverLocks[type][subType].lock();
            receiverSets[type][subType].add(receiver);
            receiverLocks[type][subType].unlock();
        }
    }

    public void unregisterReceiver(MaoAbstractDataReceiver receiver, int type, int subType) {
        if(type > 0 && type <= TYPE_NUMBER && subType > 0 && subType <= SUB_TYPE_NUMBER) {
            receiverLocks[type][subType].lock();
            receiverSets[type][subType].remove(receiver);
            receiverLocks[type][subType].unlock();
        }
    }

    private class PeerDataListener extends MaoAbstractListener<PeerEvent> {

        @Override
        protected void process(PeerEvent event) {
            String data = event.getReceivedData();
            String [] dataSlice = data.split(TYPE_SPLITER);
            if (dataSlice.length > 2) {
                int type = Integer.parseInt(dataSlice[0]);
                int subType = Integer.parseInt(dataSlice[1]);
                if(type > 0 && type <= TYPE_NUMBER && subType > 0 && subType <= SUB_TYPE_NUMBER) {
                    MaoNodeId newDeviceId = new MaoNodeId(event.getPeerIp(), event.getPeerPort());
                    if (type != AAA.get()) {
                        // check device status
                        if (nodeManager.getNode(newDeviceId).getState() != MaoNodeState.DEVICE_UP) {
                            log.warn("Node {} is not online, drop data", newDeviceId);
                            return;
                        }
                    }

                    Set<MaoAbstractDataReceiver> receivers = receiverSets[type][subType];
                    receiverLocks[type][subType].lock();
                    for (MaoAbstractDataReceiver r : receivers) {
                        r.postData(new DeviceEvent(DeviceEventType.DEVICE_DATA_RECEIVED, newDeviceId,
                                LocalDateTime.now().toString(), data));
                    }
                    receiverLocks[type][subType].unlock();
                } else {
                    log.warn("error data type {} subType {}", type, subType);
                }
            } else {
                log.warn("error data without type-subtype, data slice {}", dataSlice.length);
            }
        }

        @Override
        protected boolean isRelevant(PeerEvent event) {
            return event.getType() == PeerEventType.PEER_DATA;
        }
    }
}































