package com.mcglynn.rvo.controller.image;

import com.mcglynn.rvo.stream.BufferReceiver;
import com.mcglynn.rvo.stream.StreamMarker;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class ImageBufferReceiver extends BufferReceiver<Image> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageBufferReceiver.class);
    private static final int DEFAULT_BUFFER_SIZE = 500000;


    public ImageBufferReceiver(Consumer<Image> imageHandler, StreamMarker streamMarker) {
        super(Image::new, imageHandler, streamMarker, DEFAULT_BUFFER_SIZE);
    }

}
