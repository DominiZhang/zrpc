package com.zzd.zrpc.remoting.transport.netty.client;

import com.zzd.zrpc.remoting.dto.RpcRequest;
import com.zzd.zrpc.remoting.dto.RpcResponse;
import com.zzd.zrpc.remoting.transport.netty.coder.NettyKryoDecoder;
import com.zzd.zrpc.remoting.transport.netty.coder.NettyKryoEncoder;
import com.zzd.zrpc.serialize.kryo.KryoSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zzd
 * @date 2023/1/9
 */
@Slf4j
public class NettyClient {
    private final String host;
    private final int port;
    private static final Bootstrap boot;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // 静态初始化客户端相关资源，如 EventLoopGroup，BootStrap
    static {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        boot = new Bootstrap();
        KryoSerializer serializer = new KryoSerializer();
        boot.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                // 链接超时时间的option，超过这个时间还是无法建立连接，则连接失败
                // 如果 15 秒之内没有发送数据给服务端的话，就发送一次心跳请求
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) throws Exception {
                        // 自定义序列化解码器
                        // RpcResponse -> ByteBuf
                        sc.pipeline().addLast(new NettyKryoDecoder(serializer, RpcResponse.class));
                        // ByteBuf -> RpcRequest
                        sc.pipeline().addLast(new NettyKryoEncoder(serializer, RpcRequest.class));
                        sc.pipeline().addLast(new NettyClientHandler());
                    }
                });
    }

    /**
     * 发送消息到服务端
     * @param req 消息体
     * @return 服务端返回的数据
     */
    public RpcResponse sendMessage(RpcRequest req) {
        try {
            ChannelFuture channelFuture = boot.connect(host, port).sync();
            log.info("client connect {}", host + ":" + port);
            Channel channel = channelFuture.channel();
            log.info("send message");
            if (null != channel) {
                channel.writeAndFlush(req).addListener(future -> {
                    if (future.isSuccess()) {
                        log.info("client send message: [{}]", req.toString());
                    } else {
                        log.error("send failed: ", future.cause());
                    }
                });
                // 阻塞等待, 直到Channel关闭
                channel.closeFuture().sync();
                // 将服务端返回的数据(RpcResponse)对象取出
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
                return channel.attr(key).get();
            }
        } catch (InterruptedException e) {
            log.error("occur exception when connect server:", e);
        }
        return null;
    }
}
