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

import org.finos.waltz.common.OptionalUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.Cardinality;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_definition.AssessmentVisibility;
import org.finos.waltz.model.bulk_upload.BulkUploadMode;
import org.finos.waltz.model.bulk_upload.ResolutionStatus;
import org.finos.waltz.model.bulk_upload.legal_entity_relationship.*;
import org.finos.waltz.model.legal_entity.LegalEntity;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.service.application.ApplicationService;
import org.finos.waltz.service.assessment_definition.AssessmentDefinitionService;
import org.finos.waltz.service.bulk_upload.BulkUploadLegalEntityRelationshipService;
import org.finos.waltz.service.legal_entity.LegalEntityService;
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
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.finos.waltz.common.CollectionUtilities.*;
import static org.finos.waltz.common.SetUtilities.*;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.*;


public class BulkUploadLegalEntityRelationshipServiceTest extends BaseInMemoryIntegrationTest {

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
    private ApplicationService appSvc;

    @Autowired
    private LegalEntityHelper legalEntityHelper;

    @Autowired
    private LegalEntityService legalEntitySvc;

    @Test
    public void headersCanBeParsed() {
        String name = mkName("headersCanBeParsed");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long defnId = assessmentHelper.createDefinition(schemeId, name + "Definition", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP);
        AssessmentDefinition defn = defnSvc.getById(defnId);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));

        String[] headerRow = {"a", "le", "comment", defn.externalId().get()};
        Set<Tuple2<Integer, ResolvedAssessmentHeader>> assessmentWithColIdx = service.parseAssessmentsFromHeader(tuple(1, headerRow));

        assertEquals(1, assessmentWithColIdx.size(), "Should return a single assessment definition");
        assertTrue(first(assessmentWithColIdx).v2.headerDefinition().isPresent(), "Should be able to identify an assessment definition using externalIds");
        assertEquals(3, first(assessmentWithColIdx).v1, "First assessment definition in the list should be returned with a column index of 3");
        assertEquals(defn, first(assessmentWithColIdx).v2.headerDefinition().get(), "Should identify the correct assessment definition from the externalId");
    }

    @Test
    public void reportsWhereAssessmentCannotBeIdentified() {
        String name = mkName("reportsWhereAssessmentCannotBeIdentified");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long defnId = assessmentHelper.createDefinition(schemeId, name + "Definition", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP);
        AssessmentDefinition defn = defnSvc.getById(defnId);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));

        String[] headerRow = {"a", "le", "comment", "invalid identifier"};
        Set<Tuple2<Integer, ResolvedAssessmentHeader>> assessmentWithColIdx = service.parseAssessmentsFromHeader(tuple(1, headerRow));
        Optional<Tuple2<Integer, ResolvedAssessmentHeader>> maybeDefnA = find(assessmentWithColIdx, d -> d.v1 == 3);

        assertTrue(maybeDefnA.isPresent(), "Header should find data for column idx 3");
        Optional<AssessmentDefinition> assessmentDefinition = maybeDefnA.get().v2.headerDefinition();
        assertFalse(assessmentDefinition.isPresent(), "Assessment should not be found column idx 3");

        Set<AssessmentResolutionErrorCode> errors = SetUtilities.map(maybeDefnA.get().v2.errors(), AssessmentHeaderResolutionError::errorCode);
        assertTrue(errors.contains(AssessmentResolutionErrorCode.HEADER_DEFINITION_NOT_FOUND), "Rating value should be reported as not identifiable");
        assertEquals(ResolutionStatus.ERROR, maybeDefnA.get().v2.status(), "If errors not empty then resolution status should be 'ERROR'");
    }

    @Test
    public void reportsWhenNullValueForHeaderAssessment() {
        String name = mkName("reportsWhenNullValueForHeaderAssessment");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long defnId = assessmentHelper.createDefinition(schemeId, name + "Definition", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP);
        AssessmentDefinition defn = defnSvc.getById(defnId);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));

        String[] headerRow = {"a", "le", "comment", null, defn.externalId().get()};
        Set<Tuple2<Integer, ResolvedAssessmentHeader>> assessmentWithColIdx = service.parseAssessmentsFromHeader(tuple(1, headerRow));
        Optional<Tuple2<Integer, ResolvedAssessmentHeader>> maybeDefnA = find(assessmentWithColIdx, d -> d.v1 == 3);

        assertTrue(maybeDefnA.isPresent(), "Header should find data for column idx 3");
        Optional<AssessmentDefinition> assessmentDefinition = maybeDefnA.get().v2.headerDefinition();
        assertFalse(assessmentDefinition.isPresent(), "Assessment should not be found column idx 3");

        Set<AssessmentResolutionErrorCode> errors = SetUtilities.map(maybeDefnA.get().v2.errors(), AssessmentHeaderResolutionError::errorCode);
        assertTrue(errors.contains(AssessmentResolutionErrorCode.NO_VALUE_PROVIDED), "Rating value should be reported as not identifiable");
        assertEquals(ResolutionStatus.ERROR, maybeDefnA.get().v2.status(), "If errors not empty then resolution status should be 'ERROR'");
    }

    @Test
    public void reportsWhenEmptyValueForHeaderAssessment() {
        String name = mkName("reportsWhenNullValueForHeaderAssessment");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long defnId = assessmentHelper.createDefinition(schemeId, name + "Definition", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP);
        AssessmentDefinition defn = defnSvc.getById(defnId);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));

        String[] headerRow = {"a", "le", "comment", "", defn.externalId().get()};
        Set<Tuple2<Integer, ResolvedAssessmentHeader>> assessmentWithColIdx = service.parseAssessmentsFromHeader(tuple(1, headerRow));
        Optional<Tuple2<Integer, ResolvedAssessmentHeader>> maybeDefnA = find(assessmentWithColIdx, d -> d.v1 == 3);

        assertTrue(maybeDefnA.isPresent(), "Header should find data for column idx 3");
        Optional<AssessmentDefinition> assessmentDefinition = maybeDefnA.get().v2.headerDefinition();
        assertFalse(assessmentDefinition.isPresent(), "Assessment should not be found column idx 3");

        Set<AssessmentResolutionErrorCode> errors = SetUtilities.map(maybeDefnA.get().v2.errors(), AssessmentHeaderResolutionError::errorCode);
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

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));

        String[] headerRow = {"a", "le", "comment", defnC.externalId().get(), defnB.externalId().get(), defnA.externalId().get()};

        Set<Tuple2<Integer, ResolvedAssessmentHeader>> assessmentWithColIdx = service.parseAssessmentsFromHeader(tuple(1, headerRow));

        Optional<Tuple2<Integer, ResolvedAssessmentHeader>> maybeDefnC = find(assessmentWithColIdx, d -> d.v1 == 3);
        Optional<Tuple2<Integer, ResolvedAssessmentHeader>> maybeDefnB = find(assessmentWithColIdx, d -> d.v1 == 4);
        Optional<Tuple2<Integer, ResolvedAssessmentHeader>> maybeDefnA = find(assessmentWithColIdx, d -> d.v1 == 5);

        assertTrue(maybeDefnC.isPresent(), "Header should find data for column idx 3");
        assertTrue(maybeDefnB.isPresent(), "Header should find data for column idx 4");
        assertTrue(maybeDefnA.isPresent(), "Header should find data for column idx 5");

        assertTrue(maybeDefnC.get().v2.headerDefinition().isPresent(), "Assessment should be identifiable for column idx 3");
        assertTrue(maybeDefnB.get().v2.headerDefinition().isPresent(), "Assessment should be identifiable for column idx 4");
        assertTrue(maybeDefnA.get().v2.headerDefinition().isPresent(), "Assessment should be identifiable for column idx 5");

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

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));

        String assessmentHeaderString = format("%s / %s", defnA.externalId().get(), "invalid identifier");

        String[] headerRow = {"a", "le", "comment", assessmentHeaderString};

        Set<Tuple2<Integer, ResolvedAssessmentHeader>> assessmentWithColIdx = service.parseAssessmentsFromHeader(tuple(1, headerRow));

        Optional<Tuple2<Integer, ResolvedAssessmentHeader>> maybeDefnA = find(assessmentWithColIdx, d -> d.v1 == 3);

        assertTrue(maybeDefnA.isPresent(), "Header should find data for column idx 3");
        Optional<AssessmentDefinition> assessmentDefinition = maybeDefnA.get().v2.headerDefinition();

        assertTrue(assessmentDefinition.isPresent(), "Assessment should be identifiable for column idx 3");
        assertEquals(defnA, assessmentDefinition.get(), "Should identify the correct assessment definition from the externalId");

        Set<AssessmentResolutionErrorCode> errors = SetUtilities.map(maybeDefnA.get().v2.errors(), AssessmentHeaderResolutionError::errorCode);
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

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));

        String assessmentHeaderString = format("%s / %s", defn.externalId().get(), "TEST_RATING");

        String[] headerRow = {"a", "le", "comment", assessmentHeaderString};

        Set<Tuple2<Integer, ResolvedAssessmentHeader>> assessmentWithColIdx = service.parseAssessmentsFromHeader(tuple(1, headerRow));
        Optional<Tuple2<Integer, ResolvedAssessmentHeader>> maybeDefnA = find(assessmentWithColIdx, d -> d.v1 == 3);

        assertTrue(maybeDefnA.isPresent(), "Header should find data for column idx 3");
        Optional<AssessmentDefinition> assessmentDefinition = maybeDefnA.get().v2.headerDefinition();
        Optional<RatingSchemeItem> rating = maybeDefnA.get().v2.headerRating();

        assertTrue(assessmentDefinition.isPresent(), "Assessment should be identifiable for column idx 3");
        assertEquals(defn, assessmentDefinition.get(), "Should identify the correct assessment definition from the externalId");

        Set<AssessmentResolutionErrorCode> errors = SetUtilities.map(maybeDefnA.get().v2.errors(), AssessmentHeaderResolutionError::errorCode);
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

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));

        String assessmentHeaderString = format("%s", defnA.externalId().get());

        String[] headerRow = {"a", "le", "comment", assessmentHeaderString};

        Set<Tuple2<Integer, ResolvedAssessmentHeader>> assessmentWithColIdx = service.parseAssessmentsFromHeader(tuple(1, headerRow));

        Optional<Tuple2<Integer, ResolvedAssessmentHeader>> maybeDefnA = find(assessmentWithColIdx, d -> d.v1 == 3);

        assertTrue(maybeDefnA.isPresent(), "Header should find data for column idx 3");
        Optional<AssessmentDefinition> assessmentDefinition = maybeDefnA.get().v2.headerDefinition();

        assertTrue(assessmentDefinition.isPresent(), "Assessment should be identifiable for column idx 3");
        assertFalse(maybeDefnA.get().v2.headerRating().isPresent(), "Rating should not be identifiable for column idx 3");

        Set<AssessmentResolutionErrorCode> errors = SetUtilities.map(maybeDefnA.get().v2.errors(), AssessmentHeaderResolutionError::errorCode);
        assertTrue(isEmpty(errors), "Should be no errors reported for headers where no rating specified in format: 'DEFINITION_IDENTIFIER/RATING_IDENTIFIER'");
    }


    @Test
    public void shouldReportWhenRelationshipsAsExisting() {
        String name = mkName("shouldReportWhenRelationshipsAsExisting");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP);

        AssessmentDefinition defnA = defnSvc.getById(defnIdA);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference a2 = appHelper.createNewApp(mkName("a2"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));
        long leRelKindId = legalEntityHelper.createLegalEntityRelationshipKind(name);

        Application app = appSvc.getById(a.id());
        Application app2 = appSvc.getById(a2.id());
        LegalEntity legalEntity = legalEntitySvc.getById(le.id());
        legalEntityHelper.createLegalEntityRelationship(a, le, leRelKindId);

        String inputString = format("App\tLegal Entity\tComment\n" +
                        "%s\t%s\t%s\n" +
                        "%s\t%s\t%s\n",
                app.externalId().get(),
                legalEntity.externalId(),
                "A test comment string",
                app2.externalId().get(),
                legalEntity.externalId(),
                "A test comment string");

        BulkUploadLegalEntityRelationshipCommand uploadCmd = ImmutableBulkUploadLegalEntityRelationshipCommand.builder()
                .uploadMode(BulkUploadMode.ADD_ONLY)
                .inputString(inputString)
                .legalEntityRelationshipKindId(leRelKindId)
                .build();

        ResolveBulkUploadLegalEntityRelationshipParameters resolvedCommand = service.resolve(uploadCmd);

        assertEquals(2, resolvedCommand.resolvedRows().size(), "Should identify 2 rows of data has been provided");

        Set<ResolvedUploadRow> rowsWithAssessments = SetUtilities.filter(resolvedCommand.resolvedRows(), d -> notEmpty(d.assessmentRatings()));
        assertTrue(isEmpty(rowsWithAssessments), "No rows should have assessments associated to them");

        Set<ResolvedUploadRow> erroredRows = SetUtilities.filter(resolvedCommand.resolvedRows(), d -> d.legalEntityRelationship().status().equals(ResolutionStatus.ERROR));

        Optional<ResolvedUploadRow> firstRow = find(resolvedCommand.resolvedRows(), d -> d.rowNumber() == 2L);
        Optional<ResolvedUploadRow> secondRow = find(resolvedCommand.resolvedRows(), d -> d.rowNumber() == 3L);

        assertTrue(firstRow.isPresent(), "Should find row at index 1");
        assertTrue(secondRow.isPresent(), "Should find row at index 2");

        assertEquals(ResolutionStatus.EXISTING, firstRow.get().legalEntityRelationship().status(), "Should correctly identify where a resolved row has a relationship that already exists");
        assertEquals(ResolutionStatus.NEW, secondRow.get().legalEntityRelationship().status(), "Should correctly identify where a resolved row has a relationship that already exists");
    }

    @Test
    public void shouldReportWhenRelationshipHasErrors() {
        String name = mkName("shouldReportWhenRelationshipHasErrors");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP);

        AssessmentDefinition defnA = defnSvc.getById(defnIdA);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference a2 = appHelper.createNewApp(mkName("a2"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));
        long leRelKindId = legalEntityHelper.createLegalEntityRelationshipKind(name);

        Application app2 = appSvc.getById(a2.id());
        LegalEntity legalEntity = legalEntitySvc.getById(le.id());

        String inputString = format("App\tLegal Entity\tComment\n" +
                        "%s\t%s\t%s\n" +
                        "%s\t%s\t%s\n",
                "Not an app identifier",
                legalEntity.externalId(),
                "A test comment string",
                app2.externalId().get(),
                legalEntity.externalId(),
                "A test comment string");

        BulkUploadLegalEntityRelationshipCommand uploadCmd = ImmutableBulkUploadLegalEntityRelationshipCommand.builder()
                .uploadMode(BulkUploadMode.ADD_ONLY)
                .inputString(inputString)
                .legalEntityRelationshipKindId(leRelKindId)
                .build();

        ResolveBulkUploadLegalEntityRelationshipParameters resolvedCommand = service.resolve(uploadCmd);

        assertEquals(2, resolvedCommand.resolvedRows().size(), "Should identify 2 rows of data has been provided");

        Set<ResolvedUploadRow> rowsWithAssessments = SetUtilities.filter(resolvedCommand.resolvedRows(), d -> notEmpty(d.assessmentRatings()));
        assertTrue(isEmpty(rowsWithAssessments), "No rows should have assessments associated to them");

        Set<ResolvedUploadRow> erroredRows = SetUtilities.filter(resolvedCommand.resolvedRows(), d -> d.legalEntityRelationship().status().equals(ResolutionStatus.ERROR));
        assertTrue(notEmpty(erroredRows), "One of the roes should have a status of errored");

        Optional<ResolvedUploadRow> firstRow = find(resolvedCommand.resolvedRows(), d -> d.rowNumber() == 2L);
        Optional<ResolvedUploadRow> secondRow = find(resolvedCommand.resolvedRows(), d -> d.rowNumber() == 3L);

        assertTrue(firstRow.isPresent(), "Should find row at index 1");
        assertTrue(secondRow.isPresent(), "Should find row at index 2");

        assertEquals(ResolutionStatus.ERROR, firstRow.get().legalEntityRelationship().status(), "Should correctly report when the relationship details for a row cannot be resolved");
        assertEquals(ResolutionStatus.NEW, secondRow.get().legalEntityRelationship().status(), "Should correctly identify where a resolved row has a relationship that already exists");
    }

    @Test
    public void canResolveAssessmentsForLegalEntityRelationship() {
        String name = mkName("canResolveAssessmentsForLegalEntityRelationship");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP);
        long greenRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Green", 10, "green", "G", "TEST_GREEN");
        long redRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Red", 20, "red", "R", "TEST_RED");

        AssessmentDefinition defnA = defnSvc.getById(defnIdA);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));
        long leRelKindId = legalEntityHelper.createLegalEntityRelationshipKind(name);

        Application app = appSvc.getById(a.id());
        LegalEntity legalEntity = legalEntitySvc.getById(le.id());

        String inputString = format("App\tLegal Entity\tComment\t%s\n" +
                        "%s\t%s\t%s\t%s\n",
                defnA.externalId().get(),
                app.externalId().get(),
                legalEntity.externalId(),
                "A test comment string",
                "TEST_GREEN");

        BulkUploadLegalEntityRelationshipCommand uploadCmd = ImmutableBulkUploadLegalEntityRelationshipCommand.builder()
                .uploadMode(BulkUploadMode.ADD_ONLY)
                .inputString(inputString)
                .legalEntityRelationshipKindId(leRelKindId)
                .build();

        ResolveBulkUploadLegalEntityRelationshipParameters resolvedCommand = service.resolve(uploadCmd);

        assertEquals(1, resolvedCommand.resolvedRows().size(), "Should identify 1 row of data has been provided");

        Set<ResolvedUploadRow> rowsWithAssessments = SetUtilities.filter(resolvedCommand.resolvedRows(), d -> notEmpty(d.assessmentRatings()));
        assertTrue(notEmpty(rowsWithAssessments), "Data rows should have assessment associated");

        Optional<ResolvedUploadRow> dataRow = find(resolvedCommand.resolvedRows(), d -> d.rowNumber() == 2L);

        assertTrue(dataRow.isPresent(), "Should find row at index 1");

        Set<ResolvedAssessmentRating> resolvedAssessmentRatings = dataRow.get().assessmentRatings();

        assertEquals(1, resolvedAssessmentRatings.size(), "Should correctly resolve 1 assessment definition column");

        Optional<ResolvedAssessmentRating> firstAssessment = find(resolvedAssessmentRatings, d -> d.columnIndex() == 3); // Zero offset and 3 relationship cols
        assertTrue(firstAssessment.isPresent(), "Should find assessment at index 0");
        assertTrue(isEmpty(firstAssessment.get().assessmentHeader().errors()), "Should resolve assessment header with no errors");

        assertEquals(1, firstAssessment.get().resolvedRatings().size(), "Should only find one assessment rating for this row and definition");

        ResolvedRatingValue resolvedRatingValue = first(firstAssessment.get().resolvedRatings());
        assertTrue(resolvedRatingValue.rating().isPresent(), "Should be able to identify the correct rating scheme item");
        assertEquals("TEST_GREEN", resolvedRatingValue.rating().get().externalId().get(), "Should be able to identify the correct rating scheme item");
    }

    @Test
    public void canResolveMultipleAssessmentsForLegalEntityRelationship() {
        String name = mkName("canResolveMultipleAssessmentsForLegalEntityRelationship");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP, Cardinality.ZERO_MANY);
        long greenRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Green", 10, "green", "G", "TEST_GREEN");
        long redRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Red", 20, "red", "R", "TEST_RED");

        AssessmentDefinition defnA = defnSvc.getById(defnIdA);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));
        long leRelKindId = legalEntityHelper.createLegalEntityRelationshipKind(name);

        Application app = appSvc.getById(a.id());
        LegalEntity legalEntity = legalEntitySvc.getById(le.id());

        String inputString = format("App\tLegal Entity\tComment\t%s\n" +
                        "%s\t%s\t%s\t%s\n",
                defnA.externalId().get(),
                app.externalId().get(),
                legalEntity.externalId(),
                "A test comment string",
                "TEST_GREEN;TEST_RED");

        BulkUploadLegalEntityRelationshipCommand uploadCmd = ImmutableBulkUploadLegalEntityRelationshipCommand.builder()
                .uploadMode(BulkUploadMode.ADD_ONLY)
                .inputString(inputString)
                .legalEntityRelationshipKindId(leRelKindId)
                .build();

        ResolveBulkUploadLegalEntityRelationshipParameters resolvedCommand = service.resolve(uploadCmd);

        assertEquals(1, resolvedCommand.resolvedRows().size(), "Should identify 1 row of data has been provided");

        Set<ResolvedUploadRow> rowsWithAssessments = SetUtilities.filter(resolvedCommand.resolvedRows(), d -> notEmpty(d.assessmentRatings()));
        assertTrue(notEmpty(rowsWithAssessments), "Data rows should have assessment associated");

        Optional<ResolvedUploadRow> dataRow = find(resolvedCommand.resolvedRows(), d -> d.rowNumber() == 2L);

        assertTrue(dataRow.isPresent(), "Should find row at index 1");

        Set<ResolvedAssessmentRating> resolvedAssessmentRatings = dataRow.get().assessmentRatings();

        assertEquals(1, resolvedAssessmentRatings.size(), "Should correctly resolve 1 assessment definition column");

        Optional<ResolvedAssessmentRating> firstAssessment = find(resolvedAssessmentRatings, d -> d.columnIndex() == 3); // Zero offset and 3 relationship cols
        assertTrue(firstAssessment.isPresent(), "Should find assessment at index 0");
        assertTrue(isEmpty(firstAssessment.get().assessmentHeader().errors()), "Should resolve assessment header with no errors");

        assertEquals(2, firstAssessment.get().resolvedRatings().size(), "Should only find 2 assessment ratings for this row and definition");

        Set<ResolvedRatingValue> erroredRatings = SetUtilities.filter(firstAssessment.get().resolvedRatings(), d -> notEmpty(d.errors()));
        assertTrue(isEmpty(erroredRatings), "Should resolve all rating values");

        Set<RatingSchemeItem> resolvedRatingValues = firstAssessment.get().resolvedRatings()
                .stream()
                .filter(d -> d.rating().isPresent())
                .map(d -> d.rating().get())
                .collect(Collectors.toSet());

        assertEquals(asSet("TEST_GREEN", "TEST_RED"), SetUtilities.map(resolvedRatingValues, d -> d.externalId().get()), "Should be able to identify the correct rating scheme items");
    }

    @Test
    public void canResolveFromSpecificRatingColumn() {
        String name = mkName("canResolveFromSpecificRatingColumn");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP, Cardinality.ZERO_MANY);
        long greenRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Green", 10, "green", "G", "TEST_GREEN");
        long redRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Red", 20, "red", "R", "TEST_RED");

        AssessmentDefinition defnA = defnSvc.getById(defnIdA);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));
        long leRelKindId = legalEntityHelper.createLegalEntityRelationshipKind(name);

        Application app = appSvc.getById(a.id());
        LegalEntity legalEntity = legalEntitySvc.getById(le.id());

        String inputString = format("App\tLegal Entity\tComment\t%s\n" +
                        "%s\t%s\t%s\t%s\n",
                format("%s / %s", defnA.externalId().get(), "TEST_GREEN"),
                app.externalId().get(),
                legalEntity.externalId(),
                "A test comment string",
                "y");

        BulkUploadLegalEntityRelationshipCommand uploadCmd = ImmutableBulkUploadLegalEntityRelationshipCommand.builder()
                .uploadMode(BulkUploadMode.ADD_ONLY)
                .inputString(inputString)
                .legalEntityRelationshipKindId(leRelKindId)
                .build();

        ResolveBulkUploadLegalEntityRelationshipParameters resolvedCommand = service.resolve(uploadCmd);

        assertEquals(1, resolvedCommand.resolvedRows().size(), "Should identify 1 row of data has been provided");

        Set<ResolvedUploadRow> rowsWithAssessments = SetUtilities.filter(resolvedCommand.resolvedRows(), d -> notEmpty(d.assessmentRatings()));
        assertTrue(notEmpty(rowsWithAssessments), "Data rows should have assessment associated");

        Optional<ResolvedUploadRow> dataRow = find(resolvedCommand.resolvedRows(), d -> d.rowNumber() == 2L);

        assertTrue(dataRow.isPresent(), "Should find row at index 1");

        Set<ResolvedAssessmentRating> resolvedAssessmentRatings = dataRow.get().assessmentRatings();

        assertEquals(1, resolvedAssessmentRatings.size(), "Should correctly resolve 1 assessment definition column");

        Optional<ResolvedAssessmentRating> firstAssessment = find(resolvedAssessmentRatings, d -> d.columnIndex() == 3); // Zero offset and 3 relationship cols
        assertTrue(firstAssessment.isPresent(), "Should find assessment at index 0");
        assertTrue(isEmpty(firstAssessment.get().assessmentHeader().errors()), "Should resolve assessment header with no errors");

        Set<ResolvedRatingValue> resolvedRatings = firstAssessment.get().resolvedRatings();
        assertEquals(1, resolvedRatings.size(), "Should only find 1 assessment ratings for this row and definition");

        Set<ResolvedRatingValue> erroredRatings = SetUtilities.filter(resolvedRatings, d -> notEmpty(d.errors()));
        assertTrue(isEmpty(erroredRatings), "Should resolve all rating values");

        Set<RatingSchemeItem> resolvedRatingValues = resolvedRatings
                .stream()
                .filter(d -> d.rating().isPresent())
                .map(d -> d.rating().get())
                .collect(Collectors.toSet());

        assertTrue(OptionalUtilities.isEmpty(first(resolvedRatings).comment()), "Should not input 'y' value as comment");
        assertEquals(asSet("TEST_GREEN"), SetUtilities.map(resolvedRatingValues, d -> d.externalId().get()), "Should be able to identify the correct rating scheme item");
    }


    @Test
    public void canParseHeadersWhenSpecificRatingColumn() {
        String name = mkName("canParseHeadersWhenSpecificRatingColumn");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP, Cardinality.ZERO_MANY);
        long greenRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Green", 10, "green", "G", "TEST_GREEN");
        long redRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Red", 20, "red", "R", "TEST_RED");

        AssessmentDefinition defnA = defnSvc.getById(defnIdA);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));
        long leRelKindId = legalEntityHelper.createLegalEntityRelationshipKind(name);

        Application app = appSvc.getById(a.id());
        LegalEntity legalEntity = legalEntitySvc.getById(le.id());

        String assessmentComment = "A comment to add to the assessment rating";

        String inputString = format("App\tLegal Entity\tComment\t%s\n" +
                        "%s\t%s\t%s\t%s\n",
                format("%s / %s", defnA.externalId().get(), "TEST_RED"),
                app.externalId().get(),
                legalEntity.externalId(),
                "A test comment string",
                assessmentComment);

        BulkUploadLegalEntityRelationshipCommand uploadCmd = ImmutableBulkUploadLegalEntityRelationshipCommand.builder()
                .uploadMode(BulkUploadMode.ADD_ONLY)
                .inputString(inputString)
                .legalEntityRelationshipKindId(leRelKindId)
                .build();

        ResolveBulkUploadLegalEntityRelationshipParameters resolvedCommand = service.resolve(uploadCmd);

        assertEquals(1, resolvedCommand.resolvedRows().size(), "Should identify 1 row of data has been provided");

        Set<ResolvedUploadRow> rowsWithAssessments = SetUtilities.filter(resolvedCommand.resolvedRows(), d -> notEmpty(d.assessmentRatings()));
        assertTrue(notEmpty(rowsWithAssessments), "Data rows should have assessment associated");

        Optional<ResolvedUploadRow> dataRow = find(resolvedCommand.resolvedRows(), d -> d.rowNumber() == 2L);

        assertTrue(dataRow.isPresent(), "Should find row at index 1");

        Set<ResolvedAssessmentRating> resolvedAssessmentRatings = dataRow.get().assessmentRatings();

        assertEquals(1, resolvedAssessmentRatings.size(), "Should correctly resolve 1 assessment definition column");

        Optional<ResolvedAssessmentRating> firstAssessment = find(resolvedAssessmentRatings, d -> d.columnIndex() == 3); // Zero offset and 3 relationship cols
        assertTrue(firstAssessment.isPresent(), "Should find assessment at index 0");
        assertTrue(isEmpty(firstAssessment.get().assessmentHeader().errors()), "Should resolve assessment header with no errors");

        Set<ResolvedRatingValue> resolvedRatings = firstAssessment.get().resolvedRatings();
        assertEquals(1, resolvedRatings.size(), "Should only find 1 assessment ratings for this row and definition");

        Set<ResolvedRatingValue> erroredRatings = SetUtilities.filter(resolvedRatings, d -> notEmpty(d.errors()));
        assertTrue(isEmpty(erroredRatings), "Should resolve all rating values");

        Set<RatingSchemeItem> resolvedRatingValues = resolvedRatings
                .stream()
                .filter(d -> d.rating().isPresent())
                .map(d -> d.rating().get())
                .collect(Collectors.toSet());

        assertEquals(assessmentComment, first(resolvedRatings).comment().orElse(""), "Should be able to parse the comment string from a specific rating column");
        assertEquals(asSet("TEST_RED"), SetUtilities.map(resolvedRatingValues, d -> d.externalId().get()), "Should be able to identify the correct rating scheme item");
    }

    @Test
    public void reportsErrorsOnAssessmentHeaderForAllRowsButRelationshipCanStillBeParsed() {
        String name = mkName("reportsErrorsOnAssessmentHeaderForAllRowsButRelationshipCanStillBeParsed");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP, Cardinality.ZERO_MANY);
        long greenRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Green", 10, "green", "G", "TEST_GREEN");
        long redRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Red", 20, "red", "R", "TEST_RED");

        AssessmentDefinition defnA = defnSvc.getById(defnIdA);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));
        long leRelKindId = legalEntityHelper.createLegalEntityRelationshipKind(name);

        Application app = appSvc.getById(a.id());
        LegalEntity legalEntity = legalEntitySvc.getById(le.id());

        String assessmentComment = "A comment to add to the assessment rating";

        String inputString = format("App\tLegal Entity\tComment\t%s\n" +
                        "%s\t%s\t%s\t%s\n",
                format("%s / %s", "UNKNOWN DEFN IDENTIFIER", "TEST_RED"),
                app.externalId().get(),
                legalEntity.externalId(),
                "A test comment string",
                assessmentComment);

        BulkUploadLegalEntityRelationshipCommand uploadCmd = ImmutableBulkUploadLegalEntityRelationshipCommand.builder()
                .uploadMode(BulkUploadMode.ADD_ONLY)
                .inputString(inputString)
                .legalEntityRelationshipKindId(leRelKindId)
                .build();

        ResolveBulkUploadLegalEntityRelationshipParameters resolvedCommand = service.resolve(uploadCmd);

        assertEquals(1, resolvedCommand.resolvedRows().size(), "Should identify 1 row of data has been provided");

        Set<ResolvedUploadRow> rowsWithAssessments = SetUtilities.filter(resolvedCommand.resolvedRows(), d -> notEmpty(d.assessmentRatings()));
        assertTrue(notEmpty(rowsWithAssessments), "Data rows should have assessment associated");

        Optional<ResolvedUploadRow> dataRow = find(resolvedCommand.resolvedRows(), d -> d.rowNumber() == 2L);

        assertTrue(dataRow.isPresent(), "Should find row at index 1");

        Set<ResolvedAssessmentRating> resolvedAssessmentRatings = dataRow.get().assessmentRatings();
        assertEquals(1, resolvedAssessmentRatings.size(), "Should correctly resolve 1 assessment definition column");

        Optional<ResolvedAssessmentRating> firstAssessment = find(resolvedAssessmentRatings, d -> d.columnIndex() == 3); // Zero offset and 3 relationship cols
        assertTrue(firstAssessment.isPresent(), "Should find assessment at index 0");
        assertTrue(notEmpty(firstAssessment.get().assessmentHeader().errors()), "Should resolve assessment header with no errors");

        Set<ResolvedRatingValue> resolvedRatings = firstAssessment.get().resolvedRatings();
        assertEquals(1, resolvedRatings.size(), "Should only find 1 assessment ratings for this row and definition");

        Set<ResolvedRatingValue> erroredRatings = SetUtilities.filter(resolvedRatings, d -> notEmpty(d.errors()));
        assertTrue(notEmpty(erroredRatings), "Should report error as definition could not be identified");

        assertFalse(any(resolvedRatings, d -> d.rating().isPresent()), "Should be unable to resolve a rating where the definition is unknown");
    }


    @Test
    public void reportsDuplicateHeaders() {
        String name = mkName("reportsErrorsOnAssessmentHeaderForAllRowsButRelationshipCanStillBeParsed");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP, Cardinality.ZERO_MANY);
        long greenRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Green", 10, "green", "G", "TEST_GREEN");
        long redRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Red", 20, "red", "R", "TEST_RED");

        AssessmentDefinition defnA = defnSvc.getById(defnIdA);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));
        long leRelKindId = legalEntityHelper.createLegalEntityRelationshipKind(name);

        Application app = appSvc.getById(a.id());
        LegalEntity legalEntity = legalEntitySvc.getById(le.id());

        String assessmentComment = "A comment to add to the assessment rating";

        String inputString = format("App\tLegal Entity\tComment\t%s\t%s\t%s\n" +
                        "%s\t%s\t%s\t%s\t%s\t%s\n",
                format("%s / %s", defnA.externalId().get(), "TEST_GREEN"),
                format("%s / %s", defnA.externalId().get(), "TEST_GREEN"),
                format("%s / %s", defnA.externalId().get(), "TEST_RED"),
                app.externalId().get(),
                legalEntity.externalId(),
                "A test comment string",
                assessmentComment,
                assessmentComment,
                assessmentComment);

        BulkUploadLegalEntityRelationshipCommand uploadCmd = ImmutableBulkUploadLegalEntityRelationshipCommand.builder()
                .uploadMode(BulkUploadMode.ADD_ONLY)
                .inputString(inputString)
                .legalEntityRelationshipKindId(leRelKindId)
                .build();

        ResolveBulkUploadLegalEntityRelationshipParameters resolvedCommand = service.resolve(uploadCmd);

        assertEquals(1, resolvedCommand.resolvedRows().size(), "Should identify 1 row of data has been provided");

        Set<ResolvedUploadRow> rowsWithAssessments = SetUtilities.filter(resolvedCommand.resolvedRows(), d -> notEmpty(d.assessmentRatings()));
        assertTrue(notEmpty(rowsWithAssessments), "Data rows should have assessment associated");

        Optional<ResolvedUploadRow> dataRow = find(resolvedCommand.resolvedRows(), d -> d.rowNumber() == 2L);

        assertTrue(dataRow.isPresent(), "Should find row at index 1");

        Set<ResolvedAssessmentRating> resolvedAssessmentRatings = dataRow.get().assessmentRatings();
        assertEquals(3, resolvedAssessmentRatings.size(), "Should correctly resolve 3 assessment definition columns");

        Optional<ResolvedAssessmentRating> firstAssessment = find(resolvedAssessmentRatings, d -> d.columnIndex() == 3); // Zero offset and 3 relationship cols
        Optional<ResolvedAssessmentRating> secondAssessment = find(resolvedAssessmentRatings, d -> d.columnIndex() == 4); // Zero offset and 4 relationship cols
        Optional<ResolvedAssessmentRating> thirdAssessment = find(resolvedAssessmentRatings, d -> d.columnIndex() == 5); // Zero offset and 5 relationship cols

        assertTrue(firstAssessment.isPresent(), "Should find assessment at index 3");
        assertTrue(secondAssessment.isPresent(), "Should find assessment at index 4");
        assertTrue(thirdAssessment.isPresent(), "Should find assessment at index 5");

        assertTrue(notEmpty(firstAssessment.get().assessmentHeader().errors()), "Should resolve assessment header with errors if duplicate");
        assertTrue(notEmpty(secondAssessment.get().assessmentHeader().errors()), "Should resolve assessment header with errors if duplicate");
        assertTrue(isEmpty(thirdAssessment.get().assessmentHeader().errors()), "Should resolve assessment header with no errors if not duplicate");

        Set<AssessmentResolutionErrorCode> firstAssessmentErrors = SetUtilities.map(firstAssessment.get().assessmentHeader().errors(), AssessmentHeaderResolutionError::errorCode);
        Set<AssessmentResolutionErrorCode> secondAssessmentErrors = SetUtilities.map(secondAssessment.get().assessmentHeader().errors(), AssessmentHeaderResolutionError::errorCode);
        assertEquals(asSet(AssessmentResolutionErrorCode.DUPLICATE_COLUMN_HEADER), firstAssessmentErrors, "Should report duplicate column error for assessments columns idx 4 and 5");
        assertEquals(asSet(AssessmentResolutionErrorCode.DUPLICATE_COLUMN_HEADER), secondAssessmentErrors, "Should report duplicate column error for assessments columns idx 4 and 5");
    }


    @Test
    public void reportsWhereSingleValuedAssessmentAllowedOnlyAndDuplicateInRow() {
        String name = mkName("reportsWhereSingleValuedAssessmentAllowedOnlyAndDuplicateInRow");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP, Cardinality.ZERO_ONE);
        long greenRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Green", 10, "green", "G", "TEST_GREEN");
        long redRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Red", 20, "red", "R", "TEST_RED");

        AssessmentDefinition defnA = defnSvc.getById(defnIdA);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));
        long leRelKindId = legalEntityHelper.createLegalEntityRelationshipKind(name);

        Application app = appSvc.getById(a.id());
        LegalEntity legalEntity = legalEntitySvc.getById(le.id());

        String assessmentComment = "TEST_GREEN;TEST_RED";

        String inputString = format("App\tLegal Entity\tComment\t%s\n" +
                        "%s\t%s\t%s\t%s\n",
                format("%s", defnA.externalId().get()),
                app.externalId().get(),
                legalEntity.externalId(),
                "A test comment string",
                assessmentComment);

        BulkUploadLegalEntityRelationshipCommand uploadCmd = ImmutableBulkUploadLegalEntityRelationshipCommand.builder()
                .uploadMode(BulkUploadMode.ADD_ONLY)
                .inputString(inputString)
                .legalEntityRelationshipKindId(leRelKindId)
                .build();

        ResolveBulkUploadLegalEntityRelationshipParameters resolvedCommand = service.resolve(uploadCmd);

        assertEquals(1, resolvedCommand.resolvedRows().size(), "Should identify 1 row of data has been provided");

        Set<ResolvedUploadRow> rowsWithAssessments = SetUtilities.filter(resolvedCommand.resolvedRows(), d -> notEmpty(d.assessmentRatings()));
        assertTrue(notEmpty(rowsWithAssessments), "Data rows should have assessment associated");

        Optional<ResolvedUploadRow> dataRow = find(resolvedCommand.resolvedRows(), d -> d.rowNumber() == 2L);
        assertTrue(dataRow.isPresent(), "Should find row at index 1");

        Set<ResolvedAssessmentRating> resolvedAssessmentRatings = dataRow.get().assessmentRatings();
        assertEquals(1, resolvedAssessmentRatings.size(), "Should correctly resolve 1 assessment definition columns");

        Optional<ResolvedAssessmentRating> firstAssessment = find(resolvedAssessmentRatings, d -> d.columnIndex() == 3);

        assertTrue(firstAssessment.isPresent(), "Should find assessment at index 3");

        assertTrue(all(
                        firstAssessment.get().resolvedRatings(),
                        d -> SetUtilities.map(d.errors(), RatingResolutionError::errorCode).contains(RatingResolutionErrorCode.MULTIPLE_RATINGS_DISALLOWED)),
                "Single valued assessments can only be provided one rating value, reports an error otherwise");
    }

    @Test
    public void reportsWhereSingleValuedAssessmentAllowedOnlyAndDuplicateDueToColumnHeaders() {
        String name = mkName("reportsWhereSingleValuedAssessmentAllowedOnlyAndDuplicateDueToColumnHeaders");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP, Cardinality.ZERO_ONE);
        long greenRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Green", 10, "green", "G", "TEST_GREEN");
        long redRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Red", 20, "red", "R", "TEST_RED");

        AssessmentDefinition defnA = defnSvc.getById(defnIdA);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));
        long leRelKindId = legalEntityHelper.createLegalEntityRelationshipKind(name);

        Application app = appSvc.getById(a.id());
        LegalEntity legalEntity = legalEntitySvc.getById(le.id());

        String assessmentComment = "y";

        String inputString = format("App\tLegal Entity\tComment\t%s\t%s\n" +
                        "%s\t%s\t%s\t%s\t%s\n",
                format("%s / %s", defnA.externalId().get(), "TEST_GREEN"),
                format("%s / %s", defnA.externalId().get(), "TEST_RED"),
                app.externalId().get(),
                legalEntity.externalId(),
                "A test comment string",
                assessmentComment,
                assessmentComment);

        BulkUploadLegalEntityRelationshipCommand uploadCmd = ImmutableBulkUploadLegalEntityRelationshipCommand.builder()
                .uploadMode(BulkUploadMode.ADD_ONLY)
                .inputString(inputString)
                .legalEntityRelationshipKindId(leRelKindId)
                .build();

        ResolveBulkUploadLegalEntityRelationshipParameters resolvedCommand = service.resolve(uploadCmd);

        assertEquals(1, resolvedCommand.resolvedRows().size(), "Should identify 1 row of data has been provided");

        Set<ResolvedUploadRow> rowsWithAssessments = SetUtilities.filter(resolvedCommand.resolvedRows(), d -> notEmpty(d.assessmentRatings()));
        assertTrue(notEmpty(rowsWithAssessments), "Data rows should have assessment associated");

        Optional<ResolvedUploadRow> dataRow = find(resolvedCommand.resolvedRows(), d -> d.rowNumber() == 2L);
        assertTrue(dataRow.isPresent(), "Should find row at index 1");

        Set<ResolvedAssessmentRating> resolvedAssessmentRatings = dataRow.get().assessmentRatings();
        assertEquals(2, resolvedAssessmentRatings.size(), "Should correctly resolve 2 assessment definition columns");

        Optional<ResolvedAssessmentRating> firstAssessment = find(resolvedAssessmentRatings, d -> d.columnIndex() == 3);
        Optional<ResolvedAssessmentRating> secondAssessment = find(resolvedAssessmentRatings, d -> d.columnIndex() == 4);

        assertTrue(firstAssessment.isPresent(), "Should find assessment at index 3");
        assertTrue(secondAssessment.isPresent(), "Should find assessment at index 3");

        assertTrue(all(
                        firstAssessment.get().resolvedRatings(),
                        d -> SetUtilities.map(d.errors(), RatingResolutionError::errorCode).contains(RatingResolutionErrorCode.MULTIPLE_RATINGS_DISALLOWED)),
                "Single valued assessments can only be provided one rating value, reports an error otherwise");

        assertTrue(all(
                        secondAssessment.get().resolvedRatings(),
                        d -> SetUtilities.map(d.errors(), RatingResolutionError::errorCode).contains(RatingResolutionErrorCode.MULTIPLE_RATINGS_DISALLOWED)),
                "Single valued assessments can only be provided one rating value, reports an error otherwise");
    }

}