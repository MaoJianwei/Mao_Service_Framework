package com.maojianwei.service.framework.incubator.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by mao on 2016/7/1.
 */
public class MaoProtocolDecoder extends MessageToMessageDecoder<ByteBuf> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final byte [] PROTOCOL_PREFIX = "MAOCLOUD".getBytes();
    private static final int PROTOCOL_VERSION = 1;
    private static final int CHECKSUM_LENGTH = 32;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws UnsupportedEncodingException {

        if(msg.readableBytes() < PROTOCOL_PREFIX.length + 1 + 2) {
            //FIXME - USING LengthFieldBasedFrameDecoder, SHOULD NOT COME HERE !
            log.error(("readableBytes is not enough! Coding Attention!"));
            return;
        }

        if(!checkProtocolValid(msg)){
            log.error(("Protocol prefix is invalid!"));
            return;
        }
        log.info("Protocol prefix check OK!");

        short dataLen = msg.readShort();
        byte[] str = new byte[dataLen + 1];
        msg.readBytes(str);
        str[str.length-1] = 0;
        out.add(new String(str));
    }

    /**
     * Check Protocol Prefix: MAOCLOUD
     *
     * @param msg
     * @return
     */
    private boolean checkProtocolValid(ByteBuf msg){
        byte [] prefix = new byte[8];
        msg.readBytes(prefix);
        for (int i = 0; i < PROTOCOL_PREFIX.length; i++) {
            if (PROTOCOL_PREFIX[i] != prefix[i]) {
                return false;
            }
        }

        if (msg.readByte() != PROTOCOL_VERSION) {
            return false;
        }

        return true;
    }
}
