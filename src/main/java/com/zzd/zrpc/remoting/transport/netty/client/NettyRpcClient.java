package com.zzd.zrpc.remoting.transport.netty.client;

import com.zzd.zrpc.enums.CompressTypeEnum;
import com.zzd.zrpc.enums.SerializationTypeEnum;
import com.zzd.zrpc.extension.ExtensionLoader;
import com.zzd.zrpc.factory.SingletonFactory;
import com.zzd.zrpc.registry.ServiceDiscovery;
import com.zzd.zrpc.remoting.constants.RpcConstants;
import com.zzd.zrpc.remoting.dto.RpcMessage;
import com.zzd.zrpc.remoting.dto.RpcRequest;
import com.zzd.zrpc.remoting.dto.RpcResponse;
import com.zzd.zrpc.remoting.transport.RpcRequestTransport;
import com.zzd.zrpc.remoting.transport.netty.codec.RpcMessageDecoder;
import com.zzd.zrpc.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * initialize and close BootStrap object
 * @author zzd
 * @date 2023/1/11
 */
@Slf4j
public class NettyRpcClient implements RpcRequestTransport {
    private final ServiceDiscovery serviceDiscovery;
    private final UnprocessedRequests unprocessedRequests;
    private final ChannelProvider channelProvider;
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    public NettyRpcClient() {
        // initialize resources such as EventGroupLoopGroup and BootStrap
        this.eventLoopGroup = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                // the timeout period of the connection
                // if this time is exceeded or the connection cannot be established, the connection fails.
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) {
                        ChannelPipeline pipeline = sc.pipeline();
                        // if no data is sent to the server within 15 seconds, a heartbeat request is sent
                        pipeline.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        pipeline.addLast(new RpcMessageEncoder());
                        pipeline.addLast(new RpcMessageDecoder());
                        pipeline.addLast(new NettyRpcClientHandler());
                    }
                });
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);

    }

    // throws checked exception instead of coding
    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) listener -> {
            if (listener.isSuccess()) {
                log.info("The client has connected [{}] successfully", inetSocketAddress.toString());
                completableFuture.complete(listener.channel());
            } else {
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        // build return value
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        // build rpc service name by rpcRequest
        String rpcServiceName = rpcRequest.getRpcServiceName();
        // get server address
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        // get server address related channel
        Channel channel = getChannel(inetSocketAddress);
        if (channel.isActive()) {
            // put unprocessed request
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            RpcMessage message = RpcMessage.builder()
                    .data(rpcRequest)
                    .codec(SerializationTypeEnum.PROTOSTUFF.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .messageType(RpcConstants.REQUEST_TYPE).build();
            channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) listener -> {
                if (listener.isSuccess()) {
                    log.info("client send message: [{}]", message);
                } else {
                    listener.channel().close();
                    resultFuture.completeExceptionally(listener.cause());
                    log.error("send failed:", listener.cause());
                }
            });
        } else {
            throw new IllegalStateException();
        }

        return resultFuture;
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = channelProvider.get(inetSocketAddress);
        if (null == channel) {
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }

    public void close() {
        eventLoopGroup.shutdownGracefully();
    }
}
