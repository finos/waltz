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

import org.finos.waltz.data.logical_flow.LogicalFlowDao;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdProvider;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.test_common_again.helpers.AppHelper;
import org.finos.waltz.test_common_again.helpers.LogicalFlowHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.finos.waltz.common.CollectionUtilities.isEmpty;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.HierarchyQueryScope.CHILDREN;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LogicalFlowTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private LogicalFlowDao lfDao;

    @Autowired
    private LogicalFlowHelper helper;

    @Autowired
    private AppHelper appHelper;


    @Test
    public void basicDirectAssociations() {
        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        EntityReference c = appHelper.createNewApp("c", ouIds.b);
        EntityReference d = appHelper.createNewApp("c", ouIds.b);
        // a -> b
        // a -> d
        // c
        LogicalFlow ab = helper.createLogicalFlow(a, b);
        LogicalFlow ad = helper.createLogicalFlow(a, d);

        assertEquals(
                asSet(ab.id(), ad.id()),
                map(lfDao.findByEntityReference(a), IdProvider::id),
                "Can see flow associated to 'a'");

        assertEquals(
                asSet(ab.id()),
                map(lfDao.findByEntityReference(b), IdProvider::id),
                "Can sees flows associated to 'b'");

        assertEquals(
                asSet(ad.id()),
                map(lfDao.findByEntityReference(d), IdProvider::id),
                "Can sees flows associated to 'd'");

        assertTrue(
                isEmpty(lfDao.findByEntityReference(c)),
                "Can sees nothing associated to 'c'");
    }


    @Test
    public void bySelector() {

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        EntityReference c = appHelper.createNewApp("c", ouIds.b);
        // a -> b
        // a -> c
        // c
        LogicalFlow ab = helper.createLogicalFlow(a, b);
        LogicalFlow ac = helper.createLogicalFlow(a, c);

        assertEquals(
                asSet(ab.id(), ac.id()),
                map(lfDao.findBySelector(logicalFlowIdSelectorFactory.apply(
                                mkOpts(
                                        mkRef(EntityKind.ORG_UNIT, ouIds.root),
                                        CHILDREN))),
                        IdProvider::id)
                , "find by root ou gives all");

        assertEquals(
                asSet(ac.id()),
                map(lfDao.findBySelector(logicalFlowIdSelectorFactory.apply(
                                mkOpts(
                                        mkRef(EntityKind.ORG_UNIT, ouIds.b),
                                        CHILDREN))),
                        IdProvider::id),
                "find by ou 'b' gives only one flow");
    }

}