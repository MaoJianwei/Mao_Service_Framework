package com.maojianwei.service.framework.incubator.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by mao on 2016/7/1.
 */
public class MaoProtocolEncoder extends MessageToByteEncoder<String> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final byte [] PROTOCOL_PREFIX = "MAOCLOUD".getBytes();
    private static final int PROTOCOL_VERSION = 1;
    private static final int CHECKSUM_LENGTH = 32;

    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws UnsupportedEncodingException {

        MessageDigest digestGen = null;
        short dataLen = 0;
        try {
            digestGen = MessageDigest.getInstance("SHA-256");
            dataLen = CHECKSUM_LENGTH;
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 is not supported!");
        }


        byte [] msgBytes = msg.getBytes();
        dataLen += PROTOCOL_PREFIX.length + 1 + 2 + msgBytes.length;

        ByteBuf tmp = PooledByteBufAllocator.DEFAULT.heapBuffer();
        tmp.writeBytes(PROTOCOL_PREFIX);
        tmp.writeByte(PROTOCOL_VERSION);
        tmp.writeShort(dataLen);
        tmp.writeBytes(msgBytes);

        byte [] packet = new byte [tmp.readableBytes()];
        tmp.readBytes(packet);

        if (digestGen != null){
            try {
                log.info("will calculate SHA-256...");
                byte[] sha256 = digestGen.digest(packet);
                log.info("SHA-256 checksum is {}", String.format("%064x", new BigInteger(1, sha256)));

                out.writeBytes(packet);
                out.writeBytes(sha256);
            } catch(Exception e){
                log.error("SHA-256 is not supported!");
                //msg will not be sent.
                out.writeBytes(packet);
            }
        } else {
            out.writeBytes(packet);
        }

        log.info("release tmp Bytebuf...");
        tmp.release();
        log.info("release tmp over.");
    }
}
