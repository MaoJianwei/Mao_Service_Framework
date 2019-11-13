package com.maojianwei.service.framework.incubator.network;

import com.maojianwei.service.framework.lib.MaoAbstractModule;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.maojianwei.service.framework.incubator.network.MaoPeerState.*;
import static java.lang.String.format;

public class MaoNetworkCore extends MaoAbstractModule {

    private Map<Integer, MaoPeer> peers = new HashMap<>();

    private AtomicInteger peerIdGenerator = new AtomicInteger(1);




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
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();

        Bootstrap b = new Bootstrap();
            b.group(bossGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT) // TODO - CHECK
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000) // TODO - VERITY - ATTENTION !!!
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            try {
                                System.out.println(format("initializing pipeline for client channel %s ...", ch.toString()));

                                ChannelPipeline p = ch.pipeline();
                                p.addLast(
                                        //Attention - assume that if we use LengthFieldBasedFrameDecoder, the frame is certainly unbroken.
                                        //2016.09.17
                                        new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,
                                                9, 2, 0, 0),
                                        new MaoProtocolDecoder(),
                                        new MaoProtocolEncoder(),
                                        new MaoProtocolDuplexHandler(getInstance(), getInstance().getNextPeerId())
                                );

                                System.out.println(format("initialized pipeline for client channel %s ...", ch.toString()));
                            } catch (Throwable t) {
                                System.out.println(t.getMessage());
                            }
                        }
                    });

        Channel ch;
        try {
            ch = b.connect("127.0.0.1", 6666).sync().channel();
        } catch (Exception e) {
            System.out.println(String.format("Exception while connecting others: %s, will connect others", e.getMessage()));
            try {
                e.wait();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            return;
        }
        System.out.println(String.format("client channel info Open:%d, Active:%d, RemoteAddress:%s",
                ch.isOpen(),
                ch.isActive(),
                ch.remoteAddress().toString()));
        try {
            ch.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deactivate() {

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
