package com.maojianwei.service.framework.incubator.aaa;

import com.maojianwei.service.framework.incubator.message.queue.MaoAbstractDataReceiver;
import com.maojianwei.service.framework.incubator.message.queue.MaoAbstractListener;
import com.maojianwei.service.framework.incubator.message.queue.event.DeviceEvent;
import com.maojianwei.service.framework.incubator.message.queue.event.PeerEvent;
import com.maojianwei.service.framework.incubator.network.MaoNetworkCore;
import com.maojianwei.service.framework.lib.MaoAbstractModule;
import com.maojianwei.service.framework.lib.MaoReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.maojianwei.service.framework.incubator.message.queue.event.PeerEventType.PEER_NEW;

public class DebugAaaManager extends MaoAbstractModule {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @MaoReference
    private MaoNetworkCore maoNetworkCore;

    private DebugPeerEventListener peerEventListener = new DebugPeerEventListener();

    private DebugAaaManager() {
        super("DebugAaaManager");
    }
    private static DebugAaaManager singletonInstance;
    public static DebugAaaManager getInstance() {
        if (singletonInstance == null) {
            synchronized (DebugAaaManager.class) {
                if (singletonInstance == null) {
                    singletonInstance = new DebugAaaManager();
                }
            }
        }
        return singletonInstance;
    }



    @Override
    public void activate() {
//        maoNetworkCore.addPeerNeeds(new MaoPeerDemand("127.0.0.1", 6688));
        peerEventListener.startListener();
        maoNetworkCore.addListener(peerEventListener);
    }

    @Override
    public void deactivate() {
        maoNetworkCore.removeListener(peerEventListener);
        peerEventListener.stopListener();
    }

    private class AaaDataReceiver extends MaoAbstractDataReceiver {
        @Override
        protected void process(DeviceEvent event) {
            log.info("peer data {}, {} {} -> {} {}, {}",
                    event.
                    event.getPeerId(),
                    event.getMyIp(), event.getMyPort(), event.getPeerIp(), event.getPeerPort(),
                    event.getReceivedData());
            maoNetworkCore.permitConnected(event.getPeerId());
        }
    }

    private class DebugPeerEventListener extends MaoAbstractListener<PeerEvent> {
        @Override
        protected void process(PeerEvent event) {
            switch (event.getType()) {
                case PEER_NEW:
                    log.info("new peer {}, {} {} -> {} {}", event.getPeerId(),
                            event.getMyIp(), event.getMyPort(), event.getPeerIp(), event.getPeerPort());
                    maoNetworkCore.getPeer(event.getPeerId()).write("1,0,AAA it's me");
                    break;
                case PEER_DATA:

                    break;
            }
        }

        @Override
        protected boolean isRelevant(PeerEvent event) {
            return event.getType() == PEER_NEW;
        }
    }
}














