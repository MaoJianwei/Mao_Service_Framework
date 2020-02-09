package com.maojianwei.service.framework.incubator.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by mao on 2016/7/1.
 */
public class MaoProtocolDecoder extends MessageToMessageDecoder<ByteBuf> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final byte[] PROTOCOL_PREFIX = "MAOCLOUD".getBytes();
    private static final int PROTOCOL_VERSION = 1;
    private static final int CHECKSUM_LENGTH = 32;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws UnsupportedEncodingException {

        // Using LengthFieldBasedFrameDecoder to make sure frame integrity

        byte[] packet = new byte[msg.readableBytes()];
        msg.readBytes(packet);

        if (!checkProtocolValid(packet)) {
            log.error(("Protocol prefix is invalid!"));
            return;
        }
        log.info("Protocol prefix check OK!");

//        short dataLen = (short) (msg.readShort() - PROTOCOL_PREFIX.length - 1 - 2 - CHECKSUM_LENGTH);
//        byte[] data = new byte[dataLen + 1];
//        msg.readBytes(data, 0, dataLen);
//        data[data.length - 1] = 0;
//

        try {
            MessageDigest digestGen = MessageDigest.getInstance("SHA-256");

            if (digestGen != null) {
                log.info("will calculate SHA-256...");
                digestGen.update(packet, 0, packet.length - CHECKSUM_LENGTH);
                byte[] sha256 = digestGen.digest();
                if (checkSha256(sha256, packet)){
                    out.add(new String(packet,
                            (PROTOCOL_PREFIX.length + 1 + 2),
                            (packet.length - PROTOCOL_PREFIX.length - 1 - 2 - CHECKSUM_LENGTH)));
                }
            } else {
                log.error("SHA-256 is not supported!");
            }
        } catch (Exception e) {
            log.error("SHA-256 is not supported!");
        }
    }

    /**
     * Check Protocol Prefix: MAOCLOUD
     *
     * @param packet
     * @return
     */
    private boolean checkProtocolValid(byte[] packet) {
        for (int i = 0; i < PROTOCOL_PREFIX.length; i++) {
            if (PROTOCOL_PREFIX[i] != packet[i]) {
                return false;
            }
        }

        if (packet[PROTOCOL_PREFIX.length] != PROTOCOL_VERSION) {
            return false;
        }

        return true;
    }

    private boolean checkSha256(byte[] sha256, byte[] packet) {
        if (sha256.length < CHECKSUM_LENGTH || packet.length < CHECKSUM_LENGTH)
            return false;

        for (int i = 0; i < CHECKSUM_LENGTH; i++) {
            if (sha256[i] != packet[packet.length - CHECKSUM_LENGTH + i]) {
                return false;
            }
        }
        return true;
    }
}
