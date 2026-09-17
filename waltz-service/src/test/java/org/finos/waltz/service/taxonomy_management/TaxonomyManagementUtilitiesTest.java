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

package org.finos.waltz.service.taxonomy_management;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.HierarchyQueryScope;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.Severity;
import org.finos.waltz.model.exceptions.NotAuthorizedException;
import org.finos.waltz.model.measurable.ImmutableMeasurable;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.measurable_category.ImmutableMeasurableCategory;
import org.finos.waltz.model.measurable_category.MeasurableCategory;
import org.finos.waltz.model.measurable_rating.ImmutableMeasurableRating;
import org.finos.waltz.model.measurable_rating.MeasurableRating;
import org.finos.waltz.model.taxonomy_management.ImmutableTaxonomyChangeCommand;
import org.finos.waltz.model.taxonomy_management.ImmutableTaxonomyChangePreview;
import org.finos.waltz.model.taxonomy_management.TaxonomyChangeCommand;
import org.finos.waltz.model.taxonomy_management.TaxonomyChangePreview;
import org.finos.waltz.model.taxonomy_management.TaxonomyChangeType;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.service.measurable.MeasurableService;
import org.finos.waltz.service.measurable_category.MeasurableCategoryService;
import org.finos.waltz.service.measurable_rating.MeasurableRatingService;
import org.finos.waltz.service.user.UserRoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TaxonomyManagementUtilitiesTest {

    private static final long CATEGORY_ID = 10L;
    private static final EntityReference CATEGORY_REF = mkRef(EntityKind.MEASURABLE_CATEGORY, CATEGORY_ID);

    @Mock
    private MeasurableService measurableService;
    @Mock
    private MeasurableRatingService measurableRatingService;
    @Mock
    private MeasurableCategoryService measurableCategoryService;
    @Mock
    private UserRoleService userRoleService;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }


    private static Measurable mkMeasurable(long id, long categoryId, boolean concrete) {
        return ImmutableMeasurable.builder()
                .id(id)
                .name("m" + id)
                .description("")
                .categoryId(categoryId)
                .concrete(concrete)
                .lastUpdatedBy("test")
                .build();
    }


    private static TaxonomyChangeCommand mkCommand(long measurableId, Map<String, String> params) {
        return ImmutableTaxonomyChangeCommand.builder()
                .changeType(TaxonomyChangeType.UPDATE_NAME)
                .changeDomain(CATEGORY_REF)
                .primaryReference(mkRef(EntityKind.MEASURABLE, measurableId))
                .params(params)
                .createdBy("test")
                .lastUpdatedBy("test")
                .build();
    }


    private static MeasurableRating mkRating(long measurableId, EntityReference entity) {
        return ImmutableMeasurableRating.builder()
                .entityReference(entity)
                .measurableId(measurableId)
                .rating('G')
                .ratingId(1L)
                .lastUpdatedBy("test")
                .build();
    }


    // -- validateMeasurableInCategory / validatePrimaryMeasurable

    @Test
    public void validateMeasurableInCategoryReturnsMeasurableWhenInCategory() {
        Measurable m = mkMeasurable(1L, CATEGORY_ID, true);
        when(measurableService.getById(1L)).thenReturn(m);

        Measurable result = TaxonomyManagementUtilities.validateMeasurableInCategory(measurableService, 1L, CATEGORY_ID);

        assertSame(m, result);
    }


    @Test
    public void validateMeasurableInCategoryFailsWhenMissing() {
        when(measurableService.getById(1L)).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> TaxonomyManagementUtilities.validateMeasurableInCategory(measurableService, 1L, CATEGORY_ID));
    }


    @Test
    public void validateMeasurableInCategoryFailsWhenInDifferentCategory() {
        when(measurableService.getById(1L)).thenReturn(mkMeasurable(1L, 99L, true));

        assertThrows(
                IllegalArgumentException.class,
                () -> TaxonomyManagementUtilities.validateMeasurableInCategory(measurableService, 1L, CATEGORY_ID));
    }


    @Test
    public void validatePrimaryMeasurableUsesCommandReferences() {
        Measurable m = mkMeasurable(5L, CATEGORY_ID, true);
        when(measurableService.getById(5L)).thenReturn(m);

        Measurable result = TaxonomyManagementUtilities.validatePrimaryMeasurable(
                measurableService,
                mkCommand(5L, Collections.emptyMap()));

        assertSame(m, result);
    }


    // -- validateMeasurablesInCategory

    @Test
    public void validateMeasurablesInCategoryPassesWhenAllPresent() {
        when(measurableService.findByCategoryId(CATEGORY_ID))
                .thenReturn(asList(mkMeasurable(1L, CATEGORY_ID, true), mkMeasurable(2L, CATEGORY_ID, true)));

        assertDoesNotThrow(() -> TaxonomyManagementUtilities.validateMeasurablesInCategory(
                measurableService,
                asList(1L, 2L),
                CATEGORY_ID));
    }


    @Test
    public void validateMeasurablesInCategoryFailsWhenSomeMissing() {
        when(measurableService.findByCategoryId(CATEGORY_ID))
                .thenReturn(asList(mkMeasurable(1L, CATEGORY_ID, true)));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> TaxonomyManagementUtilities.validateMeasurablesInCategory(
                        measurableService,
                        asList(1L, 2L),
                        CATEGORY_ID));

        assertTrue(ex.getMessage().contains("Not all measurables"));
    }


    // -- validateTargetNotChild

    @Test
    public void validateTargetNotChildPassesWhenTargetIsNotDescendant() {
        Measurable m = mkMeasurable(1L, CATEGORY_ID, true);
        Measurable target = mkMeasurable(2L, CATEGORY_ID, true);
        Measurable child = mkMeasurable(3L, CATEGORY_ID, true);

        IdSelectionOptions expectedOpts = mkOpts(m.entityReference(), HierarchyQueryScope.CHILDREN);
        when(measurableService.findByMeasurableIdSelector(expectedOpts)).thenReturn(asList(child));

        assertDoesNotThrow(() -> TaxonomyManagementUtilities.validateTargetNotChild(measurableService, m, target));
    }


    @Test
    public void validateTargetNotChildFailsWhenTargetIsDescendant() {
        Measurable m = mkMeasurable(1L, CATEGORY_ID, true);
        Measurable target = mkMeasurable(2L, CATEGORY_ID, true);

        IdSelectionOptions expectedOpts = mkOpts(m.entityReference(), HierarchyQueryScope.CHILDREN);
        when(measurableService.findByMeasurableIdSelector(expectedOpts)).thenReturn(asList(target));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> TaxonomyManagementUtilities.validateTargetNotChild(measurableService, m, target));

        assertTrue(ex.getMessage().contains("is a child of"));
    }


    // -- validateConcreteMergeAllowed

    @Test
    public void concreteIntoAbstractMergeIsNotAllowed() {
        Measurable concrete = mkMeasurable(1L, CATEGORY_ID, true);
        Measurable abstractTarget = mkMeasurable(2L, CATEGORY_ID, false);

        assertThrows(
                IllegalArgumentException.class,
                () -> TaxonomyManagementUtilities.validateConcreteMergeAllowed(concrete, abstractTarget));
    }


    @Test
    public void otherMergeCombinationsAreAllowed() {
        Measurable concreteA = mkMeasurable(1L, CATEGORY_ID, true);
        Measurable concreteB = mkMeasurable(2L, CATEGORY_ID, true);
        Measurable abstractA = mkMeasurable(3L, CATEGORY_ID, false);
        Measurable abstractB = mkMeasurable(4L, CATEGORY_ID, false);

        assertDoesNotThrow(() -> TaxonomyManagementUtilities.validateConcreteMergeAllowed(concreteA, concreteB));
        assertDoesNotThrow(() -> TaxonomyManagementUtilities.validateConcreteMergeAllowed(abstractA, concreteB));
        assertDoesNotThrow(() -> TaxonomyManagementUtilities.validateConcreteMergeAllowed(abstractA, abstractB));
    }


    // -- findCurrentRatingMappings

    @Test
    public void findCurrentRatingMappingsCollectsDistinctEntityReferences() {
        TaxonomyChangeCommand cmd = mkCommand(7L, Collections.emptyMap());
        EntityReference app1 = mkRef(EntityKind.APPLICATION, 100L);
        EntityReference app2 = mkRef(EntityKind.APPLICATION, 200L);

        IdSelectionOptions expectedOpts = mkOpts(cmd.primaryReference(), HierarchyQueryScope.EXACT);
        when(measurableRatingService.findByMeasurableIdSelector(expectedOpts))
                .thenReturn(asList(mkRating(7L, app1), mkRating(7L, app2), mkRating(7L, app1)));

        Set<EntityReference> result = TaxonomyManagementUtilities.findCurrentRatingMappings(measurableRatingService, cmd);

        assertEquals(asSet(app1, app2), result);
    }


    // -- addToPreview

    @Test
    public void addToPreviewSkipsZeroImpactCounts() {
        ImmutableTaxonomyChangePreview.Builder builder = ImmutableTaxonomyChangePreview.builder()
                .command(mkCommand(1L, Collections.emptyMap()));

        ImmutableTaxonomyChangePreview.Builder result = TaxonomyManagementUtilities.addToPreview(
                builder,
                0,
                Severity.WARNING,
                "should be skipped");

        assertSame(builder, result);
        assertTrue(result.build().impacts().isEmpty());
    }


    @Test
    public void addToPreviewAddsImpactWhenCountIsPositive() {
        ImmutableTaxonomyChangePreview.Builder builder = ImmutableTaxonomyChangePreview.builder()
                .command(mkCommand(1L, Collections.emptyMap()));

        TaxonomyChangePreview preview = TaxonomyManagementUtilities.addToPreview(
                builder,
                3,
                Severity.ERROR,
                "three things")
                .build();

        assertEquals(1, preview.impacts().size());
        assertEquals(3, preview.impacts().get(0).impactCount());
        assertEquals(Severity.ERROR, preview.impacts().get(0).severity());
        assertEquals("three things", preview.impacts().get(0).description());
    }


    // -- param accessors

    @Test
    public void paramAccessorsReadFromCommandParams() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "new name");
        params.put("description", "new desc");
        params.put("externalId", "EXT-1");
        params.put("concrete", "false");
        TaxonomyChangeCommand cmd = mkCommand(1L, params);

        assertEquals("new name", TaxonomyManagementUtilities.getNameParam(cmd));
        assertEquals("new desc", TaxonomyManagementUtilities.getDescriptionParam(cmd));
        assertEquals("EXT-1", TaxonomyManagementUtilities.getExternalIdParam(cmd));
        assertFalse(TaxonomyManagementUtilities.getConcreteParam(cmd, true));
    }


    @Test
    public void paramAccessorsFallBackWhenMissing() {
        TaxonomyChangeCommand cmd = mkCommand(1L, Collections.emptyMap());

        assertNull(TaxonomyManagementUtilities.getNameParam(cmd));
        assertNull(TaxonomyManagementUtilities.getDescriptionParam(cmd));
        assertNull(TaxonomyManagementUtilities.getExternalIdParam(cmd));
        assertTrue(TaxonomyManagementUtilities.getConcreteParam(cmd, true));
        assertFalse(TaxonomyManagementUtilities.getConcreteParam(cmd, false));
    }


    // -- verifyUserHasPermissions

    @Test
    public void userWithoutTaxonomyEditorRoleIsRejected() {
        when(userRoleService.hasRole("bob", SystemRole.TAXONOMY_EDITOR.name())).thenReturn(false);

        assertThrows(
                NotAuthorizedException.class,
                () -> TaxonomyManagementUtilities.verifyUserHasPermissions(userRoleService, "bob"));
    }


    @Test
    public void userWithTaxonomyEditorRoleIsAccepted() {
        when(userRoleService.hasRole("bob", SystemRole.TAXONOMY_EDITOR.name())).thenReturn(true);

        assertDoesNotThrow(() -> TaxonomyManagementUtilities.verifyUserHasPermissions(userRoleService, "bob"));
    }


    @Test
    public void nonEditableCategoryIsRejected() {
        when(userRoleService.hasRole("bob", SystemRole.TAXONOMY_EDITOR.name())).thenReturn(true);
        when(measurableCategoryService.getById(CATEGORY_ID)).thenReturn(mkCategory(false));

        NotAuthorizedException ex = assertThrows(
                NotAuthorizedException.class,
                () -> TaxonomyManagementUtilities.verifyUserHasPermissions(
                        measurableCategoryService,
                        userRoleService,
                        "bob",
                        CATEGORY_REF));

        assertTrue(ex.getMessage().contains("not editable"));
    }


    @Test
    public void editableCategoryIsAccepted() {
        when(userRoleService.hasRole("bob", SystemRole.TAXONOMY_EDITOR.name())).thenReturn(true);
        when(measurableCategoryService.getById(CATEGORY_ID)).thenReturn(mkCategory(true));

        assertDoesNotThrow(() -> TaxonomyManagementUtilities.verifyUserHasPermissions(
                measurableCategoryService,
                userRoleService,
                "bob",
                CATEGORY_REF));
    }


    @Test
    public void nonCategoryDomainSkipsCategoryLookup() {
        when(userRoleService.hasRole("bob", SystemRole.TAXONOMY_EDITOR.name())).thenReturn(true);

        assertDoesNotThrow(() -> TaxonomyManagementUtilities.verifyUserHasPermissions(
                measurableCategoryService,
                userRoleService,
                "bob",
                mkRef(EntityKind.DATA_TYPE, 1L)));

        verify(measurableCategoryService, never()).getById(eq(1L));
    }


    @Test
    public void roleCheckHappensBeforeCategoryLookup() {
        when(userRoleService.hasRole(anyString(), eq(SystemRole.TAXONOMY_EDITOR.name()))).thenReturn(false);

        assertThrows(
                NotAuthorizedException.class,
                () -> TaxonomyManagementUtilities.verifyUserHasPermissions(
                        measurableCategoryService,
                        userRoleService,
                        "bob",
                        CATEGORY_REF));

        verify(measurableCategoryService, never()).getById(CATEGORY_ID);
    }


    private static MeasurableCategory mkCategory(boolean editable) {
        return ImmutableMeasurableCategory.builder()
                .id(CATEGORY_ID)
                .name("cat")
                .description("")
                .icon("cog")
                .ratingSchemeId(1L)
                .allowPrimaryRatings(false)
                .editable(editable)
                .lastUpdatedBy("test")
                .build();
    }

}
