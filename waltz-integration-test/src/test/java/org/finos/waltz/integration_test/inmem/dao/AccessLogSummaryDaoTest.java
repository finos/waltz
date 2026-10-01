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

package org.finos.waltz.integration_test.inmem.dao;

import org.finos.waltz.data.access_log.AccessLogDao;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.Duration;
import org.finos.waltz.model.accesslog.AccessLogSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.finos.waltz.schema.tables.AccessLog.ACCESS_LOG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccessLogSummaryDaoTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private AccessLogDao accessLogDao;


    @BeforeEach
    public void clearAccessLog() {
        getDsl().deleteFrom(ACCESS_LOG).execute();
    }


    @Test
    public void summaryByMonthGroupsCountsAndDistinctUsers() {
        insert("u1", "home", LocalDateTime.of(2024, 3, 10, 9, 0));
        insert("u1", "home", LocalDateTime.of(2024, 3, 12, 9, 0));
        insert("u2", "home", LocalDateTime.of(2024, 3, 20, 9, 0));
        insert("u1", "home", LocalDateTime.of(2024, 4, 2, 9, 0));

        Map<String, AccessLogSummary> byPeriod = summaryByPeriod(Duration.MONTH,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31));

        assertEquals(2, byPeriod.size());
        assertEquals(3L, byPeriod.get("2024-03").counts());
        assertEquals(2L, byPeriod.get("2024-03").distinctUserCount());
        assertEquals(1L, byPeriod.get("2024-04").counts());
        assertEquals(1L, byPeriod.get("2024-04").distinctUserCount());
    }


    @Test
    public void summaryByDayLabelsAsIsoDate() {
        insert("u1", "home", LocalDateTime.of(2024, 3, 10, 9, 0));
        insert("u2", "home", LocalDateTime.of(2024, 3, 10, 11, 0));
        insert("u1", "home", LocalDateTime.of(2024, 3, 11, 9, 0));

        Map<String, AccessLogSummary> byPeriod = summaryByPeriod(Duration.DAY,
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 3, 31));

        assertEquals(2, byPeriod.size());
        assertEquals(2L, byPeriod.get("2024-03-10").counts());
        assertEquals(1L, byPeriod.get("2024-03-11").counts());
    }


    @Test
    public void summaryByYearGroupsAcrossYears() {
        insert("u1", "home", LocalDateTime.of(2023, 6, 1, 9, 0));
        insert("u1", "home", LocalDateTime.of(2024, 6, 1, 9, 0));
        insert("u2", "home", LocalDateTime.of(2024, 7, 1, 9, 0));

        Map<String, AccessLogSummary> byPeriod = summaryByPeriod(Duration.YEAR,
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2024, 12, 31));

        assertEquals(2, byPeriod.size());
        assertEquals(1L, byPeriod.get("2023").counts());
        assertEquals(2L, byPeriod.get("2024").counts());
    }


    @Test
    public void summaryByWeekProducesIsoWeekLabels() {
        insert("u1", "home", LocalDateTime.of(2024, 1, 8, 9, 0));  // ISO week 2
        insert("u1", "home", LocalDateTime.of(2024, 1, 15, 9, 0)); // ISO week 3

        List<AccessLogSummary> summary = accessLogDao.findAccessLogSummary(Duration.WEEK,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31));

        assertEquals(2, summary.size());
        summary.forEach(s -> assertTrue(s.period().matches("\\d{4}-W\\d{2}"),
                "week label should look like yyyy-Www but was " + s.period()));
    }


    @Test
    public void dateRangeIsInclusiveOfEndDateAndExcludesOutside() {
        insert("u1", "home", LocalDateTime.of(2024, 2, 28, 9, 0));  // before range
        insert("u1", "home", LocalDateTime.of(2024, 3, 31, 23, 30)); // last day of range, late in day
        insert("u1", "home", LocalDateTime.of(2024, 4, 1, 0, 30));   // after range

        Map<String, AccessLogSummary> byPeriod = summaryByPeriod(Duration.DAY,
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 3, 31));

        assertEquals(1, byPeriod.size());
        assertEquals(1L, byPeriod.get("2024-03-31").counts());
    }


    @Test
    public void topPagesOrdersByCountAndRespectsLimit() {
        insert("u1", "page-a", LocalDateTime.of(2024, 3, 1, 9, 0));
        insert("u2", "page-a", LocalDateTime.of(2024, 3, 1, 9, 0));
        insert("u3", "page-a", LocalDateTime.of(2024, 3, 1, 9, 0));
        insert("u1", "page-b", LocalDateTime.of(2024, 3, 1, 9, 0));

        List<AccessLogSummary> top = accessLogDao.findTopPagesByAccess(
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 3, 31),
                1);

        assertEquals(1, top.size());
        assertEquals("page-a", top.get(0).state());
        assertEquals(3L, top.get(0).counts());
    }


    @Test
    public void activityByHourBucketsByHourOfDay() {
        insert("u1", "home", LocalDateTime.of(2024, 3, 1, 9, 15));
        insert("u2", "home", LocalDateTime.of(2024, 3, 2, 9, 45));
        insert("u1", "home", LocalDateTime.of(2024, 3, 1, 14, 5));

        Map<Integer, Long> byHour = accessLogDao
                .findActivityByHourOfDay(LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 31))
                .stream()
                .collect(Collectors.toMap(AccessLogSummary::hour, AccessLogSummary::counts));

        assertEquals(2L, byHour.get(9));
        assertEquals(1L, byHour.get(14));
    }


    @Test
    public void activityByDayOfWeekUsesIsoNumbering() {
        insert("u1", "home", LocalDateTime.of(2024, 3, 11, 9, 0));  // Monday
        insert("u2", "home", LocalDateTime.of(2024, 3, 11, 10, 0)); // Monday
        insert("u1", "home", LocalDateTime.of(2024, 3, 12, 9, 0));  // Tuesday

        Map<Integer, Long> byDow = accessLogDao
                .findActivityByDayOfWeek(LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 31))
                .stream()
                .collect(Collectors.toMap(AccessLogSummary::dayOfWeek, AccessLogSummary::counts));

        assertEquals(2L, byDow.get(1)); // Monday = 1 (ISO)
        assertEquals(1L, byDow.get(2)); // Tuesday = 2 (ISO)
    }


    @Test
    public void topActiveUsersOrdersByCountAndRespectsLimit() {
        insert("u1", "home", LocalDateTime.of(2024, 3, 1, 9, 0));
        insert("u1", "home", LocalDateTime.of(2024, 3, 2, 9, 0));
        insert("u1", "home", LocalDateTime.of(2024, 3, 3, 9, 0));
        insert("u2", "home", LocalDateTime.of(2024, 3, 1, 9, 0));

        List<AccessLogSummary> top = accessLogDao.findTopActiveUsers(
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 3, 31),
                1);

        assertEquals(1, top.size());
        assertEquals("u1", top.get(0).userId());
        assertEquals(3L, top.get(0).counts());
    }


    @Test
    public void sessionDurationsOnlyIncludesMultiAccessDays() {
        insert("u1", "home", LocalDateTime.of(2024, 3, 1, 10, 0));
        insert("u1", "detail", LocalDateTime.of(2024, 3, 1, 10, 30));
        insert("u2", "home", LocalDateTime.of(2024, 3, 1, 10, 0)); // single access -> excluded

        List<AccessLogSummary> sessions = accessLogDao.findSessionDurations(
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 3, 31));

        assertEquals(1, sessions.size());
        assertEquals("u1", sessions.get(0).userId());
        assertEquals(2L, sessions.get(0).counts());
        assertEquals(30L, sessions.get(0).sessionDuration());
    }


    private Map<String, AccessLogSummary> summaryByPeriod(Duration freq, LocalDate start, LocalDate end) {
        return accessLogDao
                .findAccessLogSummary(freq, start, end)
                .stream()
                .collect(Collectors.toMap(AccessLogSummary::period, s -> s));
    }


    private void insert(String userId, String state, LocalDateTime createdAt) {
        getDsl()
                .insertInto(ACCESS_LOG)
                .columns(ACCESS_LOG.USER_ID, ACCESS_LOG.STATE, ACCESS_LOG.CREATED_AT)
                .values(userId, state, Timestamp.valueOf(createdAt))
                .execute();
    }
}
