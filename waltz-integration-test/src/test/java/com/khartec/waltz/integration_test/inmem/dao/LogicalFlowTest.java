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
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import org.junit.Before;
import org.junit.Test;

import static com.khartec.waltz.common.CollectionUtilities.isEmpty;
import static com.khartec.waltz.common.SetUtilities.asSet;
import static com.khartec.waltz.common.SetUtilities.map;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.HierarchyQueryScope.CHILDREN;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LogicalFlowTest extends BaseInMemoryIntegrationTest {

    private LogicalFlowDao dao;

    @Before
    public void setupOuTest() {
        System.out.println("ouTest::setup");
        dao = ctx.getBean(LogicalFlowDao.class);
    }


    @Test
    public void basicDirectAssociations() {
        EntityReference a = createNewApp("a", ouIds.a);
        EntityReference b = createNewApp("b", ouIds.a1);
        EntityReference c = createNewApp("c", ouIds.b);
        EntityReference d = createNewApp("c", ouIds.b);
        // a -> b
        // a -> d
        // c
        LogicalFlow ab = createLogicalFlow(a, b);
        LogicalFlow ad = createLogicalFlow(a, d);

        assertEquals(
                "Can see flow associated to 'a'",
                asSet(ab.id(), ad.id()),
                map(dao.findByEntityReference(a), IdProvider::id));

        assertEquals(
                "Can sees flows associated to 'b'",
                asSet(ab.id()),
                map(dao.findByEntityReference(b), IdProvider::id));

        assertEquals(
                "Can sees flows associated to 'b'",
                asSet(ad.id()),
                map(dao.findByEntityReference(d), IdProvider::id));

        assertTrue(
                "Can sees nothing associated to 'c'",
                isEmpty(dao.findByEntityReference(c)));
    }


    @Test
    public void bySelector() {

        EntityReference a = createNewApp("a", ouIds.a);
        EntityReference b = createNewApp("b", ouIds.a1);
        EntityReference d = createNewApp("c", ouIds.b);
        // a -> b
        // a -> d
        // c
        LogicalFlow ab = createLogicalFlow(a, b);
        LogicalFlow ad = createLogicalFlow(a, d);

        assertEquals("find by root ou gives all",
                asSet(ab.id(), ad.id()),
                map(dao.findBySelector(logicalFlowIdSelectorFactory.apply(
                        mkOpts(
                            mkRef(EntityKind.ORG_UNIT, ouIds.root),
                            CHILDREN))),
                    IdProvider::id));


        assertEquals("find by ou 'b' gives only one flow",
                asSet(ad.id()),
                map(dao.findBySelector(logicalFlowIdSelectorFactory.apply(
                            mkOpts(
                                mkRef(EntityKind.ORG_UNIT, ouIds.b),
                                CHILDREN))),
                        IdProvider::id));

    }

}