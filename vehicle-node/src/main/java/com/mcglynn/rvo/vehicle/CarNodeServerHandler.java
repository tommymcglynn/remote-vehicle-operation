package com.mcglynn.rvo.vehicle;

import com.mcglynn.rvo.data.CarControlProtos;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarNodeServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CarNodeServerHandler.class);

    private CarNode carNode;

    public CarNodeServerHandler(CarNode carNode) {
        this.carNode = carNode;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        CarControlProtos.CarControllerCommand m = (CarControlProtos.CarControllerCommand) msg;
        try {
            carNode.handleCommand(m);
        }
        catch (Exception e) {
            LOGGER.error("Failed to handle controller command", e);
        }
        ctx.writeAndFlush(carNode.getCurrentData());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
