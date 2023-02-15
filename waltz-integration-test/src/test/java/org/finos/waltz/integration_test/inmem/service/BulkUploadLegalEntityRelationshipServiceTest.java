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

import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_definition.AssessmentVisibility;
import org.finos.waltz.model.bulk_upload.ResolutionStatus;
import org.finos.waltz.model.bulk_upload.legal_entity_relationship.AssessmentHeaderResolutionError;
import org.finos.waltz.model.bulk_upload.legal_entity_relationship.AssessmentResolutionErrorCode;
import org.finos.waltz.model.bulk_upload.legal_entity_relationship.ResolvedAssessmentHeader;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.service.assessment_definition.AssessmentDefinitionService;
import org.finos.waltz.service.bulk_upload.BulkUploadLegalEntityRelationshipService;
import org.finos.waltz.service.rating_scheme.RatingSchemeService;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.finos.waltz.test_common.helpers.AssessmentHelper;
import org.finos.waltz.test_common.helpers.LegalEntityHelper;
import org.finos.waltz.test_common.helpers.RatingSchemeHelper;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.finos.waltz.common.CollectionUtilities.find;
import static org.finos.waltz.common.CollectionUtilities.first;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.*;


public class BulkUploadLegalEntityRelationshipServiceTest extends BaseInMemoryIntegrationTest {

    public static final String TEST_STRING_PREFIX = "1234, ABCD, test_comment";

    @Autowired
    private BulkUploadLegalEntityRelationshipService service;

    @Autowired
    private AssessmentHelper assessmentHelper;

    @Autowired
    private AssessmentDefinitionService defnSvc;

    @Autowired
    private RatingSchemeHelper ratingSchemeHelper;

    @Autowired
    private RatingSchemeService ratingSchemeSvc;

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private LegalEntityHelper legalEntityHelper;

    @Test
    public void headersCanBeParsed() {
        String name = mkName("headersCanBeParsed");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long defnId = assessmentHelper.createDefinition(schemeId, name + "Definition", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP);
        AssessmentDefinition defn = defnSvc.getById(defnId);

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference le = legalEntityHelper.create("le");

        String[] headerRow = {"a", "le", "comment", defn.externalId().get()};
        Set<Tuple2<Integer, ResolvedAssessmentHeader>> assessmentWithColIdx = service.parseAssessmentsFromHeader(tuple(1, headerRow));

        assertEquals(1, assessmentWithColIdx.size(), "Should return a single assessment definition");
        assertTrue(first(assessmentWithColIdx).v2.headerDefinition().isPresent(), "Should be able to identify an assessment definition using externalIds");
        assertEquals(4, first(assessmentWithColIdx).v1, "First assessment definition in the list should be returned with a column index of 4");
        assertEquals(defn, first(assessmentWithColIdx).v2.headerDefinition().get(), "Should identify the correct assessment definition from the externalId");
    }

    @Test
    public void reportsWhereAssessmentCannotBeIdentified() {
        String name = mkName("reportsWhereAssessmentCannotBeIdentified");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long defnId = assessmentHelper.createDefinition(schemeId, name + "Definition", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP);
        AssessmentDefinition defn = defnSvc.getById(defnId);

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference le = legalEntityHelper.create("le");

        String[] headerRow = {"a", "le", "comment", "invalid identifier"};
        Set<Tuple2<Integer, ResolvedAssessmentHeader>> assessmentWithColIdx = service.parseAssessmentsFromHeader(tuple(1, headerRow));
        Optional<Tuple2<Integer, ResolvedAssessmentHeader>> maybeDefnA = find(assessmentWithColIdx, d -> d.v1 == 4);

        assertTrue(maybeDefnA.isPresent(), "Header should find data for column idx 4");
        Optional<AssessmentDefinition> assessmentDefinition = maybeDefnA.get().v2.headerDefinition();
        assertFalse(assessmentDefinition.isPresent(), "Assessment should not be found column idx 4");

        Set<AssessmentResolutionErrorCode> errors = map(maybeDefnA.get().v2.errors(), AssessmentHeaderResolutionError::errorCode);
        assertTrue(errors.contains(AssessmentResolutionErrorCode.HEADER_DEFINITION_NOT_FOUND), "Rating value should be reported as not identifiable");
        assertEquals(ResolutionStatus.ERROR, maybeDefnA.get().v2.status(), "If errors not empty then resolution status should be 'ERROR'");
    }

