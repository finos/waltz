/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.common;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

public class LoggingUtilities {

    private static final String LOG_CONFIG_FILE_NAME = "waltz-logback.xml";


    /**
     * Initialises logging for Waltz.  This will attempt to locate a
     * file called <code>waltz-logback.xml</code> from either:
     * <ul>
     *     <li>
     *         root of classpath
     *     </li>
     *     <li>
     *         directory: <code>${user.home}/.waltz</code>
     *     </li>
     * </ul>
     *
     * Note: this file is allowed to use System.out to communicate
     * failures to the operator.
     */
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
                System.out.println("Found logback configuration file: " + logbackConfigFile);
                try (InputStream configInputStream = logbackConfigFile.getInputStream()) {
                    configurator.doConfigure(configInputStream);
                }
            } else {
                System.out.println("Logback configuration file not found..");
            }
        } catch (IOException | JoranException e) {
            // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }
}
