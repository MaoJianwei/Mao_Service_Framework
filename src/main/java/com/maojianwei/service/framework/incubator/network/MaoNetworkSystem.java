package com.maojianwei.service.framework.incubator.network;

import com.maojianwei.service.framework.lib.MaoAbstractModule;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import static java.lang.String.format;

public class MaoNetworkSystem extends MaoAbstractModule {

    private static int TCP_BACKLOG_VALUE = 20;
    private static int SERVER_PORT = 6666; // TODO - modify

    private Channel serverChannel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public MaoNetworkSystem() {
        super("MaoNetworkSystem");
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
                .childHandler(new NetworkChannelInitializer(MaoNetworkCore.getInstance()));


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


//
//    private class ConnectTask implements Runnable {
//
//        private final Logger log = LoggerFactory.getLogger(getClass());
//        private MaoProtocolNetworkControllerImpl controller;
//
//        Bootstrap b = new Bootstrap();
//
//        public ConnectTask(MaoProtocolNetworkControllerImpl controller) {
//            this.controller = controller;
//
//            log.info("init Bootstrap...");
//            b.group(controller.bossGroup)
//                    .channel(NioSocketChannel.class)
//                    .option(ChannelOption.TCP_NODELAY, true)
//                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT) // TODO - CHECK
//                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000) // TODO - VERITY - ATTENTION !!!
//                    .handler(new NetworkChannelInitializer(controller, true));
//            log.info("Bootstrap init ok");
//        }
//
//        @Override
//        public void run() {
//
//            log.info("New ConnectTask start...");
//
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
//                log.info("client channel info Open:{}, Active:{}, RemoteAddress:{}",
//                        ch.isOpen(),
//                        ch.isActive(),
//                        ch.remoteAddress().toString());
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
//
//
//            bossGroup.schedule(this, 30, TimeUnit.SECONDS);
//        }
//
//        private boolean verifyActiveConnectionRule(InetAddress localIp, InetAddress remoteIp) {
//            if (localIp.getClass().equals(remoteIp.getClass())) {
//                byte[] localIpBytes = localIp.getAddress();
//                byte[] remoteIpBytes = remoteIp.getAddress();
//
//                for (int i = 0; i < localIpBytes.length; i++) {
//                    if (localIpBytes[i] < remoteIpBytes[i]) {
//                        log.info("Verify Pass, local: {}, remote: {}",
//                                localIp.getHostAddress(), remoteIp.getHostAddress());
//                        return true;
//                    } else if (localIpBytes[i] > remoteIpBytes[i]) {
//                        log.info("Verify Deny, local: {}, remote: {}",
//                                localIp.getHostAddress(), remoteIp.getHostAddress());
//                        return false;
//                    }
//                }
//                log.error("Local Ip is equal to Remote Ip! Please troubleshoot!" + EOL +
//                        "localIp: {}, remoteIp: {}", localIp.getHostAddress(), remoteIp.getHostAddress());
//            } else {
//                log.error("IP type not match! localIp: {}, remoteIp: {}",
//                        localIp.getClass(), remoteIp.getClass());
//            }
//            return false;
//        }
//    }


    private class NetworkChannelInitializer extends ChannelInitializer<SocketChannel> {

        private MaoNetworkCore networkCore;
        //private boolean isRoleClient;

        public NetworkChannelInitializer(MaoNetworkCore networkCore) {//, boolean isRoleClient) {
            this.networkCore = networkCore;
            //this.isRoleClient = isRoleClient;
        }

        @Override
        public void initChannel(SocketChannel ch) {
            try {
                System.out.println(format("initializing pipeline for channel $s ...", ch.toString()));

                ChannelPipeline p = ch.pipeline();
                p.addLast(
                        //Attention - assume that if we use LengthFieldBasedFrameDecoder, the frame is certainly unbroken.
                        //2016.09.17
                        new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,
                                12, 4, 0, 0),
                        new MaoProtocolDecoder(),
                        new MaoProtocolEncoder()
                        //new MaoProtocolDuplexHandler(networkCore)
                        );

                System.out.println(format("initializing pipeline for channel $s ...", ch.toString()));
            } catch (Throwable t) {
                System.out.println(t.getMessage());
            }
        }
    }
}
