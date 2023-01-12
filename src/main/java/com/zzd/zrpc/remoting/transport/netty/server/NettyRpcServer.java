package com.zzd.zrpc.remoting.transport.netty.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zzd.zrpc.config.CustomShutdownHook;
import com.zzd.zrpc.config.RpcServiceConfig;
import com.zzd.zrpc.factory.SingletonFactory;
import com.zzd.zrpc.provider.ServiceProvider;
import com.zzd.zrpc.provider.impl.ZkServiceProviderImpl;
import com.zzd.zrpc.remoting.transport.netty.codec.RpcMessageDecoder;
import com.zzd.zrpc.remoting.transport.netty.codec.RpcMessageEncoder;
import com.zzd.zrpc.utils.RuntimeUtil;
import com.zzd.zrpc.utils.ThreadPoolFactoryUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.data.Id;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Receive the client message, call the corresponding method according to client message,
 * and then return the result to the client.
 *
 * @author zzd
 * @date 2023/1/12
 */
@Slf4j
@Component
public class NettyRpcServer {
    public static final int PORT = 9898;

    private final ServiceProvider serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);

    public void registerService(RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig);
    }

    @SneakyThrows
    public void start() {
        CustomShutdownHook.getCustomShutdownHook().clearAll();
        String host = InetAddress.getLocalHost().getHostAddress();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
                RuntimeUtil.cpus() * 2,
                ThreadPoolFactoryUtil.createThreadFactory("service-handler-group", false)
        );
        try {
            ServerBootstrap server = new ServerBootstrap();
            server.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // nagle
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // heart beat
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // initialize when client first send request
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) {
                            ChannelPipeline p = sc.pipeline();
                            // close the connection if no client request is received in 30 seconds
                            p.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            p.addLast(new RpcMessageEncoder());
                            p.addLast(new RpcMessageDecoder());
                            p.addLast(serviceHandlerGroup, new NettyRpcServerHandler());
                        }
                    });

            // bind port sync
            ChannelFuture channelFuture = server.bind(host, PORT).sync();
            // wait server listen port close
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("occur exception when start server: {}", e.getMessage());
        } finally {
            log.error("shutdown bossGroup and workerGroup");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            serviceHandlerGroup.shutdownGracefully();
        }
    }
}
