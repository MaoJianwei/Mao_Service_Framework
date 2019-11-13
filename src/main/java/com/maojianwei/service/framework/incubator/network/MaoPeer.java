package com.maojianwei.service.framework.incubator.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

public class MaoPeer {

    private final MaoNetworkCore networkCore;
    private final Channel channel;

    private final int id;
    private MaoPeerState state;

    private final String myIp;
    private final String peerIp;
    private final int myPort;
    private final int peerPort;


    private long connectedTimestamp;
    private long disconnectedTimestamp;




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

    public void announceConnected() {
        networkCore.announceConnected(id);
    }

    public void announceDisconnected() {
        networkCore.announceDisconnected(id);
    }

    public void dataReceived(String data) {
        networkCore.dataReceived(id, data);
    }

    public ChannelFuture write(String msg) {
        return channel.write(msg);
    }



    public void setState(MaoPeerState state) {
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public MaoPeerState getState() {
        return state;
    }

    public String getMyIp() {
        return myIp;
    }

    public String getPeerIp() {
        return peerIp;
    }

    public int getMyPort() {
        return myPort;
    }

    public int getPeerPort() {
        return peerPort;
    }
}
