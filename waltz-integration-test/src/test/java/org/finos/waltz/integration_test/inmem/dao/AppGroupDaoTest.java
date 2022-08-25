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

import org.finos.waltz.common.CollectionUtilities;
import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.data.app_group.AppGroupOrganisationalUnitDao;
import org.finos.waltz.data.application.ApplicationDao;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.app_group.AppGroupEntry;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.test_common.helpers.AppGroupHelper;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

import static java.lang.String.format;
import static org.finos.waltz.common.CollectionUtilities.any;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AppGroupDaoTest extends BaseInMemoryIntegrationTest {

    private final ApplicationIdSelectorFactory idSelectorFactory = new ApplicationIdSelectorFactory();

    @Autowired
    private ApplicationDao appDao;

    @Autowired
    private AppGroupOrganisationalUnitDao appGroupOuDao;

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private AppGroupHelper appGroupHelper;

    private Long raOu;
    private EntityReference r1;
    private EntityReference ra2;
    private EntityReference raa3;


    @BeforeEach
    public void before() {
        Long rootOu = createOrgUnit("r", null);
        raOu = createOrgUnit("ra", rootOu);
        Long raaOu = createOrgUnit("raa", raOu);
        Long rbOu = createOrgUnit("rb", rootOu);

        rebuildHierarchy(EntityKind.ORG_UNIT);

        r1 = appHelper.createNewApp("r1", rootOu);
        ra2 = appHelper.createNewApp("ra2", raOu);
        raa3 = appHelper.createNewApp("raa3", raaOu);
        appHelper.createNewApp("rb4", rbOu);
    }


    @Test
    public void usingAppSelectorWithAPlainAppGroupWorks() throws InsufficientPrivelegeException {
        Long gId = appGroupHelper.createAppGroupWithAppRefs("t1", asSet(r1, ra2));
        checkAppIdSelectorForRef(mkRef(EntityKind.APP_GROUP, gId), r1, ra2);
    }


    @Test
    public void usingAppSelectorWithAComplexAppGroupWorks() throws InsufficientPrivelegeException {
        Long gId = appGroupHelper.createAppGroupWithAppRefs("t2", asSet(r1));
        appGroupOuDao.addOrgUnit(gId, raOu);
        checkAppIdSelectorForRef(
                mkRef(EntityKind.APP_GROUP, gId),
                r1,
                ra2,
                raa3);
    }


    @Test
    public void canAddOrgUnitsToGroups() throws InsufficientPrivelegeException {
        Long gId = appGroupHelper.createAppGroupWithAppRefs("t3", asSet(r1));
        appGroupOuDao.addOrgUnit(gId, raOu);
        List<AppGroupEntry> ouEntries = appGroupOuDao.getEntriesForGroup(gId);
        assertEquals(1, ouEntries.size());
        AppGroupEntry entry = CollectionUtilities.first(ouEntries);
        assertEquals(EntityKind.ORG_UNIT, entry.kind());
        assertEquals(raOu, Long.valueOf(entry.id()));
    }


    @Test
    public void canRemoveOrgUnitsFromGroups() throws InsufficientPrivelegeException {
        Long gId = appGroupHelper.createAppGroupWithAppRefs("t3", asSet(r1));
        appGroupOuDao.addOrgUnit(gId, raOu);
        assertEquals(1, appGroupOuDao.getEntriesForGroup(gId).size());
        appGroupOuDao.removeOrgUnit(gId, raOu);
        assertEquals(0, appGroupOuDao.getEntriesForGroup(gId).size());
    }



    private void checkAppIdSelectorForRef(EntityReference selectorRef,
                                          EntityReference... expectedRefs) {
        List<Application> result = appDao.findByAppIdSelector(idSelectorFactory.apply(mkOpts(selectorRef)));
        assertEquals(expectedRefs.length, result.size(),
                format("Expected %d apps, but got %d", expectedRefs.length, result.size()));
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