    @Test
    public void reportsWhenNullValueForHeaderAssessment() {
        String name = mkName("reportsWhenNullValueForHeaderAssessment");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long defnId = assessmentHelper.createDefinition(schemeId, name + "Definition", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP);
        AssessmentDefinition defn = defnSvc.getById(defnId);

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference le = legalEntityHelper.create("le");

        String[] headerRow = {"a", "le", "comment", null, defn.externalId().get()};
        Set<Tuple2<Integer, ResolvedAssessmentHeader>> assessmentWithColIdx = service.parseAssessmentsFromHeader(tuple(1, headerRow));
        Optional<Tuple2<Integer, ResolvedAssessmentHeader>> maybeDefnA = find(assessmentWithColIdx, d -> d.v1 == 4);

        assertTrue(maybeDefnA.isPresent(), "Header should find data for column idx 4");
        Optional<AssessmentDefinition> assessmentDefinition = maybeDefnA.get().v2.headerDefinition();
        assertFalse(assessmentDefinition.isPresent(), "Assessment should not be found column idx 4");

        Set<AssessmentResolutionErrorCode> errors = map(maybeDefnA.get().v2.errors(), AssessmentHeaderResolutionError::errorCode);
        assertTrue(errors.contains(AssessmentResolutionErrorCode.NO_VALUE_PROVIDED), "Rating value should be reported as not identifiable");
        assertEquals(ResolutionStatus.ERROR, maybeDefnA.get().v2.status(), "If errors not empty then resolution status should be 'ERROR'");
    }

    @Test
    public void reportsWhenNEmptyValueForHeaderAssessment() {
        String name = mkName("reportsWhenNullValueForHeaderAssessment");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long defnId = assessmentHelper.createDefinition(schemeId, name + "Definition", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP);
        AssessmentDefinition defn = defnSvc.getById(defnId);

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference le = legalEntityHelper.create("le");

        String[] headerRow = {"a", "le", "comment", "", defn.externalId().get()};
        Set<Tuple2<Integer, ResolvedAssessmentHeader>> assessmentWithColIdx = service.parseAssessmentsFromHeader(tuple(1, headerRow));
        Optional<Tuple2<Integer, ResolvedAssessmentHeader>> maybeDefnA = find(assessmentWithColIdx, d -> d.v1 == 4);

        assertTrue(maybeDefnA.isPresent(), "Header should find data for column idx 4");
        Optional<AssessmentDefinition> assessmentDefinition = maybeDefnA.get().v2.headerDefinition();
        assertFalse(assessmentDefinition.isPresent(), "Assessment should not be found column idx 4");

        Set<AssessmentResolutionErrorCode> errors = map(maybeDefnA.get().v2.errors(), AssessmentHeaderResolutionError::errorCode);
        assertTrue(errors.contains(AssessmentResolutionErrorCode.NO_VALUE_PROVIDED), "Rating value should be reported as not identifiable");
        assertEquals(ResolutionStatus.ERROR, maybeDefnA.get().v2.status(), "If errors not empty then resolution status should be 'ERROR'");
    }

    @Test
    public void multipleAssessmentsCanBeIdentifiedFromHeaderWIthTheCorrectIndex() {
        String name = mkName("multipleAssessmentsCanBeIdentifiedFromHeaderWIthTheCorrectIndex");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP);
        long defnIdB = assessmentHelper.createDefinition(schemeId, name + "DefinitionB", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP);
        long defnIdC = assessmentHelper.createDefinition(schemeId, name + "DefinitionC", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP);

        AssessmentDefinition defnA = defnSvc.getById(defnIdA);
        AssessmentDefinition defnB = defnSvc.getById(defnIdB);
        AssessmentDefinition defnC = defnSvc.getById(defnIdC);

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference le = legalEntityHelper.create("le");

        String[] headerRow = {"a", "le", "comment", defnC.externalId().get(), defnB.externalId().get(), defnA.externalId().get()};

        Set<Tuple2<Integer, ResolvedAssessmentHeader>> assessmentWithColIdx = service.parseAssessmentsFromHeader(tuple(1, headerRow));

        Optional<Tuple2<Integer, ResolvedAssessmentHeader>> maybeDefnC = find(assessmentWithColIdx, d -> d.v1 == 4);
        Optional<Tuple2<Integer, ResolvedAssessmentHeader>> maybeDefnB = find(assessmentWithColIdx, d -> d.v1 == 5);
        Optional<Tuple2<Integer, ResolvedAssessmentHeader>> maybeDefnA = find(assessmentWithColIdx, d -> d.v1 == 6);

        assertTrue(maybeDefnC.isPresent(), "Header should find data for column idx 4");
        assertTrue(maybeDefnB.isPresent(), "Header should find data for column idx 5");
        assertTrue(maybeDefnA.isPresent(), "Header should find data for column idx 6");

        assertTrue(maybeDefnC.get().v2.headerDefinition().isPresent(), "Assessment should be identifiable for column idx 4");
        assertTrue(maybeDefnB.get().v2.headerDefinition().isPresent(), "Assessment should be identifiable for column idx 5");
        assertTrue(maybeDefnA.get().v2.headerDefinition().isPresent(), "Assessment should be identifiable for column idx 6");

