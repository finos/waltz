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


import org.finos.waltz.common.RandomUtilities;
import org.finos.waltz.common.SetUtilities;
import org.jooq.ExecuteContext;
import org.jooq.impl.DefaultExecuteListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;


public class SlowDatabaseConnectionSimulator extends DefaultExecuteListener {

    private static final Logger LOG = LoggerFactory.getLogger(SlowDatabaseConnectionSimulator.class);

    private long maxDelayInMillis = 100;

    public SlowDatabaseConnectionSimulator(int maxDelayInMillis) {
        LOG.info("Initialising with {} milli threshold", maxDelayInMillis);
        this.maxDelayInMillis = maxDelayInMillis;
    }

    @Override
    public void fetchStart(ExecuteContext ctx) {
        try {
            if (shouldGoSlow(ctx)) {
                Thread.sleep(RandomUtilities.randomLongBetween(0, maxDelayInMillis));
            }
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Set<String> goSlowTables = SetUtilities.fromArray(
            "assessment_rating",
            "rating_scheme");


    private boolean shouldGoSlow(ExecuteContext ctx) {
        String sql = ctx.sql().toLowerCase();
        return goSlowTables
                .stream()
                .map(String::toLowerCase)
                .anyMatch(sql::contains);
    }


}
