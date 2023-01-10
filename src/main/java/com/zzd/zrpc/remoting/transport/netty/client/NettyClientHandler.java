package com.zzd.zrpc.remoting.transport.netty.client;

import com.zzd.zrpc.remoting.dto.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zzd
 * @date 2023/1/9
 */
@Slf4j
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            RpcResponse resp = (RpcResponse) msg;
            log.info("client receive msg: [{}]", resp.toString());
            // 声明 AttributeKey 对象
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
            // 将服务端的返回结果保存到 AttributeMap 中, AttributeMap 可以看作是一个Channel的共享数据源
            // AttributeMap 的key是AttributeKey, value是Attribute
            ctx.channel().attr(key).set(resp);
            ctx.channel().close();
        } finally {
            // ByteBuf 实现了 ReferenceCounted 接口，可以被release
            ReferenceCountUtil.release(msg);
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("client caught exception:" + cause.getMessage());
        ctx.close();
    }
}