        assertEquals(defnA, maybeDefnA.get().v2.headerDefinition().get(), "Should identify the correct assessment definition from the externalId");
        assertEquals(defnB, maybeDefnB.get().v2.headerDefinition().get(), "Should identify the correct assessment definition from the externalId");
        assertEquals(defnC, maybeDefnC.get().v2.headerDefinition().get(), "Should identify the correct assessment definition from the externalId");
    }


    @Test
    public void reportsRatingsWhereUnidentifiable() {
        String name = mkName("reportsRatingsWhereUnidentifiable");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP);

        AssessmentDefinition defnA = defnSvc.getById(defnIdA);

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference le = legalEntityHelper.create("le");

        String assessmentHeaderString = format("%s / %s", defnA.externalId().get(), "invalid identifier");

        String[] headerRow = {"a", "le", "comment", assessmentHeaderString};

        Set<Tuple2<Integer, ResolvedAssessmentHeader>> assessmentWithColIdx = service.parseAssessmentsFromHeader(tuple(1, headerRow));

        Optional<Tuple2<Integer, ResolvedAssessmentHeader>> maybeDefnA = find(assessmentWithColIdx, d -> d.v1 == 4);

        assertTrue(maybeDefnA.isPresent(), "Header should find data for column idx 4");
        Optional<AssessmentDefinition> assessmentDefinition = maybeDefnA.get().v2.headerDefinition();

        assertTrue(assessmentDefinition.isPresent(), "Assessment should be identifiable for column idx 4");
        assertEquals(defnA, assessmentDefinition.get(), "Should identify the correct assessment definition from the externalId");

        Set<AssessmentResolutionErrorCode> errors = map(maybeDefnA.get().v2.errors(), AssessmentHeaderResolutionError::errorCode);
        assertTrue(errors.contains(AssessmentResolutionErrorCode.HEADER_RATING_NOT_FOUND), "Rating value should be reported as not identifiable");
        assertEquals(ResolutionStatus.ERROR, maybeDefnA.get().v2.status(), "If errors not empty then resolution status should be 'ERROR'");
    }


    @Test
    public void canIdentifySpecificRatingColumn() {
        String name = mkName("canIdentifySpecificRatingColumn");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long ratingId = ratingSchemeHelper.saveRatingItem(schemeId, "Test Rating", 10, "green", "G", "TEST_RATING");
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP);

        AssessmentDefinition defn = defnSvc.getById(defnIdA);

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference le = legalEntityHelper.create("le");

        String assessmentHeaderString = format("%s / %s", defn.externalId().get(), "TEST_RATING");

        String[] headerRow = {"a", "le", "comment", assessmentHeaderString};

        Set<Tuple2<Integer, ResolvedAssessmentHeader>> assessmentWithColIdx = service.parseAssessmentsFromHeader(tuple(1, headerRow));
        Optional<Tuple2<Integer, ResolvedAssessmentHeader>> maybeDefnA = find(assessmentWithColIdx, d -> d.v1 == 4);

        assertTrue(maybeDefnA.isPresent(), "Header should find data for column idx 4");
        Optional<AssessmentDefinition> assessmentDefinition = maybeDefnA.get().v2.headerDefinition();
        Optional<RatingSchemeItem> rating = maybeDefnA.get().v2.headerRating();

        assertTrue(assessmentDefinition.isPresent(), "Assessment should be identifiable for column idx 4");
        assertEquals(defn, assessmentDefinition.get(), "Should identify the correct assessment definition from the externalId");

        Set<AssessmentResolutionErrorCode> errors = map(maybeDefnA.get().v2.errors(), AssessmentHeaderResolutionError::errorCode);
        assertTrue(rating.isPresent(), "Rating value should be identified and no errors reported");
        assertTrue(isEmpty(errors), "Rating value should be identified and no errors reported");
        assertEquals(ratingId, rating.get().id().get(), "Should identify the correct header rating value from the externalId");
    }

    @Test
    public void ifNoForwardSlashInHeaderThenShouldNotFindRatingOrReportRatingError() {
        String name = mkName("ifNoForwardSlashInHeaderThenShouldNotFindRatingOrReportRatingError");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP);

        AssessmentDefinition defnA = defnSvc.getById(defnIdA);

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference le = legalEntityHelper.create("le");

        String assessmentHeaderString = format("%s", defnA.externalId().get());

        String[] headerRow = {"a", "le", "comment", assessmentHeaderString};

        Set<Tuple2<Integer, ResolvedAssessmentHeader>> assessmentWithColIdx = service.parseAssessmentsFromHeader(tuple(1, headerRow));

        Optional<Tuple2<Integer, ResolvedAssessmentHeader>> maybeDefnA = find(assessmentWithColIdx, d -> d.v1 == 4);

        assertTrue(maybeDefnA.isPresent(), "Header should find data for column idx 4");
        Optional<AssessmentDefinition> assessmentDefinition = maybeDefnA.get().v2.headerDefinition();

        assertTrue(assessmentDefinition.isPresent(), "Assessment should be identifiable for column idx 4");
        assertFalse(maybeDefnA.get().v2.headerRating().isPresent(), "Rating should not be identifiable for column idx 4");

        Set<AssessmentResolutionErrorCode> errors = map(maybeDefnA.get().v2.errors(), AssessmentHeaderResolutionError::errorCode);
        assertTrue(isEmpty(errors), "Should be no errors reported for headers where no rating specified in format: 'DEFINITION_IDENTIFIER/RATING_IDENTIFIER'");
    }

}