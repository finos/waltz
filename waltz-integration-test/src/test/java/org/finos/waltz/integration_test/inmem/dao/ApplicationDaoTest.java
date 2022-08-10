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

import org.finos.waltz.data.application.ApplicationDao;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

import static java.lang.String.format;
import static org.finos.waltz.common.CollectionUtilities.any;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ApplicationDaoTest extends BaseInMemoryIntegrationTest {

    private final ApplicationIdSelectorFactory idSelectorFactory = new ApplicationIdSelectorFactory();

    @Autowired
    private ApplicationDao appDao;

    @Autowired
    private AppHelper appHelper;

    private Long rbOu;
    private Long raaOu;
    private Long raOu;
    private Long rootOu;
    private EntityReference r1;
    private EntityReference ra2;
    private EntityReference raa3;
    private EntityReference rb4;


    @BeforeEach
    public void before() {
        rootOu = createOrgUnit("r", null);
        raOu = createOrgUnit("ra", rootOu);
        raaOu = createOrgUnit("raa", raOu);
        rbOu = createOrgUnit("rb", rootOu);

        rebuildHierarchy(EntityKind.ORG_UNIT);

        r1 = appHelper.createNewApp("r1", rootOu);
        ra2 = appHelper.createNewApp("ra2", raOu);
        raa3 = appHelper.createNewApp("raa3", raaOu);
        rb4 = appHelper.createNewApp("rb4", rbOu);
    }


    @Test
    public void usingAppSelectorWithAnOrgUnitWorks() {
        checkAppIdSelectorForOrgUnit(rootOu, r1, ra2, raa3, rb4);
        checkAppIdSelectorForOrgUnit(raOu, ra2, raa3);
        checkAppIdSelectorForOrgUnit(rbOu, rb4);
    }



    private void checkAppIdSelectorForOrgUnit(Long ouId,
                                              EntityReference... expectedRefs) {
        checkAppIdSelectorForRef(
                mkRef(EntityKind.ORG_UNIT, ouId),
                expectedRefs);
    }


    private void checkAppIdSelectorForRef(EntityReference selectorRef,
                                          EntityReference... expectedRefs) {
        List<Application> result = appDao.findByAppIdSelector(idSelectorFactory.apply(mkOpts(selectorRef)));
        assertEquals(expectedRefs.length, result.size());
        checkHasApps(result, expectedRefs);
    }


    private void checkHasApps(Collection<Application> results,
                            EntityReference... refs) {
        newArrayList(refs).forEach(
                r -> assertTrue(
                        any(results, d -> d.entityReference().equals(r)),
                        format("Cannot find %s in %s", r, results)));
    }

}
