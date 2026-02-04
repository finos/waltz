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

package org.finos.waltz.service;


import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.ExecuteContext;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultExecuteListener;
import org.jooq.tools.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;


public class SlowQueryListener extends DefaultExecuteListener {

    private static final String PERFORMANCE_APPENDER = "WALTZ.PERFORMANCE";
    private static final Logger LOG = LoggerFactory.getLogger(PERFORMANCE_APPENDER);

    private StopWatch stopWatch;
    private long slowQueryThresholdInNanos;
    private DataSource dataSource;
    private boolean logConnectionPoolStatus;

    public class SQLPerformanceWarning
            extends Exception {

        public SQLPerformanceWarning(String message) {
            super(message);
        }
    }


    public SlowQueryListener(int slowQueryThresholdSeconds, DataSource dataSource, boolean logConnectionPoolStatus) {
        LOG.info("Initialising with {} second threshold", slowQueryThresholdSeconds);
        this.slowQueryThresholdInNanos = TimeUnit.SECONDS.toNanos(slowQueryThresholdSeconds);
        this.dataSource = dataSource;
        this.logConnectionPoolStatus = logConnectionPoolStatus;
    }


    @Override
    public void executeStart(ExecuteContext ctx) {
        super.executeStart(ctx);
        stopWatch = new StopWatch();
    }


    @Override
    public void executeEnd(ExecuteContext ctx) {
        super.executeEnd(ctx);
        long split = stopWatch.split();
        if (split > slowQueryThresholdInNanos) {
            DSLContext context = DSL.using(ctx.dialect(),
                    // ... and the flag for pretty-printing
                    new Settings().withRenderFormatted(true));

            LOG.info(String.format("Slow SQL executed in %d seconds", TimeUnit.NANOSECONDS.toSeconds(split)), new SQLPerformanceWarning(context.renderInlined(ctx.query())));
            LOG.info(dataSource.toString());

            logConnectionPoolStatus();
        }
    }

    private void logConnectionPoolStatus() {
        if (logConnectionPoolStatus) {
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDataSource = (HikariDataSource) dataSource;

                int activeConnections = hikariDataSource.getHikariPoolMXBean().getActiveConnections();
                int idleConnections = hikariDataSource.getHikariPoolMXBean().getIdleConnections();
                int totalConnections = hikariDataSource.getHikariPoolMXBean().getTotalConnections();
                int threadsAwaitingConnection = hikariDataSource.getHikariPoolMXBean().getThreadsAwaitingConnection();
                String poolName = hikariDataSource.getPoolName();

                LOG.info(
                        "Hikari Pool Status [{}]: Active=[{}], Idle=[{}], Total=[{}], Awaiting=[{}]",
                        poolName,
                        activeConnections,
                        idleConnections,
                        totalConnections,
                        threadsAwaitingConnection);
            } else if (dataSource != null) {
                LOG.info("DataSource is of type: {}", dataSource.getClass().getName());
            } else {
                LOG.warn("DataSource is null, cannot log pool status.");
            }
        }
    }
}
