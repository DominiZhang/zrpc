package com.zzd.zrpc.remoting.transport.netty.client;

import com.zzd.zrpc.enums.CompressTypeEnum;
import com.zzd.zrpc.enums.SerializationTypeEnum;
import com.zzd.zrpc.factory.SingletonFactory;
import com.zzd.zrpc.remoting.constants.RpcConstants;
import com.zzd.zrpc.remoting.dto.RpcMessage;
import com.zzd.zrpc.remoting.dto.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，{@link io.netty.channel.SimpleChannelInboundHandler} 内部的
 * channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。详见《Netty进阶之路 跟着案例学 Netty》
 *
 * @author zzd
 * @date 2023/1/9
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {
    private final UnprocessedRequests unprocessedRequests;
    private final NettyRpcClient nettyRpcClient;

    public NettyRpcClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            log.info("client receive msg: [{}]", msg);
            if (msg instanceof RpcMessage) {
                RpcMessage message = (RpcMessage) msg;
                byte messageType = message.getMessageType();
                if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                    log.info("heart [{}]", message.getData());
                } else if (messageType == RpcConstants.RESPONSE_TYPE) {
                    RpcResponse<Object> rpcResponse = (RpcResponse<Object>) message.getData();
                    unprocessedRequests.complete(rpcResponse);
                }
            }
        } finally {
            // ByteBuf 实现了 ReferenceCounted 接口，可以被release
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     *  netty 心跳机制相关. 保证客户端和服务端的连接不被断掉, 避免重连
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMessage = RpcMessage.builder()
                        .codec(SerializationTypeEnum.PROTOSTUFF.getCode())
                        .compress(CompressTypeEnum.GZIP.getCode())
                        .messageType(RpcConstants.HEARTBEAT_REQUEST_TYPE)
                        .data(RpcConstants.PING)
                        .build();
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }
    }

    /**
     * called when an exception occurs in processing a client message
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("client caught exception:", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
