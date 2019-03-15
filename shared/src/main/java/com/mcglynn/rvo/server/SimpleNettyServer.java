package com.mcglynn.rvo.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class SimpleNettyServer {
    private int port;
    private ChannelInboundHandlerAdapter handlerAdapter;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public SimpleNettyServer(int port, ChannelInboundHandlerAdapter handlerAdapter) {
        this.port = port;
        this.handlerAdapter = handlerAdapter;
    }

    public void run() throws Exception {
        if (bossGroup != null && !bossGroup.isShutdown()) {
            throw new IllegalStateException("Server already running");
        }
        bossGroup = new NioEventLoopGroup(); // (1)
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap(); // (2)
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class) // (3)
                .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(handlerAdapter);
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

        // Bind and start to accept incoming connections.
        ChannelFuture f = b.bind(port).sync(); // (7)
    }

    public void shutdown() {
        if (bossGroup == null || bossGroup.isShutdown()) {
            throw new IllegalStateException("Server not running");
        }
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }
}
