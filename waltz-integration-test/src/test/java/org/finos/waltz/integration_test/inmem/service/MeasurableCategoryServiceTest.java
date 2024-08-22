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

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.exceptions.NotAuthorizedException;
import org.finos.waltz.model.measurable_category.ImmutableMeasurableCategory;
import org.finos.waltz.model.measurable_category.MeasurableCategory;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.service.measurable_category.MeasurableCategoryService;
import org.finos.waltz.test_common.helpers.ChangeLogHelper;
import org.finos.waltz.test_common.helpers.RatingSchemeHelper;
import org.finos.waltz.test_common.helpers.UserHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.junit.Assert.*;

public class MeasurableCategoryServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private MeasurableCategoryService mcSvc;

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private RatingSchemeHelper ratingSchemeHelper;

    @Autowired
    private ChangeLogHelper changeLogHelper;


    @Test
    public void needToBeAnAdminOrTaxonomyEditorToSaveCategories() {
        String admin = mkName("mc_admin");
        String taxonomyEditor = mkName("mc_tax");
        String unauthorised = mkName("unauthorised");
        userHelper.createUserWithSystemRoles(admin, SetUtilities.asSet(SystemRole.ADMIN));
        userHelper.createUserWithSystemRoles(taxonomyEditor, SetUtilities.asSet(SystemRole.TAXONOMY_EDITOR));
        userHelper.createUserWithRoles(unauthorised, "WIBBLE");

        ImmutableMeasurableCategory mc1 = mkMeasurableCategory("mc1");
        assertTrue("Should work as has ADMIN role", mcSvc.save(mc1, admin) > 0L);

        ImmutableMeasurableCategory mc2 = mkMeasurableCategory("mc2");
        assertTrue("Should work as has TAXONOMY_EDITOR role", mcSvc.save(mc2, taxonomyEditor) > 0L);

        ImmutableMeasurableCategory mc3 = mkMeasurableCategory("mc3");
        assertThrows("Should not work as incorrect roles", NotAuthorizedException.class, () -> mcSvc.save(mc3, unauthorised));
    }


    @Test
    public void categoriesCanBeUpdatedByRepeatedCallsToSave() {
        String user = mkName("mc_tax");
        userHelper.createUserWithSystemRoles(user, SetUtilities.asSet(SystemRole.TAXONOMY_EDITOR));

        ImmutableMeasurableCategory v1Pre = mkMeasurableCategory("mc");
        String v1Name = v1Pre.name();
        Long mcId = mcSvc.save(v1Pre, user);
        MeasurableCategory v1Post = mcSvc.getById(mcId);
        assertEquals("Expected initially saved name to match", v1Name, v1Post.name());

        EntityReference mcRef = mkRef(EntityKind.MEASURABLE_CATEGORY, mcId);
        changeLogHelper.assertChangeLogContainsAtLeastOneMatchingOperation(mcRef, Operation.ADD);

        ImmutableMeasurableCategory v2Pre = ImmutableMeasurableCategory.copyOf(v1Post)
                .withName(mkName("updated_mc"))
                .withDescription("updated desc")
                .withAllowPrimaryRatings(false)
                .withIcon("fire");

        mcSvc.save(v2Pre, user);
        MeasurableCategory v2Post = mcSvc.getById(mcId);
        assertEquals("Expected updated name to match", v2Pre.name(), v2Post.name());
        assertEquals("Expected updated desc to match", v2Pre.description(), v2Post.description());
        assertEquals("Expected updated primary ratings to match", v2Pre.allowPrimaryRatings(), v2Post.allowPrimaryRatings());
        assertEquals("Expected updated icon to match", v2Pre.icon(), v2Post.icon());

        changeLogHelper.assertChangeLogContainsAtLeastOneMatchingOperation(mcRef, Operation.UPDATE);
    }


    // --- util

    private ImmutableMeasurableCategory mkMeasurableCategory(String stem) {
        return ImmutableMeasurableCategory
                .builder()
                .name(mkName(stem))
                .externalId(mkName(stem.toUpperCase()))
                .description(stem + " desc")
                .icon("cog")
                .allowPrimaryRatings(true)
                .editable(true)
                .lastUpdatedBy("mctest")
                .ratingSchemeId(ratingSchemeHelper.createEmptyRatingScheme(stem + "_rs"))
                .build();
    }


}