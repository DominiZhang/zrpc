package com.zzd.zrpc.remoting.transport.netty.server;

import com.zzd.zrpc.remoting.dto.RpcRequest;
import com.zzd.zrpc.remoting.dto.RpcResponse;
import com.zzd.zrpc.remoting.transport.netty.coder.NettyKryoDecoder;
import com.zzd.zrpc.remoting.transport.netty.coder.NettyKryoEncoder;
import com.zzd.zrpc.serialize.kryo.KryoSerializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zzd
 * @date 2023/1/10
 */
@Slf4j
public class NettyServer {
    private final int port;

    private NettyServer(int port) {
        this.port = port;
    }

    private void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        KryoSerializer serializer = new KryoSerializer();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // TCP默认开启了 Nagle 算法, 该算法的作用是尽可能的发送大数据库, 减少网络IO
                    // 该选项是控制是否启用 Nagle 算法
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 是否开启 TCP 底层心跳检测
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // 系统用于临时存放已完成三次握手的请求队列的最大长度, 如果连接建立频繁, 服务器处理创建新连接较慢, 可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) {
                            sc.pipeline().addLast(new NettyKryoDecoder(serializer, RpcRequest.class));
                            sc.pipeline().addLast(new NettyKryoEncoder(serializer, RpcResponse.class));
                            sc.pipeline().addLast(new NettyServerHandler());
                        }
                    });

            // 绑定端口，同步等待绑定成功
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            // 等待服务端监听端口关闭
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("occur exception when start server: ", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
