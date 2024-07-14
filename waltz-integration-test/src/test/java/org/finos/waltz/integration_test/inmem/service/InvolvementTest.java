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

package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.HierarchyQueryScope;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.service.application.ApplicationService;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.finos.waltz.test_common.helpers.InvolvementHelper;
import org.finos.waltz.test_common.helpers.PersonHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.finos.waltz.common.CollectionUtilities.first;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class InvolvementTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private PersonHelper personHelper;

    @Autowired
    private InvolvementHelper involvementHelper;

    @Autowired
    private ApplicationService appSvc;

    @Test
    public void directInvolvementsAreFound() {

        EntityReference appA = appHelper.createNewApp("a", ouIds.a);
        Long personA = personHelper.createPerson("pa");
        long ik = involvementHelper.mkInvolvementKind("rel_a");
        involvementHelper.createInvolvement(personA, ik, appA);

        rebuildHierarchy(EntityKind.PERSON);

        IdSelectionOptions opts = mkOpts(
                mkRef(EntityKind.PERSON, personA),
                HierarchyQueryScope.CHILDREN);

        List<Application> apps = appSvc.findByAppIdSelector(opts);
        assertEquals("Expected only one app to be found", 1, apps.size());
        assertEquals("Should be the app we created earlier", appA, first(apps).entityReference());
    }


    @Test
    public void indirectInvolvementsAreFound() {
        EntityReference appA = appHelper.createNewApp("a", ouIds.a);
        EntityReference appB = appHelper.createNewApp("b", ouIds.a);
        EntityReference appC = appHelper.createNewApp("c", ouIds.a);

        Long personA = personHelper.createPerson("pa");
        Long personB = personHelper.createPerson("pb");
        Long personC = personHelper.createPerson("pc");

        personHelper.updateManager(personC, personB);
        personHelper.updateManager(personB, personA);

        long ik = involvementHelper.mkInvolvementKind("app_rel");
        involvementHelper.createInvolvement(personA, ik, appA);
        involvementHelper.createInvolvement(personB, ik, appB);
        involvementHelper.createInvolvement(personC, ik, appC);

        rebuildHierarchy(EntityKind.PERSON);

        IdSelectionOptions opts = mkOpts(
                mkRef(EntityKind.PERSON, personA),
                HierarchyQueryScope.CHILDREN);

        List<Application> apps = appSvc.findByAppIdSelector(opts);
        assertEquals("Expected only three apps to be found", 3, apps.size());
        assertTrue(map(apps, Application::entityReference).containsAll(asSet(appA, appB, appC)));
    }


    @Test
    public void onlyIndirectInvolvementsMarkedAsTransitiveAreFound() {
        EntityReference appA1 = appHelper.createNewApp("a1", ouIds.a);
        EntityReference appA2 = appHelper.createNewApp("a2", ouIds.a);
        EntityReference appB = appHelper.createNewApp("b", ouIds.a);
        EntityReference appC1 = appHelper.createNewApp("c1", ouIds.a);
        EntityReference appC2 = appHelper.createNewApp("c2", ouIds.a);

        Long personA = personHelper.createPerson("pa");
        Long personB = personHelper.createPerson("pb");
        Long personC = personHelper.createPerson("pc");

        personHelper.updateManager(personC, personB);
        personHelper.updateManager(personB, personA);

        long trans = involvementHelper.mkInvolvementKind("app_rel_trans");
        long intrans = involvementHelper.mkInvolvementKind("app_rel_intrans");

        involvementHelper.markAsIntransitive(intrans);

        involvementHelper.createInvolvement(personA, trans, appA1);
        involvementHelper.createInvolvement(personA, intrans, appA2);
        involvementHelper.createInvolvement(personB, trans, appB);
        involvementHelper.createInvolvement(personC, trans, appC1);
        involvementHelper.createInvolvement(personC, intrans, appC2);

        rebuildHierarchy(EntityKind.PERSON);

        IdSelectionOptions opts = mkOpts(
                mkRef(EntityKind.PERSON, personA),
                HierarchyQueryScope.CHILDREN);

        List<Application> apps = appSvc.findByAppIdSelector(opts);
        assertEquals("Expected only four apps to be found", 4, apps.size());
        assertTrue("Expecting C2 to be missing, but A2 should be there", map(apps, Application::entityReference).containsAll(asSet(appA1, appA2, appB, appC1)));
    }

}