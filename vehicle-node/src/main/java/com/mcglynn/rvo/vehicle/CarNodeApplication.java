package com.mcglynn.rvo.vehicle;

import com.mcglynn.rvo.data.CarControlProtos;
import com.mcglynn.rvo.util.OutErrLogger;
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
import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

public class CarNodeApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(CarNodeApplication.class);
    private static EventLoopGroup bossGroup;
    private static EventLoopGroup workerGroup;

    public static void main(String[] args) throws Exception {
        LOGGER.warn("Starting!");
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
        OutErrLogger.setOutAndErrToLog();
        String opencvNativeLibrary = Core.NATIVE_LIBRARY_NAME;
        LOGGER.warn("opencvNativeLibrary: {}", opencvNativeLibrary);
        System.loadLibrary(opencvNativeLibrary);

        int port = Integer.parseInt(System.getProperty("car.port", "8080"));
        String carNodeClassName = System.getProperty("car.node.class", "com.mcglynn.rvo.vehicle.toy.FourWheelToyCarNode");
        LOGGER.info("carNodeClassName: {}", carNodeClassName);
        CarNode carNode = initCarNode(carNodeClassName);
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
                        ch.pipeline().addLast(new CarNodeServerHandler(carNode));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

        // Bind and start to accept incoming connections.
        LOGGER.info("Will start accepting connections: port({})", port);
        ChannelFuture f = b.bind(port).sync(); // (7)

        // Wait until the server socket is closed.
        // In this example, this does not happen, but you can do that to gracefully
        // shut down your server.
        f.channel().closeFuture().sync();
    }

    private static CarNode initCarNode(String carNodeClassName) {
        Class<?> carNodeClass;
        try {
            carNodeClass = CarNodeApplication.class.getClassLoader().loadClass(carNodeClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to find car node class", e);
        }
        if (!CarNode.class.isAssignableFrom(carNodeClass)) {
            throw new RuntimeException(String.format("Class is not a CarNode: %s", carNodeClassName));
        }
        Object instance;
        try {
            instance = carNodeClass.getConstructor().newInstance();
        } catch (InstantiationException|IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
            throw new RuntimeException("Failed to construct car node class", e);
        }
        return (CarNode) instance;
    }

    private static void shutDown() {
        LOGGER.warn("Shutdown!");
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    public static class ShutdownThread extends Thread {
        @Override
        public void run() {
            shutDown();
        }
    }
}
