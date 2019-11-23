package com.maojianwei.service.framework.incubator.network;

import com.maojianwei.service.framework.incubator.network.lib.MaoPeer;
import com.maojianwei.service.framework.incubator.network.lib.MaoPeerDemand;
import com.maojianwei.service.framework.lib.MaoAbstractModule;
import com.maojianwei.service.framework.lib.MaoReference;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.maojianwei.service.framework.incubator.network.lib.MaoPeerState.*;

public class MaoNetworkCore extends MaoAbstractModule {

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

    }

    @Override
    public void deactivate() {

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
            System.out.println(String.format("announceNewPeer: peer %d existed.", peerId));
        }
        return peer;
    }


    public void announceConnected(int peerId) {
        MaoPeer peer = peers.get(peerId);
        if (peer != null) {
            peer.setState(CONNECTED);
            // TODO - send to event bus.
        } else {
            System.out.println(String.format("announceConnected: warning, peer %d not existed.", peerId));
        }
    }

    public void announceDisconnected(int peerId) {
        MaoPeer peer = peers.get(peerId);
        if (peer != null) {
            peer.setState(DISCONNECTED);
            // TODO - send to event bus.
        } else {
            System.out.println(String.format("announceDisconnected: warning, peer %d not existed."));
        }
    }

    public void dataReceived(int peerId, String data) {
        MaoPeer peer = peers.get(peerId);
        if (peer != null) {
            System.out.println(String.format("dataReceived: peer %d, data: %s", peer.getId(), data));
            // TODO - send to event bus.
        } else {
            System.out.println(String.format("dataReceived: warning, peer %d not existed."));
        }
    }


    public int getNextPeerId() {
        return peerIdGenerator.getAndAdd(1);
    }
}
