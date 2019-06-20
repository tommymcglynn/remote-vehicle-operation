package com.mcglynn.rvo.controller;

import com.mcglynn.rvo.data.CarControlProtos;
import com.mcglynn.rvo.util.ThresholdAggregator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class CarControllerClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CarControllerClientHandler.class);

    private CarController carController;
    private CarClientConfig config;
    private CarControlProtos.CarControllerCommand lastCommand;
    private long lastCommandTime;
    private ThresholdAggregator<Long> latencyAggregator;

    public CarControllerClientHandler(CarController carController, CarClientConfig config) {
        LOGGER.info("Initializing: {}", config);
        this.carController = carController;
        this.config = config;
        lastCommandTime = 0L;
        latencyAggregator = new ThresholdAggregator<>(10, longs -> {
            double averageLatency = longs.stream().mapToDouble(Long::doubleValue).average().orElse(Double.NaN);
            LOGGER.info("Average ping latency: {}ms", averageLatency);
        });
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        LOGGER.info("Channel active");
        carController.setVideoReceive(config.getVideoReceiveHost(), config.getVideoReceivePort());
        controlLoop(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        CarControlProtos.CarData m = (CarControlProtos.CarData) msg;
        long latency = m.getTime() - lastCommandTime;
        try {
            carController.handleCarData(m);
        }
        catch (Exception e) {
            LOGGER.error("Failed to handle car data", e);
        }
        latencyAggregator.add(latency);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("Failure", cause);
        ctx.close();
    }

    private void controlLoop(ChannelHandlerContext ctx) {
        ctx.channel().eventLoop().schedule(() -> {
            CarControlProtos.CarControllerCommand nextCommand = carController.getCurrentCommand();
            long timeSinceLastCommand = System.currentTimeMillis() - lastCommandTime;
            if (lastCommand == null || !lastCommand.equals(nextCommand) || timeSinceLastCommand >= config.getCommandDelayMax()) {
                lastCommand = nextCommand;
                lastCommandTime = System.currentTimeMillis();
                ctx.writeAndFlush(nextCommand);
            }
            controlLoop(ctx);
        }, config.getCommandDelayMin(), TimeUnit.MILLISECONDS);
    }
}
