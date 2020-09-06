package com.maojianwei.service.framework.incubator.network;

import com.maojianwei.service.framework.incubator.network.lib.MaoPeerDemand;
import com.maojianwei.service.framework.lib.MaoAbstractModule;
import com.maojianwei.service.framework.lib.MaoReference;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MaoNetworkUnderlay extends MaoAbstractModule {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static int TCP_BACKLOG_VALUE = 20;
    private static int SERVER_PORT = 6666; // TODO - modify

    @MaoReference
    private MaoNetworkCore maoNetworkCore;

    private Channel serverChannel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private Set<MaoPeerDemand> connectDemands = new HashSet<>();
    private ExecutorService commonTaskPool = Executors.newCachedThreadPool();


    private MaoNetworkUnderlay() {
        super("MaoNetworkUnderlay");
        iAmReady(); // no need to wait for that maoNetworkCore is ready.
    }

    private static MaoNetworkUnderlay singletonInstance;

    public static MaoNetworkUnderlay getInstance() {
        if (singletonInstance == null) {
            synchronized (MaoNetworkUnderlay.class) {
                if (singletonInstance == null) {
                    singletonInstance = new MaoNetworkUnderlay();
                }
            }
        }
        return singletonInstance;
    }

    @Override
    public void activate() {

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();


        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, TCP_BACKLOG_VALUE)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT) // TODO - CHECK
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT) // TODO - CHECK
                .childHandler(new NetworkChannelInitializer(maoNetworkCore));

        try {
            serverChannel = serverBootstrap.bind(SERVER_PORT).sync().channel();

            log.info("bind finish, channel info Open: {}, Active: {}, LocalAddress: {}",
                    serverChannel.isOpen(),
                    serverChannel.isActive(),
                    serverChannel.localAddress().toString());
        } catch (Throwable t) {
            log.warn(t.getMessage());
        }

        //bossGroup.schedule(new ConnectTask(this), 0, TimeUnit.SECONDS);

        log.info("schedule ConnectTask over");
        iAmReady();
    }

    @Override
    public void deactivate() {
        try {
            log.debug("closing serverChannel...");
            serverChannel.close().sync();
            log.info("closed serverChannel.");
        } catch (Throwable t) {
            log.warn(t.getMessage());
        } finally {
            log.debug("calling shutdownGracefully of workerGroup and bossGroup.");
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            log.info("called shutdownGracefully of workerGroup and bossGroup.");
        }
    }

    public void submitConnectDemand(MaoPeerDemand peerDemand) {
        ConnectTask task = new ConnectTask(peerDemand);
        addConnectDemand(peerDemand);
        commonTaskPool.submit(task);
    }

    public void withdrawConnectDemand(MaoPeerDemand peerDemand) {
        removeConnectDemand(peerDemand);
    }

    public void addConnectDemand(MaoPeerDemand peerDemand) {
        synchronized (connectDemands) {
            connectDemands.add(peerDemand);
        }
    }

    public void removeConnectDemand(MaoPeerDemand peerDemand) {
        synchronized (connectDemands) {
            connectDemands.remove(peerDemand);
        }
    }

    private class ConnectTask implements Runnable {

//        private final Logger log = LoggerFactory.getLogger(getClass());
//        private MaoProtocolNetworkControllerImpl controller;

        MaoPeerDemand peerDemand;
        Bootstrap b;

        public ConnectTask(MaoPeerDemand peerDemand) { //(MaoProtocolNetworkControllerImpl controller) {
//            this.controller = controller;
//            log.info("init Bootstrap...");
            this.peerDemand = peerDemand;
//            log.info("Bootstrap init ok");
        }

        @Override
        public void run() {

//            log.info("New ConnectTask start...");

            if (!peerDemand.isValid()) {
                log.warn("Fail to get peer ip {}", peerDemand.getIpStr());
            }


            InetAddress localIp = null;
            if (peerDemand.getIp() instanceof Inet4Address) {
                localIp = getLocalIp(false);
            } else if (peerDemand.getIp() instanceof Inet6Address) {
                localIp = getLocalIp(true);
            } else {
                log.warn("peer address family can not be recognized. {}", peerDemand.getIp());
                return;
            }

            if (localIp != null) {
                if (!verifyActiveConnectionRule(localIp, peerDemand.getIp())) {
                    log.info("Local is bigger, ignore {}", peerDemand.getIpStr());
                    return;
                }
            } else {
                log.error("Local IP is unavailable! {}", localIp);
                return;
            }

//            log.info("connecting to {}...", nodeIp);
            log.info("connecting to {}", peerDemand.getIpStr());

            // wait Network Underlay finish to be activated
            while (bossGroup == null) {
                try {
                    log.info("wait");
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    return;
                }
            }
            b = new Bootstrap();
            b.group(bossGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT) // TODO - CHECK
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 20000) // TODO - VERITY - ATTENTION !!!
                    .handler(new NetworkChannelInitializer(maoNetworkCore));

//            Channel ch;
//            try {
            b.connect(peerDemand.getIpStr(), peerDemand.getPort()).addListener(future -> {
                boolean s = future.isSuccess();
                removeConnectDemand(peerDemand);
            });
//            } catch (InterruptedException e) {
//                log.warn("Exception while connecting {}: {}", peerDemand.getIpStr(), e.getMessage());
//            }


//            while (true) {
//                InetAddress nodeIp = controller.agent.getOneUnConnectedNode();
//                log.info("get a new nodeIp: {}", nodeIp);
//                if (nodeIp == null) {
//                    break;
//                }
//                if (isIpv4(nodeIp)) {
//                    Inet4Address ipv4 = controller.agent.getLocalIpv4();
//                    if (ipv4 != null) {
//                        if (!verifyActiveConnectionRule(ipv4, nodeIp)) {
//                            continue;
//                        }
//                    } else {
//                        log.error("Local Ip is unavailable!(null)");
//                        break;
//                    }
//                } else {
//                    //TODO - ipv6
//                    continue;
//                }
//
//
//                log.info("connecting to {}...", nodeIp);
//                Channel ch;
//                try {
//                    ch = b.connect(nodeIp, SERVER_PORT).sync().channel();
//                } catch (Exception e) {
//                    log.warn("Exception while connecting others: {}, will connect others", e.getMessage());
//                    continue;
//                }
////                log.info("client channel info Open:{}, Active:{}, RemoteAddress:{}",
////                        ch.isOpen(),
////                        ch.isActive(),
////                        ch.remoteAddress().toString());
//
//                if (ch.isActive()) {
//                    //TODO - CHECK - if it will be OPEN but not ACTIVE when success?
//
//                    MPHello hello = MPFactories.getFactory(MPVersion.MP_03)
//                            .buildHello()
//                            .setNodeName("MaoTestB")
//                            .setNodePassword("987654321")
//                            .build();
//
//                    log.info("sending MPHello, Type:{}, Version:{}, idHashValue:{}",
//                            hello.getType(),
//                            hello.getVersion(),
//                            hello.getHashValue());
//                    ch.writeAndFlush(hello);
//                    log.info("sent MPHello, Type:{}, Version:{}, idHashValue:{}",
//                            hello.getType(),
//                            hello.getVersion(),
//                            hello.getHashValue());
//                }
//            }
//            bossGroup.schedule(this, 30, TimeUnit.SECONDS);
        }

        private InetAddress getLocalIp(boolean ipv6) {

//        Set<String> localAddresses = new HashSet<>();
            try {
                Enumeration<NetworkInterface> intfs = NetworkInterface.getNetworkInterfaces();
                while (intfs.hasMoreElements()) {
                    NetworkInterface intf = intfs.nextElement();
                    if (intf.isLoopback() || intf.isVirtual() || !intf.isUp()) {
                        continue;
                    }

                    Enumeration<InetAddress> addresses = intf.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress ip = addresses.nextElement();
                        if (ipv6 && ip instanceof Inet6Address) {
                            Inet6Address ip6 = (Inet6Address) ip;
                            if (!ip6.isSiteLocalAddress() && !ip6.isLinkLocalAddress()) {
                                return ip6;
                            }
                            //TODO

                            // localAddresses.add(addresses.nextElement().getHostAddress().split("%")[0]);
                            // split() serve to IPv6,
                            // because the value will be such as 2001:da8:215:389:9097:e45c:94b0:9be5%ens32

                            // For ipv6, false==isSiteLocalAddress() and false==isLinkLocalAddress()
                            // can be simply considered as Global routable address
                        } else if (!ipv6 && ip instanceof Inet4Address) {
                            return ip;
                        } else {
                            //log.warn("Unsupported Address Family, not ipv4 or ipv6: {}", ip.toString());
                        }
                    }
                }
            } catch (SocketException e) {
//                log.warn("Can not gain local IP, because: {}", e.getMessage());
            } catch (Exception e) {
//                log.error("Error got: " + e.getMessage());
            }
            return null;
        }

        /**
         * Small ip connects to Large ip.
         *
         * @param localIp
         * @param remoteIp
         * @return
         */
        private boolean verifyActiveConnectionRule(InetAddress localIp, InetAddress remoteIp) {
            if (localIp.getClass().equals(remoteIp.getClass())) {
                byte[] localIpBytes = localIp.getAddress();
                byte[] remoteIpBytes = remoteIp.getAddress();

                for (int i = 0; i < localIpBytes.length; i++) {
                    if (localIpBytes[i] < remoteIpBytes[i]) {
//                        log.info("Verify Pass, local: {}, remote: {}",
//                                localIp.getHostAddress(), remoteIp.getHostAddress());
                        return true;
                    } else if (localIpBytes[i] > remoteIpBytes[i]) {
//                        log.info("Verify Deny, local: {}, remote: {}",
//                                localIp.getHostAddress(), remoteIp.getHostAddress());
                        return false;
                    }
                }
//                log.error("Local Ip is equal to Remote Ip! Please troubleshoot!" + EOL +
//                        "localIp: {}, remoteIp: {}", localIp.getHostAddress(), remoteIp.getHostAddress());
            } else {
//                log.error("IP type not match! localIp: {}, remoteIp: {}",
//                        localIp.getClass(), remoteIp.getClass());
            }
            return false;
        }
    }


    private static class NetworkChannelInitializer extends ChannelInitializer<SocketChannel> {

        private final Logger log = LoggerFactory.getLogger(getClass());

        private MaoNetworkCore networkCore;
        //private boolean isRoleClient;

        public NetworkChannelInitializer(MaoNetworkCore networkCore) {//, boolean isRoleClient) {
            this.networkCore = networkCore;
            //this.isRoleClient = isRoleClient;
        }

        @Override
        public void initChannel(SocketChannel ch) {
            try {
                log.info("initializing pipeline for channel {} ...", ch.toString());

                ChannelPipeline p = ch.pipeline();
                p.addLast(
                        //Attention - assume that if we use LengthFieldBasedFrameDecoder, the frame is certainly unbroken.
                        //2016.09.17
                        new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,
                                9, 2, -11, 0),
                        new MaoProtocolDecoder(),
                        new MaoProtocolEncoder(),
                        new MaoProtocolDuplexHandler(networkCore, networkCore.getNextPeerId())
                );

                log.info("initialized pipeline for channel {} ...", ch.toString());
            } catch (Throwable t) {
                log.warn(t.getMessage());
            }
        }
    }
}
