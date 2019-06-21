package com.mcglynn.rvo.util;

import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.io.IOException;
import java.io.OutputStream;

public class LoggerStream extends OutputStream {
    private final Logger logger;
    private final Level logLevel;
    private final OutputStream outputStream;

    public LoggerStream(Logger logger, Level logLevel, OutputStream outputStream)
    {
        super();

        this.logger = logger;
        this.logLevel = logLevel;
        this.outputStream = outputStream;
    }

    public void log(Level level, String msg) {
        switch (level) {
            case ERROR:
                logger.error(msg);
                return;
            case WARN:
                logger.warn(msg);
                return;
            case INFO:
                logger.info(msg);
                return;
            case DEBUG:
                logger.debug(msg);
                return;
            case TRACE:
                logger.trace(msg);
                return;
            default:
                log(Level.ERROR, "Unexpected log level");
        }
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        outputStream.write(b);
        String string = new String(b);
        if (!string.trim().isEmpty())
            log(logLevel, string);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        outputStream.write(b, off, len);
        String string = new String(b, off, len);
        if (!string.trim().isEmpty())
            log(logLevel, string);
    }

    @Override
    public void write(int b) throws IOException
    {
        outputStream.write(b);
        String string = String.valueOf((char) b);
        if (!string.trim().isEmpty())
            log(logLevel, string);
    }
}
