package com.mcglynn.rvo.controller.video;

import com.mcglynn.rvo.controller.image.ImageBufferReceiver;
import com.mcglynn.rvo.stream.StreamMarker;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import javafx.scene.image.Image;

import java.util.function.Consumer;

public class JpegVideoInboundHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    ImageBufferReceiver imageBufferReceiver;

    public JpegVideoInboundHandler(Consumer<Image> imageHandler, StreamMarker streamMarker) {
        imageBufferReceiver = new ImageBufferReceiver(imageHandler, streamMarker);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        ByteBuf content = packet.content();
        imageBufferReceiver.put(content);
    }
}
