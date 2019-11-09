package com.maojianwei.service.framework.incubator.network;

import com.maojianwei.service.framework.lib.MaoAbstractModule;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MaoNetworkCore extends MaoAbstractModule {

    private Map<Integer, MaoPeer> peers = new HashMap<>();

    private AtomicInteger peerIdGenerator = new AtomicInteger(1);


    public MaoNetworkCore() {
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





    public MaoPeer announceNewPeer() {

    }


    public void announceConnected(int peerId, Channel channel) {
        MaoPeer newPeer = new MaoPeer(ip, channel);
        peers.put(ip, newPeer);
        newPeer.write("Beijing Tower 118.5");
    }

    public void announceDisconnected(int peerId) {
        peers.remove(ip);
    }

    public void dataReceived(int peerId, String data) {
        System.out.println(String.format("IP: %s, Data: %s", ip, data));
    }


    public int getNextPeerId() {
        return peerIdGenerator.getAndAdd(1);
    }
}
