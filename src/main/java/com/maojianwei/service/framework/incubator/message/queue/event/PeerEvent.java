package com.maojianwei.service.framework.incubator.message.queue.event;

public class PeerEvent {

    private final PeerEventType type;

    private final int peerId;

    private final String myIp;
    private final String peerIp;
    private final int myPort;
    private final int peerPort;

    private final String timestamp;

    private final String receivedData;


    public PeerEvent(PeerEventType type, int peerId, String myIp, String peerIp, int myPort, int peerPort,
                     String timestamp, String receivedData) {
        this.type = type;
        this.peerId = peerId;
        this.myIp = myIp;
        this.peerIp = peerIp;
        this.myPort = myPort;
        this.peerPort = peerPort;
        this.timestamp = timestamp;
        this.receivedData = receivedData;
    }



    public PeerEventType getType() {
        return type;
    }

    public int getPeerId() {
        return peerId;
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

    public String getTimestamp() {
        return timestamp;
    }

    public String getReceivedData() {
        return receivedData;
    }

    @Override
    public String toString() {
        return "PeerEvent{" +
                "type=" + type +
                ", peerId=" + peerId +
                ", myIp='" + myIp + '\'' +
                ", peerIp='" + peerIp + '\'' +
                ", myPort=" + myPort +
                ", peerPort=" + peerPort +
                ", timestamp=" + timestamp +
                ", receivedData='" + receivedData + '\'' +
                '}';
    }
}
