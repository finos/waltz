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

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.ImmutableGenericSelector;
import org.finos.waltz.data.cost.CostDao;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.cost.EntityCost;
import org.finos.waltz.schema.tables.records.CostKindRecord;
import org.finos.waltz.schema.tables.records.CostRecord;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.schema.Tables.COST;
import static org.finos.waltz.schema.Tables.COST_KIND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class CostDaoTest extends BaseInMemoryIntegrationTest {

    private static final int YEAR = 2024;

    @Autowired
    private CostDao costDao;

    @Autowired
    private DataSource dataSource;


    @BeforeEach
    public void wipeCosts() {
        // COST references COST_KIND, delete children first
        dsl.deleteFrom(COST).execute();
        dsl.deleteFrom(COST_KIND).execute();
    }


    @Test
    public void findTopCostsRespectsLimitAndOrdersByAmountDesc() {
        long costKindId = mkCostKind();
        // five entities with ascending amounts
        for (int i = 1; i <= 5; i++) {
            mkCost(costKindId, counter.incrementAndGet(), i * 10.0);   // 10, 20, 30, 40, 50
        }

        Set<EntityCost> top = costDao.findTopCostsForCostKindAndSelector(
                costKindId, YEAR, selectorForCostKind(costKindId), 3);

        assertEquals(3, top.size(), "should return exactly `limit` rows");

        Set<Integer> amounts = top
                .stream()
                .map(c -> c.amount().intValue())
                .collect(Collectors.toSet());
        assertEquals(newHashSet(50, 40, 30), amounts,
                "should return the `limit` highest amounts, ordered by amount desc");
    }


    /**
     * Regression test for the HikariCP connection leak: {@code findTopCostsForCostKindAndSelector}
     * previously used {@code fetchStream()...limit(limit)}, which abandons the lazy jOOQ cursor
     * (and its pooled connection) whenever the result set is larger than {@code limit}. Repeatedly
     * invoking it more times than the pool size would therefore exhaust the pool and hang.
     *
     * With the fix (eager {@code limit(limit).fetchSet(...)}) every connection is returned, so many
     * iterations complete quickly and no connections remain checked out.
     */
    @Test
    public void repeatedTopCostQueriesDoNotLeakConnections() {
        long costKindId = mkCostKind();
        // more rows than the limit, so the buggy stream would be abandoned early (and leak)
        for (int i = 0; i < 10; i++) {
            mkCost(costKindId, counter.incrementAndGet(), 100.0 + i);
        }

        GenericSelector selector = selectorForCostKind(costKindId);

        int maxPoolSize = ((HikariDataSource) dataSource).getMaximumPoolSize();
        int iterations = (maxPoolSize + 1) * 4;   // comfortably beyond the pool

        for (int i = 0; i < iterations; i++) {
            Set<EntityCost> top = costDao.findTopCostsForCostKindAndSelector(
                    costKindId, YEAR, selector, 1);
            // returning here (rather than blocking for connectionTimeout then throwing
            // SQLTransientConnectionException) already proves connections are released
            assertEquals(1, top.size(), "each call returns exactly `limit` rows");
        }

        HikariPoolMXBean pool = ((HikariDataSource) dataSource).getHikariPoolMXBean();
        assertEquals(0, pool.getActiveConnections(),
                "no db connections should remain checked out after the queries complete (leak regression)");
        assertTrue(pool.getIdleConnections() > 0,
                "released connections should be idle and available for reuse");
    }


    // --- helpers -------------------------------------------------------------

    private long mkCostKind() {
        CostKindRecord ck = dsl.newRecord(COST_KIND);
        ck.setName("costKind" + counter.incrementAndGet());
        ck.setExternalId("CK" + counter.incrementAndGet());
        ck.setSubjectKind(EntityKind.APPLICATION.name());
        ck.setIsDefault(false);
        ck.store();
        return ck.getId();
    }


    private void mkCost(long costKindId, long entityId, double amount) {
        CostRecord c = dsl.newRecord(COST);
        c.setCostKindId(costKindId);
        c.setEntityId(entityId);
        c.setEntityKind(EntityKind.APPLICATION.name());
        c.setYear(YEAR);
        c.setAmount(BigDecimal.valueOf(amount));
        c.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
        c.setLastUpdatedBy("costDaoTest");
        c.setProvenance("integration-test");
        c.store();
    }


    /**
     * Selects the entity ids that have a cost for the given cost kind, as an
     * {@code APPLICATION} generic selector (avoids needing real APPLICATION rows).
     */
    private GenericSelector selectorForCostKind(long costKindId) {
        return ImmutableGenericSelector.builder()
                .kind(EntityKind.APPLICATION)
                .selector(DSL
                        .select(COST.ENTITY_ID)
                        .from(COST)
                        .where(COST.COST_KIND_ID.eq(costKindId)))
                .build();
    }


    private static Set<Integer> newHashSet(Integer... values) {
        return java.util.Arrays.stream(values).collect(Collectors.toSet());
    }

}
