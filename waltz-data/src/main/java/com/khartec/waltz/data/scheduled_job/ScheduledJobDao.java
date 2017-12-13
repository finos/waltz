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
