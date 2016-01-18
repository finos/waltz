/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.data.access_log;

import com.khartec.waltz.model.accesslog.AccessLog;
import com.khartec.waltz.model.accesslog.ImmutableAccessLog;
import com.khartec.waltz.schema.tables.records.AccessLogRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

import static com.khartec.waltz.schema.tables.AccessLog.ACCESS_LOG;


@Repository
public class AccessLogDao {

    private static final Logger LOG = LoggerFactory.getLogger(AccessLogDao.class);

    private final DSLContext dsl;

    private final RecordMapper<Record, AccessLog> mapper = r -> {
        AccessLogRecord record = r.into(ACCESS_LOG);
        return ImmutableAccessLog.builder()
                .userId(record.getUserId())
                .params(record.getParams())
                .state(record.getState())
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

    public List<AccessLog> findForUserId(String userId) {
        return dsl.select(ACCESS_LOG.fields())
                .from(ACCESS_LOG)
                .where(ACCESS_LOG.USER_ID.equalIgnoreCase(userId))
                .orderBy(ACCESS_LOG.CREATED_AT.desc())
                .fetch(mapper);
    }
}
