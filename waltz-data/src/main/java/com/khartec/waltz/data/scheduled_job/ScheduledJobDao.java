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

package com.khartec.waltz.data.scheduled_job;


import com.khartec.waltz.model.scheduled_job.JobKey;
import com.khartec.waltz.model.scheduled_job.JobLifecycleStatus;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.Settings.SETTINGS;

@Repository
public class ScheduledJobDao {

    private final DSLContext dsl;


    @Autowired
    public ScheduledJobDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public boolean isJobRunnable(JobKey jobKey) {
        return dsl.fetchExists(DSL.select(SETTINGS.NAME)
                .from(SETTINGS)
                .where(SETTINGS.NAME.eq(jobKey.name()))
                .and(SETTINGS.VALUE.eq(JobLifecycleStatus.RUNNABLE.name())));
    }


    public boolean markJobAsRunning(JobKey jobKey) {
        return dsl.update(SETTINGS)
                .set(SETTINGS.VALUE, JobLifecycleStatus.RUNNING.name())
                .where(SETTINGS.NAME.eq(jobKey.name()))
                .and(SETTINGS.VALUE.eq(JobLifecycleStatus.RUNNABLE.name()))
                .execute()
                ==
                1;
    }


    public void updateJobStatus(JobKey jobKey, JobLifecycleStatus newStatus) {
        dsl.update(SETTINGS)
                .set(SETTINGS.VALUE, newStatus.name())
                .where(SETTINGS.NAME.eq(jobKey.name()))
                .execute();
    }
}
