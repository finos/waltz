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
