package com.mcglynn.rvo.controller;

import com.mcglynn.rvo.data.CarControlProtos;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class CarControllerClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CarControllerClientHandler.class);

    private CarController carController;
    private CarControllerConfig config;
    private CarControlProtos.CarControllerCommand lastCommand;

    public CarControllerClientHandler(CarController carController, CarControllerConfig config) {
        LOGGER.info("Initializing: {}", config);
        this.carController = carController;
        this.config = config;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        LOGGER.info("Channel active");
        controlLoop(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        CarControlProtos.CarData m = (CarControlProtos.CarData) msg;
        try {
            carController.handleCarData(m);
        }
        catch (Exception e) {
            LOGGER.error("Failed to handle car data", e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("Failure", cause);
        ctx.close();
    }

    private void controlLoop(ChannelHandlerContext ctx) {
        ctx.channel().eventLoop().schedule(() -> {
            CarControlProtos.CarControllerCommand nextCommand = carController.getCurrentCommand();
            if (lastCommand == null || !lastCommand.equals(nextCommand)) {
                lastCommand = nextCommand;
                ctx.writeAndFlush(nextCommand);
            }
            controlLoop(ctx);
        }, config.getCommandDelay(), TimeUnit.MILLISECONDS);
    }
}
