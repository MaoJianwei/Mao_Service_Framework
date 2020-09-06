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
import static com.maojianwei.service.framework.incubator.network.lib.MaoNetworkConst.*;

public class MaoNetworkDataDispatcher extends MaoAbstractModule {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @MaoReference
    private MaoNetworkCore networkCore;

    @MaoReference
    private DebugNodeManager nodeManager;

    private PeerDataListener peerDataListener = new PeerDataListener();

    private ReentrantLock[][] receiverLocks = new ReentrantLock[TYPE_NUMBER][SUB_TYPE_NUMBER];
    private Set<MaoAbstractDataReceiver>[][] receiverSets = new HashSet[TYPE_NUMBER][SUB_TYPE_NUMBER];


    private static MaoNetworkDataDispatcher singletonInstance;

    private MaoNetworkDataDispatcher() {
        super("MaoNetworkDataDispatcher");
    }

    public static MaoNetworkDataDispatcher getInstance() {
        if (singletonInstance == null) {
            synchronized (MaoNetworkDataDispatcher.class) {
                if (singletonInstance == null) {
                    singletonInstance = new MaoNetworkDataDispatcher();
                }
            }
        }
        return singletonInstance;
    }

    @Override
    public void activate() {
        initRegistry();
        peerDataListener.startListener();
        networkCore.addListener(peerDataListener);
        iAmReady();
    }

    @Override
    public void deactivate() {
        networkCore.removeListener(peerDataListener);
        peerDataListener.stopListener();
        destroyRegistry();
    }

    private void initRegistry() {
        for (int i = 0; i < TYPE_NUMBER; i++) {
            for (int j = 0; j < SUB_TYPE_NUMBER; j++) {
                receiverSets[i][j] = new HashSet<>();
            }
        }
        for (int i = 0; i < TYPE_NUMBER; i++) {
            for (int j = 0; j < SUB_TYPE_NUMBER; j++) {
                receiverLocks[i][j] = new ReentrantLock();
            }
        }
    }

    private void destroyRegistry() {
        for (int i = 0; i < TYPE_NUMBER; i++) {
            for (int j = 0; j < SUB_TYPE_NUMBER; j++) {
                receiverSets[i][j].clear();
                receiverSets[i][j] = null;
            }
        }
        for (int i = 0; i < TYPE_NUMBER; i++) {
            for (int j = 0; j < SUB_TYPE_NUMBER; j++) {
                receiverLocks[i][j] = null;
            }
        }
    }

    public void registerReceiver(MaoAbstractDataReceiver receiver, int type, int subType) {
        if (type > 0 && type <= TYPE_NUMBER && subType > 0 && subType <= SUB_TYPE_NUMBER) {
            receiverLocks[type][subType].lock();
            receiverSets[type][subType].add(receiver);
            receiverLocks[type][subType].unlock();
        }
    }

    public void unregisterReceiver(MaoAbstractDataReceiver receiver, int type, int subType) {
        if (type > 0 && type <= TYPE_NUMBER && subType > 0 && subType <= SUB_TYPE_NUMBER) {
            receiverLocks[type][subType].lock();
            receiverSets[type][subType].remove(receiver);
            receiverLocks[type][subType].unlock();
        }
    }

    private class PeerDataListener extends MaoAbstractListener<PeerEvent> {

        /**
         * Data format:
         * Type,Subtype;upper-layer-data
         * <p>
         * Example:
         * 09,06;Beijing Tower 118.5
         */

        @Override
        protected void process(PeerEvent event) {

            String data = event.getReceivedData();
            if (data.length() <= DATA_SPLITER_INDEX) { // empty upper layer is valid.
                log.warn("error data len {}", data.length());
                return;
            }
            if (!data.startsWith(DATA_SPLITER, DATA_SPLITER_INDEX)) {
                log.warn("error data without DATA_SPLITER, data len {}", data.length());
                return;
            }
            if (!data.startsWith(TYPE_SPLITER, TYPE_SPLITER_INDEX)) {
                log.warn("error data without TYPE_SPLITER, data len {}", data.length());
                return;
            }

            int type, subType;
            try {
                type = Integer.parseInt(data.substring(0, TYPE_SPLITER_INDEX));
                subType = Integer.parseInt(data.substring(TYPE_SPLITER_INDEX + 1, DATA_SPLITER_INDEX));
            } catch (NumberFormatException e) {
                log.warn("error data type-str {} subType-str {}",
                        data.substring(0, TYPE_SPLITER_INDEX), data.substring(0, DATA_SPLITER_INDEX));
                return;
            }

            if (type > 0 && type <= TYPE_NUMBER && subType > 0 && subType <= SUB_TYPE_NUMBER) {
                MaoNodeId deviceId;
                if (type != AAA.get()) {
                    deviceId = new MaoNodeId(event.getPeerIp(), event.getPeerPort());
                    // check device status
                    if (nodeManager.getNode(deviceId).getState() != MaoNodeState.DEVICE_UP) {
                        log.warn("Node {} is not online, drop data", deviceId);
                        return;
                    }
                } else {
                    // use port to carry PeerId to AAA manager.
                    deviceId = new MaoNodeId("", event.getPeerId());
                }

                //FIXME: to avoid memory copy, I want something like slice.
                //String upperData = data.substring(DATA_SPLITER_INDEX + 1);

                Set<MaoAbstractDataReceiver> receivers = receiverSets[type][subType];
                receiverLocks[type][subType].lock();
                for (MaoAbstractDataReceiver r : receivers) {
                    r.postData(new DeviceEvent(DeviceEventType.DEVICE_DATA_RECEIVED, deviceId,
                            LocalDateTime.now().toString(), data));
                }
                receiverLocks[type][subType].unlock();
            } else {
                log.warn("error data type {} subType {}", type, subType);
            }
        }

        @Override
        protected boolean isRelevant(PeerEvent event) {
            return event.getType() == PeerEventType.PEER_DATA;
        }
    }
}































