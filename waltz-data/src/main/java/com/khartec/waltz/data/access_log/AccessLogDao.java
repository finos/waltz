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

package com.khartec.waltz.data.access_log;

import com.khartec.waltz.model.accesslog.AccessLog;
import com.khartec.waltz.model.accesslog.AccessTime;
import com.khartec.waltz.model.accesslog.ImmutableAccessLog;
import com.khartec.waltz.model.accesslog.ImmutableAccessTime;
import com.khartec.waltz.schema.tables.records.AccessLogRecord;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.schema.tables.AccessLog.ACCESS_LOG;


@Repository
public class AccessLogDao {

    private static final Logger LOG = LoggerFactory.getLogger(AccessLogDao.class);

    private final DSLContext dsl;

    private final static RecordMapper<Record, AccessLog> TO_ACCESS_LOG = r -> {
        AccessLogRecord record = r.into(ACCESS_LOG);
        return ImmutableAccessLog.builder()
                .userId(record.getUserId())
                .params(record.getParams())
                .state(record.getState())
                .createdAt(record.getCreatedAt().toLocalDateTime())
                .build();
    };


    private final static RecordMapper<Record, AccessTime> TO_ACCESS_TIME = r -> {
        AccessLogRecord record = r.into(ACCESS_LOG);
        return ImmutableAccessTime.builder()
                .userId(record.getUserId())
                .createdAt(record.getCreatedAt().toLocalDateTime())
                .build();
    };


    @Autowired
    public AccessLogDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public int write(AccessLog logEntry) {
        return dsl.insertInto(ACCESS_LOG)
                .set(ACCESS_LOG.PARAMS, logEntry.params())
                .set(ACCESS_LOG.STATE, logEntry.state())
                .set(ACCESS_LOG.USER_ID, logEntry.userId())
                .set(ACCESS_LOG.CREATED_AT, Timestamp.valueOf(logEntry.createdAt()))
                .execute();
    }


    public List<AccessLog> findForUserId(String userId,
                                         Optional<Integer> limit) {
        return dsl.select(ACCESS_LOG.fields())
                .from(ACCESS_LOG)
                .where(ACCESS_LOG.USER_ID.equalIgnoreCase(userId))
                .orderBy(ACCESS_LOG.CREATED_AT.desc())
                .limit(limit.orElse(Integer.MAX_VALUE))
                .fetch(TO_ACCESS_LOG);
    }


    public List<AccessTime> findActiveUsersSince(LocalDateTime dateTime) {
        Field maxCreatedAt = DSL.max(ACCESS_LOG.CREATED_AT).as(ACCESS_LOG.CREATED_AT);
        return dsl.select(ACCESS_LOG.USER_ID, maxCreatedAt)
                .from(ACCESS_LOG)
                .where(ACCESS_LOG.CREATED_AT.greaterOrEqual(Timestamp.valueOf(dateTime)))
                .groupBy(ACCESS_LOG.USER_ID)
                .orderBy(maxCreatedAt.desc())
                .fetch(TO_ACCESS_TIME);
    }

}
