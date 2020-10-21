package com.maojianwei.service.framework.incubator.aaa;

import com.maojianwei.service.framework.incubator.aaa.lib.MaoAaaData;
import com.maojianwei.service.framework.incubator.message.queue.MaoAbstractDataReceiver;
import com.maojianwei.service.framework.incubator.message.queue.MaoAbstractListener;
import com.maojianwei.service.framework.incubator.message.queue.event.DeviceEvent;
import com.maojianwei.service.framework.incubator.message.queue.event.PeerEvent;
import com.maojianwei.service.framework.incubator.network.MaoNetworkCore;
import com.maojianwei.service.framework.incubator.network.MaoNetworkDataDispatcher;
import com.maojianwei.service.framework.lib.MaoAbstractModule;
import com.maojianwei.service.framework.lib.MaoReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.maojianwei.service.framework.incubator.message.queue.event.PeerEventType.PEER_NEW;
import static com.maojianwei.service.framework.incubator.network.lib.MaoDataType.AAA_ACCEPT;
import static com.maojianwei.service.framework.incubator.network.lib.MaoDataType.AAA_AUTH;
import static com.maojianwei.service.framework.incubator.network.lib.MaoNetworkConst.DATA_SPLITER_INDEX;
import static com.maojianwei.service.framework.incubator.node.lib.MaoNodeId.NODEID_SPLITER;

public class DebugAaaManager extends MaoAbstractModule {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @MaoReference
    private MaoNetworkDataDispatcher networkDataDispatcher;

    @MaoReference
    private MaoNetworkCore maoNetworkCore;

    private DebugPeerEventListener peerEventListener = new DebugPeerEventListener();
    private AaaDataReceiver aaaDataReceiver = new AaaDataReceiver();

    private DebugAaaManager() {
        super("DebugAaaManager");
    }
    private static DebugAaaManager singletonInstance;
    public static DebugAaaManager getInstance() {
        if (singletonInstance == null) {
            synchronized (DebugAaaManager.class) {
                if (singletonInstance == null) {
                    singletonInstance = new DebugAaaManager();
                }
            }
        }
        return singletonInstance;
    }



    @Override
    public void activate() {
//        maoNetworkCore.addPeerNeeds(new MaoPeerDemand("127.0.0.1", 6688));
        peerEventListener.startListener();
        networkDataDispatcher.registerReceiver(aaaDataReceiver, AAA_AUTH.getType(), 3);
        maoNetworkCore.addListener(peerEventListener);
        iAmReady();
    }

    @Override
    public void deactivate() {
        maoNetworkCore.removeListener(peerEventListener);

        // FIXME: may throw java.lang.NullPointerException here
        networkDataDispatcher.unregisterReceiver(aaaDataReceiver, AAA_AUTH.getType(), 3);
        peerEventListener.stopListener();
        iAmDone();
    }

    private class AaaDataReceiver extends MaoAbstractDataReceiver {

        /**
         * AAA message format:
         * RemoteIP,RemotePort
         *
         * Examples:
         * 10.0.0.1,1080
         * 2001::1,1080
         */

        @Override
        protected void process(DeviceEvent event) {
//            int peerId = Integer.parseInt(event.getDeviceId().getDeviceIdStr().split(NODEID_SPLITER)[1]);
            MaoAaaData aaa = parseAaaData(event.getReceivedData());
            if (aaa.isValid()) {
                log.info("Device id {}, IP-{}, Port-{}, {}",
                        event.getDeviceId(), aaa.getIp(), aaa.getPort(), event.getReceivedData());
                maoNetworkCore.permitConnected(peerId);

                StringBuilder sb = new StringBuilder();
                sb.append(AAA_ACCEPT.getHeader())
                        .append(event.getDeviceId()).append(",")
                        .append(event.getTimestamp());
                maoNetworkCore.getPeer(event.getDeviceId().()).wri te(AAA_AUTH.getHeader() + sb.toString());
            } else {
                log.warn("Fail to AAA, peer id {}, IP-{}, Port-{}, {}",
                        peerId, aaa.getIp(), aaa.getPort(), event.getReceivedData());
            }
        }

        private MaoAaaData parseAaaData(String originData) {
            int spliterPos = originData.indexOf(",", DATA_SPLITER_INDEX);
            if (spliterPos != -1) {
                try {
                    InetAddress ip = InetAddress.getByName(originData.substring(DATA_SPLITER_INDEX + 1, spliterPos));
                    int port = Integer.parseInt(originData.substring(spliterPos + 1));
                    return new MaoAaaData(ip, port);
                } catch (UnknownHostException e) {
                    log.warn("Fail to parse ip {}", originData.substring(DATA_SPLITER_INDEX + 1, spliterPos));
                } catch (NumberFormatException e) {
                    log.warn("Fail to parse port {}", originData.substring(spliterPos + 1));
                }
            }
            return MaoAaaData.getInvalidInstance();
        }
    }

    private class DebugPeerEventListener extends MaoAbstractListener<PeerEvent> {
        @Override
        protected void process(PeerEvent event) {
            switch (event.getType()) {
                case PEER_NEW:
                    log.info("new peer {}, {} {} -> {} {}", event.getPeerId(),
                            event.getMyIp(), event.getMyPort(), event.getPeerIp(), event.getPeerPort());

                    StringBuilder sb = new StringBuilder();
                    sb.append(AAA_AUTH.getHeader())
                            .append(event.getMyIp()).append(",")
                            .append(event.getMyPort());
                    maoNetworkCore.getPeer(event.getPeerId()).write(AAA_AUTH.getHeader() + sb.toString());
                    break;
            }
        }

        @Override
        protected boolean isRelevant(PeerEvent event) {
            return event.getType() == PEER_NEW;
        }
    }
}






















