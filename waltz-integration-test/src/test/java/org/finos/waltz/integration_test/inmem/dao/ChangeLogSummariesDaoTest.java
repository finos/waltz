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

import org.finos.waltz.data.changelog.ChangeLogSummariesDao;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.finos.waltz.schema.tables.ChangeLog.CHANGE_LOG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ChangeLogSummariesDaoTest extends BaseInMemoryIntegrationTest {

    private static final LocalDate RANGE_START = LocalDate.of(2024, 1, 1);
    private static final LocalDate RANGE_END = LocalDate.of(2024, 12, 31);

    @Autowired
    private ChangeLogSummariesDao dao;


    @BeforeEach
    public void clearChangeLog() {
        getDsl().deleteFrom(CHANGE_LOG).execute();
    }


    @Test
    public void changesByPeriodMonthGroupsCountsAndDistinctUsers() {
        insert("APPLICATION", "u1", "INFORMATION", "UPDATE", null, LocalDateTime.of(2024, 3, 10, 9, 0));
        insert("APPLICATION", "u1", "INFORMATION", "UPDATE", null, LocalDateTime.of(2024, 3, 12, 9, 0));
        insert("APPLICATION", "u2", "INFORMATION", "UPDATE", null, LocalDateTime.of(2024, 3, 20, 9, 0));
        insert("APPLICATION", "u1", "INFORMATION", "UPDATE", null, LocalDateTime.of(2024, 4, 2, 9, 0));

        Map<String, Map<String, Long>> byPeriod = dao.findChangesByPeriod(null, null, RANGE_START, RANGE_END, Duration.MONTH);

        assertEquals(2, byPeriod.size());
        assertEquals(3L, byPeriod.get("2024-03").get("counts"));
        assertEquals(2L, byPeriod.get("2024-03").get("distinctUserCount"));
        assertEquals(1L, byPeriod.get("2024-04").get("counts"));
    }


    @Test
    public void changesByPeriodRespectsDateRange() {
        insert("APPLICATION", "u1", "INFORMATION", "UPDATE", null, LocalDateTime.of(2024, 2, 15, 9, 0));
        insert("APPLICATION", "u1", "INFORMATION", "UPDATE", null, LocalDateTime.of(2024, 3, 15, 9, 0));

        Map<String, Map<String, Long>> byPeriod = dao.findChangesByPeriod(
                null, null, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 31), Duration.MONTH);

        assertEquals(1, byPeriod.size());
        assertEquals(1L, byPeriod.get("2024-03").get("counts"));
    }


    @Test
    public void changesBySeverityCounts() {
        insert("APPLICATION", "u1", "INFORMATION", "UPDATE", null, LocalDateTime.of(2024, 3, 1, 9, 0));
        insert("APPLICATION", "u1", "INFORMATION", "UPDATE", null, LocalDateTime.of(2024, 3, 2, 9, 0));
        insert("APPLICATION", "u2", "ERROR", "UPDATE", null, LocalDateTime.of(2024, 3, 3, 9, 0));

        Map<String, Long> bySeverity = dao.findChangesBySeverity(RANGE_START, RANGE_END);

        assertEquals(2L, bySeverity.get("INFORMATION"));
        assertEquals(1L, bySeverity.get("ERROR"));
    }


    @Test
    public void changesByOperationCounts() {
        insert("APPLICATION", "u1", "INFORMATION", "ADD", null, LocalDateTime.of(2024, 3, 1, 9, 0));
        insert("APPLICATION", "u1", "INFORMATION", "ADD", null, LocalDateTime.of(2024, 3, 2, 9, 0));
        insert("APPLICATION", "u2", "INFORMATION", "UPDATE", null, LocalDateTime.of(2024, 3, 3, 9, 0));

        Map<String, Long> byOperation = dao.findChangesByOperation(RANGE_START, RANGE_END);

        assertEquals(2L, byOperation.get("ADD"));
        assertEquals(1L, byOperation.get("UPDATE"));
    }


    @Test
    public void changesByEntityKindOrdersAndLimits() {
        insert("APPLICATION", "u1", "INFORMATION", "UPDATE", null, LocalDateTime.of(2024, 3, 1, 9, 0));
        insert("APPLICATION", "u1", "INFORMATION", "UPDATE", null, LocalDateTime.of(2024, 3, 2, 9, 0));
        insert("APPLICATION", "u2", "INFORMATION", "UPDATE", null, LocalDateTime.of(2024, 3, 3, 9, 0));
        insert("MEASURABLE", "u1", "INFORMATION", "UPDATE", null, LocalDateTime.of(2024, 3, 4, 9, 0));

        Map<String, Long> byKind = dao.findChangesByEntityKind(RANGE_START, RANGE_END, 1);

        assertEquals(1, byKind.size());
        assertEquals(3L, byKind.get("APPLICATION"));
    }


    @Test
    public void changesByChildKindCounts() {
        insert("APPLICATION", "u1", "INFORMATION", "UPDATE", "MEASURABLE", LocalDateTime.of(2024, 3, 1, 9, 0));
        insert("APPLICATION", "u1", "INFORMATION", "UPDATE", "MEASURABLE", LocalDateTime.of(2024, 3, 2, 9, 0));
        insert("APPLICATION", "u2", "INFORMATION", "UPDATE", null, LocalDateTime.of(2024, 3, 3, 9, 0)); // null child -> excluded

        Map<String, Long> byChild = dao.findChangesByChildKind(RANGE_START, RANGE_END, 10);

        assertEquals(1, byChild.size());
        assertEquals(2L, byChild.get("MEASURABLE"));
    }


    @Test
    public void topContributorsOrdersAndLimits() {
        insert("APPLICATION", "u1", "INFORMATION", "UPDATE", null, LocalDateTime.of(2024, 3, 1, 9, 0));
        insert("APPLICATION", "u1", "INFORMATION", "UPDATE", null, LocalDateTime.of(2024, 3, 2, 9, 0));
        insert("APPLICATION", "u1", "INFORMATION", "UPDATE", null, LocalDateTime.of(2024, 3, 3, 9, 0));
        insert("APPLICATION", "u2", "INFORMATION", "UPDATE", null, LocalDateTime.of(2024, 3, 4, 9, 0));

        Map<String, Long> top = dao.findTopContributors(RANGE_START, RANGE_END, 1);

        assertEquals(1, top.size());
        assertEquals(3L, top.get("u1"));
    }


    @Test
    public void changesByDayOfWeekUsesIsoNumbering() {
        insert("APPLICATION", "u1", "INFORMATION", "UPDATE", null, LocalDateTime.of(2024, 3, 11, 9, 0));  // Monday
        insert("APPLICATION", "u2", "INFORMATION", "UPDATE", null, LocalDateTime.of(2024, 3, 11, 10, 0)); // Monday
        insert("APPLICATION", "u1", "INFORMATION", "UPDATE", null, LocalDateTime.of(2024, 3, 12, 9, 0));  // Tuesday

        Map<Integer, Long> byDow = dao.findChangesByDayOfWeek(RANGE_START, RANGE_END);

        assertEquals(2L, byDow.get(1)); // Monday = 1 (ISO)
        assertEquals(1L, byDow.get(2)); // Tuesday = 2 (ISO)
    }


    @Test
    public void operationTrendsBucketsByPeriodPerOperation() {
        insert("APPLICATION", "u1", "INFORMATION", "ADD", null, LocalDateTime.of(2024, 3, 10, 9, 0));
        insert("APPLICATION", "u1", "INFORMATION", "ADD", null, LocalDateTime.of(2024, 3, 12, 9, 0));
        insert("APPLICATION", "u1", "INFORMATION", "ADD", null, LocalDateTime.of(2024, 4, 2, 9, 0));
        insert("APPLICATION", "u2", "INFORMATION", "UPDATE", null, LocalDateTime.of(2024, 3, 20, 9, 0));

        Map<String, Map<String, Long>> trends = dao.findOperationTrends(RANGE_START, RANGE_END, Duration.MONTH);

        assertEquals(2L, trends.get("ADD").get("2024-03"));
        assertEquals(1L, trends.get("ADD").get("2024-04"));
        assertEquals(1L, trends.get("UPDATE").get("2024-03"));
        assertNull(trends.get("UPDATE").get("2024-04"));
    }


    @Test
    public void topContributorsByPeriodBucketsPerUser() {
        insert("APPLICATION", "u1", "INFORMATION", "UPDATE", null, LocalDateTime.of(2024, 3, 10, 9, 0));
        insert("APPLICATION", "u1", "INFORMATION", "UPDATE", null, LocalDateTime.of(2024, 4, 2, 9, 0));
        insert("APPLICATION", "u2", "INFORMATION", "UPDATE", null, LocalDateTime.of(2024, 3, 20, 9, 0));

        Map<String, Map<String, Long>> trends = dao.findTopContributorsByPeriod(RANGE_START, RANGE_END, Duration.MONTH, 10);

        assertFalse(trends.isEmpty());
        assertEquals(1L, trends.get("u1").get("2024-03"));
        assertEquals(1L, trends.get("u1").get("2024-04"));
    }


    private void insert(String parentKind,
                        String userId,
                        String severity,
                        String operation,
                        String childKind,
                        LocalDateTime createdAt) {
        getDsl()
                .insertInto(CHANGE_LOG)
                .columns(CHANGE_LOG.PARENT_KIND,
                        CHANGE_LOG.PARENT_ID,
                        CHANGE_LOG.MESSAGE,
                        CHANGE_LOG.USER_ID,
                        CHANGE_LOG.SEVERITY,
                        CHANGE_LOG.OPERATION,
                        CHANGE_LOG.CHILD_KIND,
                        CHANGE_LOG.CREATED_AT)
                .values(parentKind,
                        counter.incrementAndGet(),
                        "test change",
                        userId,
                        severity,
                        operation,
                        childKind,
                        Timestamp.valueOf(createdAt))
                .execute();
    }
}
