package com.zzd.zrpc.remoting.transport.netty.server;

import com.zzd.zrpc.remoting.dto.RpcRequest;
import com.zzd.zrpc.remoting.dto.RpcResponse;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zzd
 * @date 2023/1/10
 */
@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private static final AtomicInteger ATOMIC = new AtomicInteger(1);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            RpcRequest rpcRequest = (RpcRequest) msg;
            log.info("Server receive msg: [{}], times: [{}]", rpcRequest, ATOMIC.getAndIncrement());
            RpcResponse<Object> messageFromServer = RpcResponse.builder().message("message from server").build();
            ChannelFuture channelFuture = ctx.writeAndFlush(messageFromServer);
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        } finally {
            ReferenceCountUtil.release(msg);
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Server caught exception: ", cause);
        ctx.close();
    }
}
