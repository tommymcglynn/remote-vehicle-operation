package com.mcglynn.rvo.controller;

import com.mcglynn.rvo.controller.ui.UIDebugCarController;
import com.mcglynn.rvo.data.CarControlProtos;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.concurrent.TimeUnit;

public class CarControllerApplication extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(CarControllerApplication.class);
    private static EventLoopGroup carConnectionGroup;
    private static EventLoopGroup workerGroup;
    private static boolean isRunning = true;

    private String carHost;
    private int carPort;
    private Bootstrap bootstrap;

    public static void main(String[] args) {
        launch(args);
    }

    private static void shutDown() {
        LOGGER.warn("Shutdown!");
        isRunning = false;
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
        UIDebugCarController carController = loader.getController();
        primaryStage.setTitle("Debug Car Controller");
        primaryStage.setScene(new Scene(root, 1024, 768));
        primaryStage.show();

        carHost = System.getProperty("car.host", "localhost");
        carPort = Integer.parseInt(System.getProperty("car.port", "8080"));
        int commandDelay = Integer.parseInt(System.getProperty("controller.command.delay", "30"));
        CarControllerConfig controllerConfig = CarControllerConfigBuilder.aCarControllerConfig()
                .withCommandDelay(commandDelay)
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
                ch.pipeline().addLast(new CarControllerClientHandler(carController, controllerConfig));
            }
        });

        connectToCar(0);
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
