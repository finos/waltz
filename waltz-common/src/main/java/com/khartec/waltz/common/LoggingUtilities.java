/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
