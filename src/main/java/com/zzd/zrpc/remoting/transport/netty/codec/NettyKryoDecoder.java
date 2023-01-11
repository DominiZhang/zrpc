package com.zzd.zrpc.remoting.transport.netty.codec;

import com.zzd.zrpc.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 自定义解码器
 * 会从 ByteBuf 中读取到业务对象对应的字节序列，然后再将字节序列转换为对应的业务对象
 * @author zzd
 * @date 2023/1/9
 */
@Slf4j
@AllArgsConstructor
public class NettyKryoDecoder extends ByteToMessageDecoder {
    private final Serializer serializer;
    private final Class<?> genericClass;

    /** Netty传输的消息长度也就是对象序列化后对应的字节数组的大小，存储在 ByteBuf 头部 */
    private static final int BODY_LENGTH = 4;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        // 1.byteBuf 中写入的消息长度所占的字节数已经是4了，所以 byteBuf 的可读字节必须大于 4;
        if (buf.readableBytes() >= BODY_LENGTH) {
            // 2.标记当前readIndex的位置，以便后面重置readIndex的时候使用
            buf.markReaderIndex();
            // 3.读取消息的长度
            // 注意: 消息的长度是 NettyKryoEncode 的 encode() 方法写入的
            int dataLength = buf.readInt();
            // 4.遇到不合理的情况直接return
            if (dataLength < 0 || buf.readableBytes() < 0) {
                log.error("data length or byteBuf readableBytes is not valid");
                return;
            }
            // 5.如果可读字节数小于消息长度的话，说明是不完整的消息，重置readIndex
            if (buf.readableBytes() < dataLength) {
                buf.resetReaderIndex();
                return;
            }
            // 6.执行到此，则没什么问题，可以执行反序列化
            byte[] bytes = new byte[dataLength];
            buf.readBytes(bytes);
            // 将bytes数组转换为我们需要的对象
            Object obj = serializer.deserialize(bytes, genericClass);
            out.add(obj);
            log.info("successfully decode ByteBuf to Object");
        }
    }
}
