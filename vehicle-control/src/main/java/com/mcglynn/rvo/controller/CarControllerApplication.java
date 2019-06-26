package com.mcglynn.rvo.controller;

import com.mcglynn.rvo.controller.ui.UIDebugCarController;
import com.mcglynn.rvo.controller.video.JpegVideoInboundHandler;
import com.mcglynn.rvo.data.CarControlProtos;
import com.mcglynn.rvo.util.Constants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class CarControllerApplication extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(CarControllerApplication.class);
    private static EventLoopGroup carConnectionGroup;
    private static EventLoopGroup workerGroup;
    private static EventLoopGroup videoReceiveGroup;
    private static boolean isRunning = true;

    private String carHost;
    private int carPort;
    private int videoReceivePort;
    private Bootstrap bootstrap;
    private UIDebugCarController carControllerUi;

    public static void main(String[] args) {
        launch(args);
    }

    private static void shutDown() {
        LOGGER.warn("Shutdown!");
        isRunning = false;
        videoReceiveGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        carConnectionGroup.shutdownGracefully();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        LOGGER.warn("Starting!");
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());

        URL uiViewResource = CarControllerApplication.class.getClassLoader().getResource("com/mcglynn/rvo/controller/ui/UIDebugCarController.fxml");
        if (uiViewResource == null) {
            throw new RuntimeException("Failed to load UI view resource.");
        }
        FXMLLoader loader = new FXMLLoader(uiViewResource);
        Parent root = loader.load();
        carControllerUi = loader.getController();
        primaryStage.setTitle("Car Controller");
        primaryStage.setScene(new Scene(root, 1400, 800));
        primaryStage.show();

        carHost = System.getProperty("car.host", "localhost");
        carPort = Integer.parseInt(System.getProperty("car.port", "8080"));
        String videoReceiveHost = System.getProperty("video.receive.host", InetAddress.getLocalHost().getHostAddress());
        videoReceivePort = Integer.parseInt(System.getProperty("video.receive.port", "8090"));
        int commandDelayMin = Integer.parseInt(System.getProperty("controller.command.delay.min", "30"));
        int commandDelayMax = Integer.parseInt(System.getProperty("controller.command.delay.max", "300"));
        LOGGER.info("car.host: {}", carHost);
        LOGGER.info("car.port: {}", carPort);
        LOGGER.info("video.receive.host: {}", videoReceiveHost);
        LOGGER.info("video.receive.port: {}", videoReceivePort);
        LOGGER.info("controller.command.delay.min: {}", commandDelayMin);
        LOGGER.info("controller.command.delay.max: {}", commandDelayMax);
        CarClientConfig controllerConfig = CarClientConfigBuilder.aCarClientConfig()
                .withCommandDelayMin(commandDelayMin)
                .withCommandDelayMax(commandDelayMax)
                .withVideoReceiveHost(videoReceiveHost)
                .withVideoReceivePort(videoReceivePort)
                .build();
        carConnectionGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap(); // (1)
        bootstrap.group(workerGroup); // (2)
        bootstrap.channel(NioSocketChannel.class); // (3)
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true); // (4)
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                ch.pipeline().addLast(new ProtobufDecoder(CarControlProtos.CarData.getDefaultInstance()));
                ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                ch.pipeline().addLast(new ProtobufEncoder());
                ch.pipeline().addLast(new CarControllerClientHandler(carControllerUi, controllerConfig));
            }
        });

        startAcceptingVideoData();
        connectToCar(0);
    }

    private void startAcceptingVideoData() {
        videoReceiveGroup = new NioEventLoopGroup();
        final Bootstrap b = new Bootstrap();
        b.group(videoReceiveGroup).channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    public void initChannel(final NioDatagramChannel ch) throws Exception {
                        ch.pipeline().addLast(new JpegVideoInboundHandler((image) -> Platform.runLater(() -> carControllerUi.setImage(image)), Constants.DEFAULT_IMAGE_STREAM_MARKER));
                    }
                });

        // Bind and start to accept incoming connections.
        videoReceiveGroup.schedule(() -> {
            try {
                b.bind(videoReceivePort).sync().channel().closeFuture().await();
            }
            catch (InterruptedException e) {
                LOGGER.error("Video receive interrupted", e);
            }
        }, 0, TimeUnit.MILLISECONDS);
    }

    private synchronized void connectToCar(int delayMillis) {
        if (!isRunning) return;

        carConnectionGroup.schedule(() -> {
            try {
                ChannelFuture f = bootstrap.connect(carHost, carPort).sync();
                // Wait until the connection is closed.
                f.channel().closeFuture().sync();
                LOGGER.info("Channel closed");
                connectToCar(1000);
            }
            catch (Exception e) {
                LOGGER.error("Failure with car connection", e);
                connectToCar(1000);
            }
        }, delayMillis, TimeUnit.MILLISECONDS);
    }

    public static class ShutdownThread extends Thread {
        @Override
        public void run() {
            shutDown();
        }
    }
}
