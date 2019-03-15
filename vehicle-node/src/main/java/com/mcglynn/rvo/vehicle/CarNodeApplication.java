package com.mcglynn.rvo.vehicle;

import com.mcglynn.rvo.data.CarControlProtos;
import com.mcglynn.rvo.vehicle.toy.FourWheelToyCarNode;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarNodeApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(CarNodeApplication.class);
    private static EventLoopGroup bossGroup;
    private static EventLoopGroup workerGroup;

    public static void main(String[] args) throws Exception {
        LOGGER.warn("Starting!");
        Runtime.getRuntime().addShutdownHook(new RunThread());

        int port = Integer.parseInt(System.getProperty("car.port", "8080"));
        bossGroup = new NioEventLoopGroup(); // (1)
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap(); // (2)
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class) // (3)
                .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                        ch.pipeline().addLast(new ProtobufDecoder(CarControlProtos.CarControllerCommand.getDefaultInstance()));
                        ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                        ch.pipeline().addLast(new ProtobufEncoder());
                        ch.pipeline().addLast(new CarNodeServerHandler(new FourWheelToyCarNode()));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

        // Bind and start to accept incoming connections.
        ChannelFuture f = b.bind(port).sync(); // (7)

        // Wait until the server socket is closed.
        // In this example, this does not happen, but you can do that to gracefully
        // shut down your server.
        f.channel().closeFuture().sync();
    }

    private static void shutDown() {
        LOGGER.warn("Shutdown!");
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    public static class RunThread extends Thread {
        @Override
        public void run() {
            shutDown();
        }
    }
}
