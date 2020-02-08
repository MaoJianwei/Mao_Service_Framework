package com.maojianwei.service.framework.incubator.network;

import com.maojianwei.service.framework.incubator.message.queue.MaoAbstractListener;
import com.maojianwei.service.framework.incubator.message.queue.MaoSink;
import com.maojianwei.service.framework.incubator.message.queue.event.PeerEvent;
import com.maojianwei.service.framework.incubator.message.queue.event.PeerEventType;
import com.maojianwei.service.framework.incubator.network.lib.MaoPeer;
import com.maojianwei.service.framework.incubator.network.lib.MaoPeerDemand;
import com.maojianwei.service.framework.lib.MaoAbstractModule;
import com.maojianwei.service.framework.lib.MaoReference;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.maojianwei.service.framework.incubator.network.lib.MaoPeerState.*;

public class MaoNetworkCore extends MaoAbstractModule<PeerEvent, MaoAbstractListener<PeerEvent>> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @MaoReference
    private MaoNetworkUnderlay maoNetworkUnderlay;

    private Map<Integer, MaoPeer> peers = new HashMap<>();
    private AtomicInteger peerIdGenerator = new AtomicInteger(1);

    private Set<MaoPeerDemand> peerDemands = new HashSet<>();


    private MaoNetworkCore() {
        super("MaoNetworkCore");
    }
    private static MaoNetworkCore singletonInstance;
    public static MaoNetworkCore getInstance() {
        if (singletonInstance == null) {
            synchronized (MaoNetworkCore.class) {
                if (singletonInstance == null) {
                    singletonInstance = new MaoNetworkCore();
                }
            }
        }
        return singletonInstance;
    }

    @Override
    public void activate() {
        startSink();
    }

    @Override
    public void deactivate() {
        stopSink();
    }




    public void addPeerNeeds(MaoPeerDemand peerDemand) {
        if (!peerDemands.contains(peerDemand)) {
            peerDemands.add(peerDemand);
            maoNetworkUnderlay.submitConnectDemand(peerDemand);
        }
    }




    public MaoPeer announceNewPeer(Channel channel, int peerId,
                                   String myIp, String peerIp, int myPort, int peerPort) {
        MaoPeer peer = peers.get(peerId);
        if (peer == null) {
            peer = new MaoPeer(this, channel, INIT, myIp, peerIp, myPort, peerPort, peerId);
            peers.put(peerId, peer);
        } else {
            log.info("announceNewPeer: peer {} existed.", peerId);
        }
        return peer;
    }


    public void announceConnected(int peerId) {
        MaoPeer peer = peers.get(peerId);
        if (peer != null) {
            peer.setState(CONNECTED);
            postEvent(new PeerEvent(PeerEventType.DEVICE_CONNENCTED, peerId,
                    peer.getMyIp(), peer.getPeerIp(), peer.getMyPort(), peer.getPeerPort(),
                    LocalDateTime.now().toString(), null),3);
        } else {
            log.info("announceConnected: warning, peer {} not existed.", peerId);
        }
    }

    public void announceDisconnected(int peerId) {
        MaoPeer peer = peers.get(peerId);
        if (peer != null) {
            peer.setState(DISCONNECTED);
            // TODO - send to event bus.
            postEvent(new PeerEvent(PeerEventType.DEVICE_DISCONNECTED, peerId,
                    peer.getMyIp(), peer.getPeerIp(), peer.getMyPort(), peer.getPeerPort(),
                    LocalDateTime.now().toString(), null),3);
        } else {
            log.warn("announceDisconnected: peer {} not existed.", peerId);
        }
    }

    public void dataReceived(int peerId, String data) {
        MaoPeer peer = peers.get(peerId);
        if (peer != null) {
            log.info("dataReceived: peer {}, data: {}", peer.getId(), data);
            // TODO - send to event bus.
            postEvent(new PeerEvent(PeerEventType.DEVICE_DISCONNECTED, peerId,
                    peer.getMyIp(), peer.getPeerIp(), peer.getMyPort(), peer.getPeerPort(),
                    LocalDateTime.now().toString(), data),3);
        } else {
            log.warn("dataReceived: peer {} not existed.", peerId);
        }
    }


    public int getNextPeerId() {
        return peerIdGenerator.getAndAdd(1);
    }
}
