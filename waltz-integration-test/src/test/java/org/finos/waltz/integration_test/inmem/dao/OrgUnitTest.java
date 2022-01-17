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

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.orgunit.OrganisationalUnitDao;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.HierarchyQueryScope;
import org.finos.waltz.model.orgunit.OrganisationalUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.finos.waltz.model.utils.IdUtilities.toIds;
import static org.junit.jupiter.api.Assertions.*;

public class OrgUnitTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private OrganisationalUnitDao dao;


    @Test
    public void ouCanBeRetrievedById() {
        OrganisationalUnit rootOU = dao.getById(ouIds.root);
        assertNotNull(rootOU);
    }


    @Test
    public void ousCanBeRetrievedByIds() {
        assertEquals(
                asSet(ouIds.root, ouIds.b),
                toIds(dao.findByIds(ouIds.root, ouIds.b)));
    }


    @Test
    public void ousCanBeRetrievedAsRefs_RatherThanAsFullObjects() {
        assertEquals(
                asSet(ouIds.a, ouIds.a1),
                SetUtilities.map(
                        dao.findByIdSelectorAsEntityReference(ouSelectorFactory.apply(
                                mkOpts(
                                    mkRef(EntityKind.ORG_UNIT, ouIds.a),
                                    HierarchyQueryScope.CHILDREN))),
                        EntityReference::id));
    }


    @Test
    public void ousCanBeRetrievedBySelector() {
        assertEquals(
                asSet(ouIds.root, ouIds.a, ouIds.a1, ouIds.b),
                toIds(dao.findBySelector(ouSelectorFactory.apply(mkOpts(
                        mkRef(EntityKind.ORG_UNIT, ouIds.root),
                        HierarchyQueryScope.CHILDREN)))));

        assertEquals(
                asSet(ouIds.a, ouIds.a1),
                toIds(dao.findBySelector(ouSelectorFactory.apply(mkOpts(
                        mkRef(EntityKind.ORG_UNIT, ouIds.a),
                        HierarchyQueryScope.CHILDREN)))));

        assertEquals(
                asSet(ouIds.root, ouIds.a),
                toIds(dao.findBySelector(ouSelectorFactory.apply(mkOpts(
                        mkRef(EntityKind.ORG_UNIT, ouIds.a),
                        HierarchyQueryScope.PARENTS)))));

        assertEquals(
                asSet(ouIds.a1),
                toIds(dao.findBySelector(ouSelectorFactory.apply(mkOpts(
                        mkRef(EntityKind.ORG_UNIT, ouIds.a1),
                        HierarchyQueryScope.EXACT)))));
    }


    @Test
    public void allOusCanLoadedViaFindAll() {
        assertEquals(
                asSet(ouIds.root, ouIds.a, ouIds.a1, ouIds.b),
                toIds(dao.findAll()));
    }


    @Test
    public void ouDescriptionsCanBeUpdated() {
        assertNotEquals("updated description", dao.getById(ouIds.a).description());
        dao.updateDescription(ouIds.a, "updated description");
        assertEquals("updated description", dao.getById(ouIds.a).description());
    }

}