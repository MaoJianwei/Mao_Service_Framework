package com.maojianwei.service.framework.incubator.network;

import com.maojianwei.service.framework.incubator.network.lib.MaoPeer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mao on 2016/9/17.
 */
public class MaoProtocolDuplexHandler extends ChannelDuplexHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    //private final boolean isRoleClient;

    private MaoNetworkCore networkCore;
    private final int peerId;

    private MaoPeer peer;

    //private MaoProtocolNode maoProtocolNode;
    //private MaoProtocolState state;
    //private Channel channel;
    //private String ip;

    public MaoProtocolDuplexHandler(MaoNetworkCore networkCore, int peerId) {
        this.networkCore = networkCore;
        this.peerId = peerId;


        //this.isRoleClient = isRoleClient;
        //state = MaoProtocolState.INIT;
    }

//
//    private MaoProtocolState getState(){
//        return this.state;
//    }
//    private void setState(MaoProtocolState newState){
//        this.state = newState;
//    }
//    enum MaoProtocolState {
//
//        /**
//         * Before TCP channel is Active.
//         */
//        INIT{
//
//        },
//
//        WAIT_HELLO{
//            @Override
//            void processHelloMessage(ChannelHandlerContext ctx, MPHello mpHello){
//
//                //TODO - below is hello world Test
//                ChannelHandler channelHandler = ctx.handler();
//                MaoProtocolDuplexHandler h = (MaoProtocolDuplexHandler) channelHandler;
//
//                log.info("ready to process a MPHello, state:{} Type: {}, Version: {}, idHashValue: {}",
//                        h.getState(),
//                        mpHello.getType(),
//                        mpHello.getVersion(),
//                        mpHello.getHashValue());
//
//
//
//                if(!h.isRoleClient) {
//                    log.info("My role is Server, ready to send hello as a reply.");
//                    if (mpHello.getVersion().get() >= MPVersion.MP_03.get()) {
//
//                        log.info("will generate a hello as a reply.");
//                        MPFactory factory = MPFactories.getFactory(MPVersion.MP_03);
//                        MPHello hello = factory.buildHello()
//                                .setNodeName("MaoTestA")
//                                .setNodePassword("123456789")
//                                .build();
//
//                        log.info("will writeAndFlush a hello as a reply, Type: {}, Version: {}, idHashValue: {}",
//                                hello.getType(),
//                                hello.getVersion(),
//                                hello.getHashValue());
//                        ctx.writeAndFlush(hello);
//                    } else {
//                        log.warn("version is not match to anyone!");
//                    }
//                }
//
//                log.info("will get new MaoProtocolNode representation...");
//                h.maoProtocolNode = h.controller.getMaoProtocolNode(
//                        ctx.channel().remoteAddress().toString().split(":")[0].replace("/",""));
//                log.info("got a new MaoProtocolNode representation, {}", h.maoProtocolNode.getAddress());
//
//                log.info("ready to announce maoProtocolNode connected...");
//                h.maoProtocolNode.announceConnected();
//                log.info("announce maoProtocolNode connected, finished");
//
//                h.setState(ACTIVE);
//                log.info("state should go to ACTIVE, state: {}", h.getState());
//            };
//        },
//        ACTIVE{
//
//        },
//        GOODDAY,
//        ENDING,
//        END;
//
//
//        private void processMPMessage(ChannelHandlerContext ctx, MPMessage mpMessage){
//
//            log.info("ready to process a MPMessage, state:{} Type: {}, Version: {}",
//                    ((MaoProtocolDuplexHandler)ctx.handler()).getState(),
//                    mpMessage.getType(),
//                    mpMessage.getVersion());
//
//            switch(mpMessage.getType()){
//                case HELLO:
//                    processHelloMessage(ctx, (MPHello) mpMessage);
//                    break;
//                case ECHO_REQUEST:
//                    processEchoRequestMessage();
//                    break;
//                case ECHO_REPLY:
//                    processEchoReplyMessage();
//                    break;
//                case GOODDAY:
//                    processGoodDayMessage();
//                    break;
//                default:
//                    //TODO - throw
//                    log.warn("go across all case to default in processMPMessage(), channel's String: {}",
//                            ctx.channel().toString());
//            }
//        }
//
//        void processHelloMessage(ChannelHandlerContext ctx, MPHello mpHello){
//            log.warn("go into default processHelloMessage()");
//        };
//        void processEchoRequestMessage(){
//            log.warn("go into default processEchoRequestMessage()");
//
//            //TODO - send EchoReply
//
//            //get version
//            //MPVersion version;
//            //MPFactories.
//
//        };
//        void processEchoReplyMessage(){
//            log.warn("go into default processEchoReplyMessage()");
//        };
//        void processGoodDayMessage(){
//            log.warn("go into default processGoodDayMessage()");
//        };
//    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();

        String [] local = channel.localAddress().toString().split(":");
        String [] remote = channel.remoteAddress().toString().split(":");

        String myIp = local[0].replace("/", "");
        String peerIp = remote[0].replace("/", "");
        int myPort = Integer.parseInt(local[1]);
        int peerPort = Integer.parseInt(remote[1]);

        peer = networkCore.announceNewPeer(channel, peerId, myIp, peerIp, myPort, peerPort);
//        peer.announceConnected();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        peer.announceDisconnected();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof String) {
            peer.dataReceived((String) msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        //IdleTimeout
        //...
        log.warn("userEventTriggered, {}", evt.toString());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //MPParseError
        //...
        log.warn("exceptionCaught, {}", cause.getMessage());
    }
}
