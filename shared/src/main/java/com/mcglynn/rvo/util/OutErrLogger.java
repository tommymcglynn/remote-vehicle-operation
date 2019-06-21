package com.mcglynn.rvo.util;

import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.PrintStream;

public class OutErrLogger {

    public static void setOutAndErrToLog()
    {
        setOutToLog();
        setErrToLog();
    }

    public static void setOutToLog()
    {
        System.setOut(new PrintStream(new LoggerStream(LoggerFactory.getLogger("out"), Level.INFO, System.out)));
    }

    public static void setErrToLog()
    {
        System.setErr(new PrintStream(new LoggerStream(LoggerFactory.getLogger("err"), Level.ERROR, System.err)));
    }

}
