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

package org.finos.waltz.data.access_log;

import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.model.accesslog.AccessLog;
import org.finos.waltz.model.accesslog.AccessLogSummary;
import org.finos.waltz.model.accesslog.AccessTime;
import org.finos.waltz.model.accesslog.ImmutableAccessLog;
import org.finos.waltz.model.accesslog.ImmutableAccessLogSummary;
import org.finos.waltz.model.accesslog.ImmutableAccessTime;
import org.finos.waltz.schema.tables.records.AccessLogRecord;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record3;
import org.jooq.RecordMapper;
import org.jooq.SelectSeekStep2;
import org.jooq.DSLContext;
import org.jooq.DatePart;

import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.finos.waltz.schema.tables.AccessLog.ACCESS_LOG;


@Repository
public class AccessLogDao {

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
        Field<Timestamp> maxCreatedAt = DSL.max(ACCESS_LOG.CREATED_AT).as(ACCESS_LOG.CREATED_AT);
        return dsl
                .select(ACCESS_LOG.USER_ID, maxCreatedAt)
                .from(ACCESS_LOG)
                .where(ACCESS_LOG.CREATED_AT.greaterOrEqual(Timestamp.valueOf(dateTime)))
                .groupBy(ACCESS_LOG.USER_ID)
                .orderBy(maxCreatedAt.desc())
                .fetch(TO_ACCESS_TIME);
    }

    public List<AccessLogSummary> findAccessCountsByPageSince(LocalDateTime dateTime) {
        return dsl
                .select(ACCESS_LOG.STATE, DSL.count().as("counts"))
                .from(ACCESS_LOG)
                .where(ACCESS_LOG.CREATED_AT.greaterOrEqual(Timestamp.valueOf(dateTime)))
                .groupBy(ACCESS_LOG.STATE)
                .fetch(r -> ImmutableAccessLogSummary
                        .builder()
                        .state(r.get(ACCESS_LOG.STATE))
                        .counts(r.get("counts", Long.class))
                        .build());
    }

    public List<AccessLogSummary> findWeeklyAccessLogSummary(LocalDateTime dateTime) {
        // Use MSSQL DATEPART for year and week extraction
        Field<Integer> yearCreated = DSL.extract(ACCESS_LOG.CREATED_AT, DatePart.YEAR);
        Field<Integer> weekCreated = DSL.extract(ACCESS_LOG.CREATED_AT, DatePart.WEEK);
        Field<Long> numberOfAccesses = DSL.count().cast(Long.class);

        SelectSeekStep2<Record3<Integer, Integer, Long>, Integer, Integer> qry = dsl
            .select(yearCreated.as("year_created"), weekCreated.as("week_created"), numberOfAccesses.as("num_accesses"))
            .from(ACCESS_LOG)
            .where(ACCESS_LOG.CREATED_AT.greaterOrEqual(Timestamp.valueOf(dateTime)))
            .groupBy(yearCreated,
                    weekCreated)
            .orderBy(yearCreated.asc(), weekCreated.asc());

        return qry
                .fetch(r -> ImmutableAccessLogSummary
                .builder()
                .year(r.get("year_created", Integer.class))
                .week(r.get("week_created", Integer.class))
                .counts(r.get("num_accesses", Long.class))
                .build());
    }

    public List<AccessLogSummary> findUniqueUsersSince(LocalDateTime dateTime) {

        return dsl
                .select(ACCESS_LOG.USER_ID, ACCESS_LOG.CREATED_AT)
                .from(ACCESS_LOG)
                .where(ACCESS_LOG.CREATED_AT.greaterOrEqual(Timestamp.valueOf(dateTime)))
                .fetch(r -> ImmutableAccessLogSummary
                        .builder()
                        .userId(r.get(ACCESS_LOG.USER_ID))
                        .createdAt(r.get(ACCESS_LOG.CREATED_AT).toLocalDateTime())
                        .build());
    }

    public List<AccessLogSummary> findYearOnYearUsers(String mode) {
        Field<Integer> distinctCountsField = DSL.countDistinct(ACCESS_LOG.USER_ID).as("counts");
        Field<Integer> allCountsField = DSL.count(ACCESS_LOG.USER_ID).as("counts");

        Field<Integer> countsField = StringUtilities.safeEq(mode, "distinct") ? distinctCountsField : allCountsField;
        Field<Integer> yearField = DSL.extract(ACCESS_LOG.CREATED_AT, DatePart.YEAR);

        return dsl
                .select(countsField, yearField.as("year"))
                .from(ACCESS_LOG)
                .groupBy(yearField)
                .fetch(r -> ImmutableAccessLogSummary
                        .builder()
                        .counts(r.get("counts", Long.class))
                        .year(r.get("year", Integer.class))
                        .build());
    }

    public List<Integer> findAccessLogYears() {
        return dsl
                .selectDistinct(DSL.extract(ACCESS_LOG.CREATED_AT, DatePart.YEAR).as("year"))
                .from(ACCESS_LOG)
                .fetch(r -> r.get("year", Integer.class));
    }

    public List<AccessLogSummary> findMonthOnMonthUsers(String mode, Integer currentYear) {
        Field<Integer> distinctCountsField = DSL.countDistinct(ACCESS_LOG.USER_ID).as("counts");
        Field<Integer> allCountsField = DSL.count(ACCESS_LOG.USER_ID).as("counts");

        Field<Integer> countsField = StringUtilities.safeEq(mode, "distinct") ? distinctCountsField : allCountsField;
        Field<Integer> monthField = DSL.extract(ACCESS_LOG.CREATED_AT, DatePart.MONTH);


        return dsl
                .select(countsField, monthField.as("month"))
                .from(ACCESS_LOG)
                .where(DSL.extract(ACCESS_LOG.CREATED_AT, DatePart.YEAR).eq(currentYear))
                .groupBy(monthField)
                .fetch(r -> ImmutableAccessLogSummary
                        .builder()
                        .counts(r.get("counts", Long.class))
                        .month(r.get("month", Integer.class))
                        .build());
    }

}
