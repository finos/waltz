package com.khartec.waltz.service;


import org.jooq.DSLContext;
import org.jooq.ExecuteContext;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultExecuteListener;
import org.jooq.tools.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


public class SlowQueryListener extends DefaultExecuteListener {

    private static final String PERFORMANCE_APPENDER = "WALTZ.PERFORMANCE";
    private static final Logger LOG = LoggerFactory.getLogger(PERFORMANCE_APPENDER);

    private final int slowQueryThresholdSeconds;
    private StopWatch stopWatch;
    private long slowQueryThresholdInNanos;

    public class SQLPerformanceWarning
            extends Exception {

        public SQLPerformanceWarning(String message) {
            super(message);
        }
    }


    public SlowQueryListener(int slowQueryThresholdSeconds) {
        LOG.info(String.format("Initialising with %s second threshold", slowQueryThresholdSeconds));
        this.slowQueryThresholdSeconds = slowQueryThresholdSeconds;
        this.slowQueryThresholdInNanos = TimeUnit.SECONDS.toNanos(slowQueryThresholdSeconds);
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

            LOG.warn(String.format("Slow SQL executed in %d seconds", TimeUnit.NANOSECONDS.toSeconds(split)), new SQLPerformanceWarning(context.renderInlined(ctx.query())));
        }
    }
}
