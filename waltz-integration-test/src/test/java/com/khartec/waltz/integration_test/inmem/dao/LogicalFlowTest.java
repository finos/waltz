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

package com.khartec.waltz.integration_test.inmem.dao;

import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import org.jooq.Record1;
import org.jooq.SelectJoinStep;
import org.junit.Before;
import org.junit.Test;

import static com.khartec.waltz.schema.Tables.APPLICATION;
import static org.junit.Assert.assertNotNull;

public class LogicalFlowTest extends BaseInMemoryIntegrationTest {

    private LogicalFlowDao dao;

    @Before
    public void setupOuTest() {
        System.out.println("ouTest::setup");
        dao = ctx.getBean(LogicalFlowDao.class);
    }


    @Test
    public void foo() {
        EntityReference a = createNewApp("a", ouIds.a);
        EntityReference b = createNewApp("b", ouIds.b);
        SelectJoinStep<Record1<String>> qry = getDsl().select(APPLICATION.NAME).from(APPLICATION);
        System.out.println(qry);
        qry.fetch().format(System.out);
        LogicalFlow lf = createLogicalFlow(a, b);
        assertNotNull(lf);
    }

}