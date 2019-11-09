package com.maojianwei.service.framework.incubator.network;

import io.netty.channel.Channel;

public class MaoPeer {

    private final MaoNetworkCore networkCore;
    private final Channel channel;

    private MaoPeerState state;


    private final String myIp;
    private final String peerIp;
    private final int myPort;
    private final int peerPort;

    private final int id;



    public MaoPeer(MaoNetworkCore networkCore, Channel channel, MaoPeerState state,
                   String myIp, String peerIp, int myPort, int peerPort, int peerId) {
        this.networkCore = networkCore;
        this.channel = channel;
        this.state = state;
        this.myIp = myIp;
        this.peerIp = peerIp;
        this.myPort = myPort;
        this.peerPort = peerPort;
        this.id = peerId;
    }

    public void write(String msg) {
        channel.write(msg);
    }
}
