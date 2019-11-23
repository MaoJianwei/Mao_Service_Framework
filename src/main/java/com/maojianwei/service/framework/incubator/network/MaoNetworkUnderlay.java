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

import java.net.*;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.String.format;

public class MaoNetworkUnderlay extends MaoAbstractModule {

    private static int TCP_BACKLOG_VALUE = 20;
    private static int SERVER_PORT = 6666; // TODO - modify

    @MaoReference
    private MaoNetworkCore maoNetworkCore;

    private Channel serverChannel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private ExecutorService commonTaskPool = Executors.newCachedThreadPool();


    private MaoNetworkUnderlay() {
        super("MaoNetworkUnderlay");
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

            System.out.println(format("bind finish, channel info Open:{}, Active:{}, LocalAddress:{}",
                    serverChannel.isOpen(),
                    serverChannel.isActive(),
                    serverChannel.localAddress().toString()));
        } catch (Throwable t) {
            System.out.println(t.getMessage());
        }

        //bossGroup.schedule(new ConnectTask(this), 0, TimeUnit.SECONDS);

        System.out.println("schedule ConnectTask over");
    }

    @Override
    public void deactivate() {
        try {
            System.out.println("closing serverChannel...");
            serverChannel.close().sync();
            System.out.println("closed serverChannel.");
        } catch (Throwable t) {
            System.out.println(t.getMessage());
        } finally {
            System.out.println("calling shutdownGracefully of workerGroup and bossGroup.");
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            System.out.println("called shutdownGracefully of workerGroup and bossGroup.");
        }
    }

    public void submitConnectDemand(MaoPeerDemand peerDemand) {
        commonTaskPool.submit(new ConnectTask(peerDemand));
    }

    private class ConnectTask implements Runnable {

//        private final Logger log = LoggerFactory.getLogger(getClass());
//        private MaoProtocolNetworkControllerImpl controller;

        MaoPeerDemand peerDemand;
        Bootstrap b;

        public ConnectTask(MaoPeerDemand peerDemand) {//(MaoProtocolNetworkControllerImpl controller) {
//            this.controller = controller;

//            log.info("init Bootstrap...");
            this.peerDemand = peerDemand;


//            log.info("Bootstrap init ok");
        }

        @Override
        public void run() {

//            log.info("New ConnectTask start...");

            if (!peerDemand.isValid()) {
                System.out.println("WARN: Fail to get peer ip" + peerDemand.getIpStr());
            }

            if (peerDemand.getIp() instanceof Inet4Address) {
                Inet4Address localIpv4 = getLocalIpv4();
                if (localIpv4 != null) {
                    if (!verifyActiveConnectionRule(localIpv4, peerDemand.getIp())) {
                        System.out.println("Local is bigger, ignore " + peerDemand.getIpStr());
                        return;
                    }
                } else {
//                    log.error("Local Ip is unavailable!(null)");
                    return;
                }
            } else {
                //TODO - ipv6
                return;
            }

//            log.info("connecting to {}...", nodeIp);
            System.out.println("connecting to " + peerDemand.getIpStr());

            // wait Network Underlay finish to be activated
            while(bossGroup == null) {
                try {
                    System.out.println("wait");
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
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000) // TODO - VERITY - ATTENTION !!!
                    .handler(new NetworkChannelInitializer(maoNetworkCore));

            Channel ch;
            try {
                ch = b.connect(peerDemand.getIpStr(), peerDemand.getPort()).sync().channel();
            } catch (Exception e) {
//                log.warn("Exception while connecting others: {}, will connect others", e.getMessage());
                System.out.println("Exception while connecting others: " + e.getMessage());
            }

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

        private Inet4Address getLocalIpv4() {

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
                        if (ip instanceof Inet4Address) {
                            return (Inet4Address) ip;
                        } else if (ip instanceof Inet6Address) {
                            //TODO

                            // localAddresses.add(addresses.nextElement().getHostAddress().split("%")[0]);
                            // split() serve to IPv6,
                            // because the value will be such as 2001:da8:215:389:9097:e45c:94b0:9be5%ens32

                            // For ipv6, false==isSiteLocalAddress() and false==isLinkLocalAddress()
                            // can be simply considered as Global routable address
                        } else {
                            System.out.println("WARN: Unsupported Address, not ipv4 or ipv6, " + ip.toString());
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

        private MaoNetworkCore networkCore;
        //private boolean isRoleClient;

        public NetworkChannelInitializer(MaoNetworkCore networkCore) {//, boolean isRoleClient) {
            this.networkCore = networkCore;
            //this.isRoleClient = isRoleClient;
        }

        @Override
        public void initChannel(SocketChannel ch) {
            try {
                System.out.println(format("initializing pipeline for channel %s ...", ch.toString()));

                ChannelPipeline p = ch.pipeline();
                p.addLast(
                        //Attention - assume that if we use LengthFieldBasedFrameDecoder, the frame is certainly unbroken.
                        //2016.09.17
                        new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,
                                9, 2, 0, 0),
                        new MaoProtocolDecoder(),
                        new MaoProtocolEncoder(),
                        new MaoProtocolDuplexHandler(networkCore, networkCore.getNextPeerId())
                        );

                System.out.println(format("initialized pipeline for channel %s ...", ch.toString()));
            } catch (Throwable t) {
                System.out.println(t.getMessage());
            }
        }
    }
}
