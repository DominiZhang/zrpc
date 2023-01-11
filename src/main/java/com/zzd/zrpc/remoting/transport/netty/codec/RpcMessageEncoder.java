package com.zzd.zrpc.remoting.transport.netty.codec;

import com.zzd.zrpc.compress.Compress;
import com.zzd.zrpc.enums.CompressTypeEnum;
import com.zzd.zrpc.enums.SerializationTypeEnum;
import com.zzd.zrpc.extension.ExtensionLoader;
import com.zzd.zrpc.remoting.constants.RpcConstants;
import com.zzd.zrpc.remoting.dto.RpcMessage;
import com.zzd.zrpc.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zzd
 * @date 2023/1/11
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(1);

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage rpcMessage, ByteBuf out) {
        try {
            // add header info
            out.writeBytes(RpcConstants.MAGIC_NUMBER);
            out.writeByte(RpcConstants.VERSION);
            // leave a place to write the value of full length
            out.writerIndex(out.writerIndex() + 4);
            byte messageType = rpcMessage.getMessageType();
            out.writeByte(messageType);
            out.writeByte(rpcMessage.getCodec());
            out.writeByte(rpcMessage.getCompress());
            out.writeInt(ATOMIC_INTEGER.getAndIncrement());
            // build full length
            byte[] bodyBytes = null;
            int fullLength = RpcConstants.HEAD_LENGTH;
            // if messageType is not heartbeat message, fullLength = head length + body length
            if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE
                    && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                // serialize the object
                String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
                log.info("codec name: [{}]", codecName);
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                        .getExtension(codecName);
                bodyBytes = serializer.serialize(rpcMessage.getData());
                // compress the bytes
                String compressName = CompressTypeEnum.getName(rpcMessage.getCompress());
                Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                        .getExtension(compressName);
                bodyBytes = compress.compress(bodyBytes);
                fullLength += bodyBytes.length;
            }

            if (bodyBytes != null) {
                out.writeBytes(bodyBytes);
            }
            int writerIndex = out.writerIndex();
            // find position of full length
            out.writerIndex(writerIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
            out.writeInt(fullLength);
            // reset to the last position of body
            out.writerIndex(writerIndex);
        } catch (Exception e) {
            log.error("Encode request error: ", e);
        }
    }
}
