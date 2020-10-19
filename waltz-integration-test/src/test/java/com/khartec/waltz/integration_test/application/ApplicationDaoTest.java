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

package com.khartec.waltz.integration_test.application;

import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.integration_test.BaseIntegrationTest;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.application.Application;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.CollectionUtilities.any;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ApplicationDaoTest extends BaseIntegrationTest {

    private final ApplicationIdSelectorFactory idSelectorFactory = new ApplicationIdSelectorFactory();
    private final ApplicationDao appDao = ctx.getBean(ApplicationDao.class);
    private Long rbOu;
    private Long raaOu;
    private Long raOu;
    private Long rootOu;
    private EntityReference r1;
    private EntityReference ra2;
    private EntityReference raa3;
    private EntityReference rb4;


    @Before
    public void before() {
        rootOu = createOrgUnit("r", null);
        raOu = createOrgUnit("ra", rootOu);
        raaOu = createOrgUnit("raa", raOu);
        rbOu = createOrgUnit("rb", rootOu);

        rebuildHierarachy(EntityKind.ORG_UNIT);

        r1 = createNewApp("r1", rootOu);
        ra2 = createNewApp("ra2", raOu);
        raa3 = createNewApp("raa3", raaOu);
        rb4 = createNewApp("rb4", rbOu);
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
        assertEquals(
                format("Expected %d apps, but got %d", expectedRefs.length, result.size()),
                expectedRefs.length, result.size());
        checkHasApps(result, expectedRefs);
    }


    private void checkHasApps(Collection<Application> results,
                            EntityReference... refs) {
        newArrayList(refs).forEach(
            r -> assertTrue(
                    format("Cannot find %s in %s", r, results),
                    any(results, d -> d.entityReference().equals(r))));
    }

}
