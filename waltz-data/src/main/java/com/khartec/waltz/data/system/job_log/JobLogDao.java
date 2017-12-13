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

package com.khartec.waltz.data.system.job_log;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.system.job_log.ImmutableJobLog;
import com.khartec.waltz.model.system.job_log.JobLog;
import com.khartec.waltz.model.system.job_log.JobStatus;
import com.khartec.waltz.schema.tables.records.SystemJobLogRecord;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.schema.tables.SystemJobLog.SYSTEM_JOB_LOG;

/**
 * Created by dwatkins on 23/05/2016.
 */
@Repository
public class JobLogDao {

    private static final RecordMapper<? super Record, JobLog> TO_JOB_SUMMARY = r -> {

        SystemJobLogRecord record = r.into(SYSTEM_JOB_LOG);

        return ImmutableJobLog.builder()
                .name(record.getName())
                .status(JobStatus.SUCCESS)
                .start(record.getStart().toLocalDateTime())
                .entityKind(EntityKind.valueOf(record.getEntityKind()))
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public JobLogDao(DSLContext dsl) {
        Checks.checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<JobLog> findLatestSuccessful() {

        Field[] fields = new Field[] {
                SYSTEM_JOB_LOG.NAME,
                SYSTEM_JOB_LOG.ENTITY_KIND,
                DSL.max(SYSTEM_JOB_LOG.START).as(SYSTEM_JOB_LOG.START)
        };

        return dsl.select(fields)
                .from(SYSTEM_JOB_LOG)
                .where(SYSTEM_JOB_LOG.STATUS.eq(JobStatus.SUCCESS.name()))
                .groupBy(SYSTEM_JOB_LOG.NAME,
                        SYSTEM_JOB_LOG.ENTITY_KIND)
                .fetch(TO_JOB_SUMMARY);
    }

}
