package com.khartec.waltz.common;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.IOException;

public class LoggingUtilities {

    private static final String LOG_CONFIG_FILE_NAME = "waltz-logback.xml";


    public static void configureLogging() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            context.reset();
            configurator.setContext(context);
            System.out.println("Attempting to load logback configuration file: " + LOG_CONFIG_FILE_NAME
                                + " from classpath or " + System.getProperty("user.home") + "/.waltz/");

            Resource logbackConfigFile = IOUtilities.getFileResource(LOG_CONFIG_FILE_NAME);
            if (logbackConfigFile.exists()) {
                System.out.println("Found logback configuration file at: " + logbackConfigFile.getFile().getAbsolutePath());
                configurator.doConfigure(logbackConfigFile.getFile());
            } else {
                System.out.println("Logback configuration file not found..");
            }
        } catch (IOException | JoranException e) {
            // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }
}
