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

import org.finos.waltz.common.MapUtilities;
import org.finos.waltz.common.OptionalUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.Cardinality;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_definition.AssessmentVisibility;
import org.finos.waltz.model.bulk_upload.ResolutionStatus;
import org.finos.waltz.model.bulk_upload.ResolvedAssessmentHeaderStatus;
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
K
import static java.lang.String.format;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.finos.waltz.common.CollectionUtilities.*;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.bulk_upload.legal_entity_relationship.LegalEntityBulkUploadFixedColumns.*;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.junit.jupiter.api.Assertions.*;

@Disabled(".status() no longer found on response, need to rewrite to look at .errors()")
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
    public void assessmentHeadersCanBeParsed() {
        String name = mkName("headersCanBeParsed");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long leRelKindId = legalEntityHelper.createLegalEntityRelationshipKind(name);
        long defnId = assessmentHelper.createDefinition(schemeId, name + "Definition", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP, mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, leRelKindId));
        AssessmentDefinition defn = defnSvc.getById(defnId);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));

        Set<String> headerRow = asSet(ENTITY_IDENTIFIER, LEGAL_ENTITY_IDENTIFIER, COMMENT, defn.externalId().get());
        Set<AssessmentHeaderCell> assessmentHeaders = service.parseAssessmentsFromHeader(mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, leRelKindId), headerRow);

        assertEquals(1, assessmentHeaders.size(), "Should return a single assessment definition");
        assertTrue(first(assessmentHeaders).resolvedAssessmentDefinition().isPresent(), "Should be able to identify an assessment definition using externalIds");
        assertEquals(defn, first(assessmentHeaders).resolvedAssessmentDefinition().get(), "Should identify the correct assessment definition from the externalId");
    }

    @Test
    public void reportsWhereAssessmentCannotBeIdentified() {
        String name = mkName("reportsWhereAssessmentCannotBeIdentified");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long leRelKindId = legalEntityHelper.createLegalEntityRelationshipKind(name);
        long defnId = assessmentHelper.createDefinition(schemeId, name + "Definition", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP, mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, leRelKindId));
        AssessmentDefinition defn = defnSvc.getById(defnId);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));

        Set<String> headerRow = asSet(ENTITY_IDENTIFIER, LEGAL_ENTITY_IDENTIFIER, COMMENT, "invalid identifier");
        Set<AssessmentHeaderCell> assessmentHeaders = service.parseAssessmentsFromHeader(mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, leRelKindId), headerRow);

        AssessmentHeaderCell assessmentHeader = first(assessmentHeaders);
        Optional<AssessmentDefinition> assessmentDefinition = assessmentHeader.resolvedAssessmentDefinition();
        assertFalse(assessmentDefinition.isPresent(), "Assessment should not be found column idx 3");

        assertEquals(ResolvedAssessmentHeaderStatus.HEADER_DEFINITION_NOT_FOUND, assessmentHeader.status(), "If errors not empty then resolution status should be 'HEADER_DEFINITION_NOT_FOUND'");
    }

    @Test
    public void reportsWhenEmptyValueForHeaderAssessment() {
        String name = mkName("reportsWhenEmptyValueForHeaderAssessment");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long leRelKindId = legalEntityHelper.createLegalEntityRelationshipKind(name);
        long defnId = assessmentHelper.createDefinition(schemeId, name + "Definition", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP, mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, leRelKindId));
        AssessmentDefinition defn = defnSvc.getById(defnId);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));

        Set<String> headerRow = asSet(ENTITY_IDENTIFIER, LEGAL_ENTITY_IDENTIFIER, COMMENT, "", defn.externalId().get());
        Set<AssessmentHeaderCell> assessmentHeaders = service.parseAssessmentsFromHeader(mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, leRelKindId), headerRow);

        Optional<AssessmentHeaderCell> headerWithEmptyStringVal = find(assessmentHeaders, d -> d.inputString().equalsIgnoreCase(""));
        assertTrue(headerWithEmptyStringVal.isPresent(), "Header should find resolved assessment detail for column");

        Optional<AssessmentDefinition> assessmentDefinition = headerWithEmptyStringVal.get().resolvedAssessmentDefinition();
        assertFalse(assessmentDefinition.isPresent(), "Assessment should not be identified");

        assertEquals(ResolvedAssessmentHeaderStatus.HEADER_DEFINITION_NOT_FOUND, headerWithEmptyStringVal.get().status(), "If errors not empty then resolution status should be 'HEADER_DEFINITION_NOT_FOUND'");
    }

    @Test
    public void multipleAssessmentsCanBeIdentifiedFromHeaderWIthTheCorrectIndex() {
        String name = mkName("multipleAssessmentsCanBeIdentifiedFromHeaderWIthTheCorrectIndex");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long leRelKindId = legalEntityHelper.createLegalEntityRelationshipKind(name);
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP, mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, leRelKindId));
        long defnIdB = assessmentHelper.createDefinition(schemeId, name + "DefinitionB", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP, mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, leRelKindId));
        long defnIdC = assessmentHelper.createDefinition(schemeId, name + "DefinitionC", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP, mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, leRelKindId));

        AssessmentDefinition defnA = defnSvc.getById(defnIdA);
        AssessmentDefinition defnB = defnSvc.getById(defnIdB);
        AssessmentDefinition defnC = defnSvc.getById(defnIdC);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));

        String defHeader1 = defnC.externalId().get();
        String defHeader2 = defnB.externalId().get();
        String defHeader3 = defnA.externalId().get();

        Set<String> headerRow = asSet(ENTITY_IDENTIFIER, LEGAL_ENTITY_IDENTIFIER, COMMENT, defHeader1, defHeader2, defHeader3);

        Set<AssessmentHeaderCell> assessmentHeaders = service.parseAssessmentsFromHeader(mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, leRelKindId), headerRow);

        Optional<AssessmentHeaderCell> maybeDefnC = find(assessmentHeaders, d -> d.inputString().equalsIgnoreCase(defHeader1));
        Optional<AssessmentHeaderCell> maybeDefnB = find(assessmentHeaders, d -> d.inputString().equalsIgnoreCase(defHeader2));
        Optional<AssessmentHeaderCell> maybeDefnA = find(assessmentHeaders, d -> d.inputString().equalsIgnoreCase(defHeader3));

        assertTrue(maybeDefnC.isPresent(), "Header should find data for column idx 3");
        assertTrue(maybeDefnB.isPresent(), "Header should find data for column idx 4");
        assertTrue(maybeDefnA.isPresent(), "Header should find data for column idx 5");

        assertTrue(maybeDefnC.get().resolvedAssessmentDefinition().isPresent(), "Assessment should be identifiable for column idx 3");
        assertTrue(maybeDefnB.get().resolvedAssessmentDefinition().isPresent(), "Assessment should be identifiable for column idx 4");
        assertTrue(maybeDefnA.get().resolvedAssessmentDefinition().isPresent(), "Assessment should be identifiable for column idx 5");

        assertEquals(defnA, maybeDefnA.get().resolvedAssessmentDefinition().get(), "Should identify the correct assessment definition from the externalId");
        assertEquals(defnB, maybeDefnB.get().resolvedAssessmentDefinition().get(), "Should identify the correct assessment definition from the externalId");
        assertEquals(defnC, maybeDefnC.get().resolvedAssessmentDefinition().get(), "Should identify the correct assessment definition from the externalId");
    }


    @Test
    public void reportsRatingsWhereUnidentifiable() {
        String name = mkName("reportsRatingsWhereUnidentifiable");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long leRelKindId = legalEntityHelper.createLegalEntityRelationshipKind(name);
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP, mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, leRelKindId));

        AssessmentDefinition defnA = defnSvc.getById(defnIdA);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));

        String assessmentHeaderString = format("%s / %s", defnA.externalId().get(), "invalid identifier");

        Set<String> headerRow = asSet(ENTITY_IDENTIFIER, LEGAL_ENTITY_IDENTIFIER, COMMENT, assessmentHeaderString);

        Set<AssessmentHeaderCell> assessmentHeaders = service.parseAssessmentsFromHeader(mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, leRelKindId), headerRow);

        Optional<AssessmentHeaderCell> maybeDefnA = find(assessmentHeaders, d -> d.inputString().equalsIgnoreCase(assessmentHeaderString));

        assertTrue(maybeDefnA.isPresent(), "Header should find data for column");
        Optional<AssessmentDefinition> assessmentDefinition = maybeDefnA.get().resolvedAssessmentDefinition();

        assertTrue(assessmentDefinition.isPresent(), "Assessment should be identifiable");
        assertEquals(defnA, assessmentDefinition.get(), "Should identify the correct assessment definition from the externalId");

        assertEquals(ResolvedAssessmentHeaderStatus.HEADER_RATING_NOT_FOUND, maybeDefnA.get().status(), "If errors not empty then resolution status should be 'HEADER_RATING_NOT_FOUND'");
    }


    @Test
    public void canIdentifySpecificRatingColumn() {
        String name = mkName("canIdentifySpecificRatingColumn");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long ratingId = ratingSchemeHelper.saveRatingItem(schemeId, "Test Rating", 10, "green", "G", "TEST_RATING");
        long leRelKindId = legalEntityHelper.createLegalEntityRelationshipKind(name);
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP, mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, leRelKindId));

        AssessmentDefinition defn = defnSvc.getById(defnIdA);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));

        String assessmentHeaderString = format("%s / %s", defn.externalId().get(), "TEST_RATING");

        Set<String> headerRow = asSet(ENTITY_IDENTIFIER, LEGAL_ENTITY_IDENTIFIER, COMMENT, assessmentHeaderString);

        Set<AssessmentHeaderCell> assessmentHeaders = service.parseAssessmentsFromHeader(mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, leRelKindId), headerRow);
        Optional<AssessmentHeaderCell> maybeDefnA = find(assessmentHeaders, d -> d.inputString().equalsIgnoreCase(assessmentHeaderString));

        assertTrue(maybeDefnA.isPresent(), "Header should find data for column");
        Optional<AssessmentDefinition> assessmentDefinition = maybeDefnA.get().resolvedAssessmentDefinition();
        Optional<RatingSchemeItem> rating = maybeDefnA.get().resolvedRating();

        assertTrue(assessmentDefinition.isPresent(), "Assessment should be identifiable for column");
        assertEquals(defn, assessmentDefinition.get(), "Should identify the correct assessment definition from the externalId");

        assertTrue(rating.isPresent(), "Rating value should be identified and no errors reported");
        assertEquals(ratingId, rating.get().id().get(), "Should identify the correct header rating value from the externalId");
    }

    @Test
    public void ifNoForwardSlashInHeaderThenShouldNotFindRatingOrReportRatingError() {
        String name = mkName("ifNoForwardSlashInHeaderThenShouldNotFindRatingOrReportRatingError");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long leRelKindId = legalEntityHelper.createLegalEntityRelationshipKind(name);
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP, mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, leRelKindId));

        AssessmentDefinition defnA = defnSvc.getById(defnIdA);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));

        String assessmentHeaderString = format("%s", defnA.externalId().get());

        Set<String> headerRow = asSet(ENTITY_IDENTIFIER, LEGAL_ENTITY_IDENTIFIER, COMMENT, assessmentHeaderString);

        Set<AssessmentHeaderCell> assessmentWithColIdx = service.parseAssessmentsFromHeader(mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, leRelKindId), headerRow);

        Optional<AssessmentHeaderCell> maybeDefnA = find(assessmentWithColIdx, d -> d.inputString().equalsIgnoreCase(assessmentHeaderString));

        assertTrue(maybeDefnA.isPresent(), "Header should find data for column");
        Optional<AssessmentDefinition> assessmentDefinition = maybeDefnA.get().resolvedAssessmentDefinition();

        assertTrue(assessmentDefinition.isPresent(), "Assessment should be identifiable for column");
        assertFalse(maybeDefnA.get().resolvedRating().isPresent(), "Rating should not be identifiable for column");

        assertEquals(ResolvedAssessmentHeaderStatus.HEADER_FOUND, maybeDefnA.get().status(), "Should be no errors reported for headers where no rating specified in format: 'DEFINITION_IDENTIFIER/RATING_IDENTIFIER'");
    }


    @Test
    public void shouldReportWhenRelationshipsAsExisting() {
        String name = mkName("shouldReportWhenRelationshipsAsExisting");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long leRelKindId = legalEntityHelper.createLegalEntityRelationshipKind(name);
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP, mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, leRelKindId));

        AssessmentDefinition defnA = defnSvc.getById(defnIdA);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference a2 = appHelper.createNewApp(mkName("a2"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));

        Application app = appSvc.getById(a.id());
        Application app2 = appSvc.getById(a2.id());
        LegalEntity legalEntity = legalEntitySvc.getById(le.id());
        legalEntityHelper.createLegalEntityRelationship(a, le, leRelKindId);

        String baseString = format("%s\t%s\t%s\n", ENTITY_IDENTIFIER, LEGAL_ENTITY_IDENTIFIER, COMMENT);
        String inputString = format(baseString +
                        "%s\t%s\t%s\n" +
                        "%s\t%s\t%s\n",
                app.externalId().get(),
                legalEntity.externalId(),
                "A test comment string",
                app2.externalId().get(),
                legalEntity.externalId(),
                "A test comment string");

        BulkUploadLegalEntityRelationshipCommand uploadCmd = ImmutableBulkUploadLegalEntityRelationshipCommand.builder()
                .inputString(inputString)
                .legalEntityRelationshipKindId(leRelKindId)
                .build();


        ResolveBulkUploadLegalEntityRelationshipResponse resolvedCommand = service.resolve(uploadCmd);

        assertEquals(2, resolvedCommand.rows().size(), "Should identify 2 rows of data has been provided");

        Set<ResolvedUploadRow> rowsWithAssessments = SetUtilities.filter(resolvedCommand.rows(), d -> notEmpty(d.assessmentRatings()));
        assertTrue(isEmpty(rowsWithAssessments), "No rows should have assessments associated to them");

        Set<ResolvedUploadRow> erroredRows = SetUtilities.filter(resolvedCommand.rows(), d -> d.legalEntityRelationship().errors().isEmpty());

        Optional<ResolvedUploadRow> firstRow = find(resolvedCommand.rows(), d -> d.rowNumber() == 2L);
        Optional<ResolvedUploadRow> secondRow = find(resolvedCommand.rows(), d -> d.rowNumber() == 3L);

        assertTrue(firstRow.isPresent(), "Should find first row");
        assertTrue(secondRow.isPresent(), "Should find second row");

        assertEquals(ResolutionStatus.EXISTING, firstRow.get().legalEntityRelationship().errors(), "Should correctly identify where a resolved row has a relationship that already exists");
        assertEquals(ResolutionStatus.NEW, secondRow.get().legalEntityRelationship().errors(), "Should correctly identify where a resolved row has a relationship that already exists");
    }

    @Test
    public void shouldReportWhenRelationshipHasErrors() {
        String name = mkName("shouldReportWhenRelationshipHasErrors");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long leRelKindId = legalEntityHelper.createLegalEntityRelationshipKind(name);
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP, mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, leRelKindId));

        AssessmentDefinition defnA = defnSvc.getById(defnIdA);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference a2 = appHelper.createNewApp(mkName("a2"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));

        Application app2 = appSvc.getById(a2.id());
        LegalEntity legalEntity = legalEntitySvc.getById(le.id());

        String baseString = format("%s\t%s\t%s\n", ENTITY_IDENTIFIER, LEGAL_ENTITY_IDENTIFIER, COMMENT);
        String inputString = format(baseString +
                        "%s\t%s\t%s\n" +
                        "%s\t%s\t%s\n",
                "Not an app identifier",
                legalEntity.externalId(),
                "A test comment string",
                app2.externalId().get(),
                legalEntity.externalId(),
                "A test comment string");

        BulkUploadLegalEntityRelationshipCommand uploadCmd = ImmutableBulkUploadLegalEntityRelationshipCommand.builder()
                .inputString(inputString)
                .legalEntityRelationshipKindId(leRelKindId)
                .build();


        ResolveBulkUploadLegalEntityRelationshipResponse resolvedCommand = service.resolve(uploadCmd);

        assertEquals(2, resolvedCommand.rows().size(), "Should identify 2 rows of data has been provided");

        Set<ResolvedUploadRow> rowsWithAssessments = SetUtilities.filter(resolvedCommand.rows(), d -> notEmpty(d.assessmentRatings()));
        assertTrue(isEmpty(rowsWithAssessments), "No rows should have assessments associated to them");

        Set<ResolvedUploadRow> erroredRows = SetUtilities.filter(resolvedCommand.rows(), d -> d.legalEntityRelationship().errors().equals(ResolutionStatus.ERROR));
        assertTrue(notEmpty(erroredRows), "One of the roes should have a status of errored");

        Optional<ResolvedUploadRow> firstRow = find(resolvedCommand.rows(), d -> d.rowNumber() == 2L);
        Optional<ResolvedUploadRow> secondRow = find(resolvedCommand.rows(), d -> d.rowNumber() == 3L);

        assertTrue(firstRow.isPresent(), "Should find row at index 1");
        assertTrue(secondRow.isPresent(), "Should find row at index 2");

        assertEquals(ResolutionStatus.ERROR, firstRow.get().legalEntityRelationship().errors(), "Should correctly report when the relationship details for a row cannot be resolved");
        assertEquals(ResolutionStatus.NEW, secondRow.get().legalEntityRelationship().errors(), "Should correctly identify where a resolved row has a relationship that already exists");
    }

    @Test
    public void canResolveAssessmentsForLegalEntityRelationship() {
        String name = mkName("canResolveAssessmentsForLegalEntityRelationship");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long leRelKindId = legalEntityHelper.createLegalEntityRelationshipKind(name);
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP, mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, leRelKindId));
        long greenRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Green", 10, "green", "G", "TEST_GREEN");
        long redRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Red", 20, "red", "R", "TEST_RED");

        AssessmentDefinition defnA = defnSvc.getById(defnIdA);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));

        Application app = appSvc.getById(a.id());
        LegalEntity legalEntity = legalEntitySvc.getById(le.id());

        String assessmentHeader = defnA.externalId().get();

        String baseString = format("%s\t%s\t%s\t%s\n", ENTITY_IDENTIFIER, LEGAL_ENTITY_IDENTIFIER, COMMENT, assessmentHeader);
        String inputString = format(baseString +
                        "%s\t%s\t%s\t%s\n",
                app.externalId().get(),
                legalEntity.externalId(),
                "A test comment string",
                "TEST_GREEN");

        BulkUploadLegalEntityRelationshipCommand uploadCmd = ImmutableBulkUploadLegalEntityRelationshipCommand.builder()
                .inputString(inputString)
                .legalEntityRelationshipKindId(leRelKindId)
                .build();


        ResolveBulkUploadLegalEntityRelationshipResponse resolvedCommand = service.resolve(uploadCmd);

        Map<String, Integer> colIdByInputString = MapUtilities.indexBy(resolvedCommand.assessmentHeaders(), AssessmentHeaderCell::inputString, AssessmentHeaderCell::columnId);
        Integer colId = colIdByInputString.get(assessmentHeader);

        assertEquals(1, resolvedCommand.rows().size(), "Should identify 1 row of data has been provided");

        Set<ResolvedUploadRow> rowsWithAssessments = SetUtilities.filter(resolvedCommand.rows(), d -> notEmpty(d.assessmentRatings()));
        assertTrue(notEmpty(rowsWithAssessments), "Data rows should have assessment associated");

        Optional<ResolvedUploadRow> dataRow = find(resolvedCommand.rows(), d -> d.rowNumber() == 2L);

        assertTrue(dataRow.isPresent(), "Should find row at index 1");

        Set<AssessmentCell> resolvedAssessmentRatings = dataRow.get().assessmentRatings();

        assertEquals(1, resolvedAssessmentRatings.size(), "Should correctly resolve 1 assessment definition column");

        Optional<AssessmentCell> firstAssessment = find(resolvedAssessmentRatings, d -> d.columnId() == colId);
        assertTrue(firstAssessment.isPresent(), "Should find assessment");
        assertFalse(firstAssessment.get().statuses().contains(ResolutionStatus.ERROR), "Should resolve assessment header with no errors");

        assertEquals(1, firstAssessment.get().ratings().size(), "Should only find one assessment rating for this row and definition");

        AssessmentCellRating resolvedRatingValue = first(firstAssessment.get().ratings());
        assertTrue(resolvedRatingValue.resolvedRating().isPresent(), "Should be able to identify the correct rating scheme item");
        assertEquals("TEST_GREEN", resolvedRatingValue.resolvedRating().get().externalId().get(), "Should be able to identify the correct rating scheme item");
    }

    @Test
    public void canResolveMultipleAssessmentsForLegalEntityRelationship() {
        String name = mkName("canResolveMultipleAssessmentsForLegalEntityRelationship");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long leRelKindId = legalEntityHelper.createLegalEntityRelationshipKind(name);
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP, Cardinality.ZERO_MANY, Optional.of(mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, leRelKindId)));
        long greenRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Green", 10, "green", "G", "TEST_GREEN");
        long redRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Red", 20, "red", "R", "TEST_RED");

        AssessmentDefinition defnA = defnSvc.getById(defnIdA);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));

        Application app = appSvc.getById(a.id());
        LegalEntity legalEntity = legalEntitySvc.getById(le.id());

        String assessmentHeader = defnA.externalId().get();
        String baseString = format("%s\t%s\t%s\t%s\n", ENTITY_IDENTIFIER, LEGAL_ENTITY_IDENTIFIER, COMMENT, assessmentHeader);
        String inputString = format(baseString +
                        "%s\t%s\t%s\t%s\n",
                app.externalId().get(),
                legalEntity.externalId(),
                "A test comment string",
                "TEST_GREEN;TEST_RED");

        BulkUploadLegalEntityRelationshipCommand uploadCmd = ImmutableBulkUploadLegalEntityRelationshipCommand.builder()
                .inputString(inputString)
                .legalEntityRelationshipKindId(leRelKindId)
                .build();


        ResolveBulkUploadLegalEntityRelationshipResponse resolvedCommand = service.resolve(uploadCmd);

        Map<String, Integer> colIdByInputString = MapUtilities.indexBy(resolvedCommand.assessmentHeaders(), AssessmentHeaderCell::inputString, AssessmentHeaderCell::columnId);
        Integer colId = colIdByInputString.get(assessmentHeader);

        assertEquals(1, resolvedCommand.rows().size(), "Should identify 1 row of data has been provided");

        Set<ResolvedUploadRow> rowsWithAssessments = SetUtilities.filter(resolvedCommand.rows(), d -> notEmpty(d.assessmentRatings()));
        assertTrue(notEmpty(rowsWithAssessments), "Data rows should have assessment associated");

        Optional<ResolvedUploadRow> dataRow = find(resolvedCommand.rows(), d -> d.rowNumber() == 2L);

        assertTrue(dataRow.isPresent(), "Should find row at index 1");

        Set<AssessmentCell> resolvedAssessmentRatings = dataRow.get().assessmentRatings();

        assertEquals(1, resolvedAssessmentRatings.size(), "Should correctly resolve 1 assessment definition column");

        Optional<AssessmentCell> firstAssessment = find(resolvedAssessmentRatings, d -> d.columnId() == colId);
        assertTrue(firstAssessment.isPresent(), "Should find assessment");
        assertFalse(firstAssessment.get().statuses().contains(ResolutionStatus.ERROR), "Should resolve assessment header with no errors");

        assertEquals(2, firstAssessment.get().ratings().size(), "Should only find 2 assessment ratings for this row and definition");

        Set<AssessmentCellRating> erroredRatings = SetUtilities.filter(firstAssessment.get().ratings(), d -> notEmpty(d.errors()));
        assertTrue(isEmpty(erroredRatings), "Should resolve all rating values");

        Set<RatingSchemeItem> resolvedRatingValues = firstAssessment.get().ratings()
                .stream()
                .filter(d -> d.resolvedRating().isPresent())
                .map(d -> d.resolvedRating().get())
                .collect(Collectors.toSet());

        assertEquals(asSet("TEST_GREEN", "TEST_RED"), SetUtilities.map(resolvedRatingValues, d -> d.externalId().get()), "Should be able to identify the correct rating scheme items");
    }

    @Test
    public void canResolveFromSpecificRatingColumn() {
        String name = mkName("canResolveFromSpecificRatingColumn");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long leRelKindId = legalEntityHelper.createLegalEntityRelationshipKind(name);
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP, mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, leRelKindId));
        long greenRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Green", 10, "green", "G", "TEST_GREEN");
        long redRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Red", 20, "red", "R", "TEST_RED");

        AssessmentDefinition defnA = defnSvc.getById(defnIdA);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));

        Application app = appSvc.getById(a.id());
        LegalEntity legalEntity = legalEntitySvc.getById(le.id());

        String assessmentHeader = format("%s / %s", defnA.externalId().get(), "TEST_GREEN");
        String baseString = format("%s\t%s\t%s\t%s\n", ENTITY_IDENTIFIER, LEGAL_ENTITY_IDENTIFIER, COMMENT, assessmentHeader);
        String inputString = format(baseString +
                        "%s\t%s\t%s\t%s\n",
                app.externalId().get(),
                legalEntity.externalId(),
                "A test comment string",
                "y");

        BulkUploadLegalEntityRelationshipCommand uploadCmd = ImmutableBulkUploadLegalEntityRelationshipCommand.builder()
                .inputString(inputString)
                .legalEntityRelationshipKindId(leRelKindId)
                .build();


        ResolveBulkUploadLegalEntityRelationshipResponse resolvedCommand = service.resolve(uploadCmd);

        Map<String, Integer> colIdByInputString = MapUtilities.indexBy(resolvedCommand.assessmentHeaders(), AssessmentHeaderCell::inputString, AssessmentHeaderCell::columnId);
        Integer colId = colIdByInputString.get(assessmentHeader);

        assertEquals(1, resolvedCommand.rows().size(), "Should identify 1 row of data has been provided");

        Set<ResolvedUploadRow> rowsWithAssessments = SetUtilities.filter(resolvedCommand.rows(), d -> notEmpty(d.assessmentRatings()));
        assertTrue(notEmpty(rowsWithAssessments), "Data rows should have assessment associated");

        Optional<ResolvedUploadRow> dataRow = find(resolvedCommand.rows(), d -> d.rowNumber() == 2L);

        assertTrue(dataRow.isPresent(), "Should find row at index 1");

        Set<AssessmentCell> resolvedAssessmentRatings = dataRow.get().assessmentRatings();

        assertEquals(1, resolvedAssessmentRatings.size(), "Should correctly resolve 1 assessment definition column");

        Optional<AssessmentCell> firstAssessment = find(resolvedAssessmentRatings, d -> d.columnId() == colId);
        assertTrue(firstAssessment.isPresent(), "Should find assessment");
        assertFalse(firstAssessment.get().statuses().contains(ResolutionStatus.ERROR), "Should resolve assessment header with no errors");

        Set<AssessmentCellRating> resolvedRatings = firstAssessment.get().ratings();
        assertEquals(1, resolvedRatings.size(), "Should only find 1 assessment ratings for this row and definition");

        Set<AssessmentCellRating> erroredRatings = SetUtilities.filter(resolvedRatings, d -> notEmpty(d.errors()));
        assertTrue(isEmpty(erroredRatings), "Should resolve all rating values");

        Set<RatingSchemeItem> resolvedRatingValues = resolvedRatings
                .stream()
                .filter(d -> d.resolvedRating().isPresent())
                .map(d -> d.resolvedRating().get())
                .collect(Collectors.toSet());

        assertTrue(OptionalUtilities.isEmpty(first(resolvedRatings).comment()), "Should not input 'y' value as comment");
        assertEquals(asSet("TEST_GREEN"), SetUtilities.map(resolvedRatingValues, d -> d.externalId().get()), "Should be able to identify the correct rating scheme item");
    }


    @Test
    public void canParseHeadersWhenSpecificRatingColumn() {
        String name = mkName("canParseHeadersWhenSpecificRatingColumn");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long leRelKindId = legalEntityHelper.createLegalEntityRelationshipKind(name);
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP, Cardinality.ZERO_MANY, Optional.of(mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, leRelKindId)));
        long greenRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Green", 10, "green", "G", "TEST_GREEN");
        long redRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Red", 20, "red", "R", "TEST_RED");

        AssessmentDefinition defnA = defnSvc.getById(defnIdA);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));

        Application app = appSvc.getById(a.id());
        LegalEntity legalEntity = legalEntitySvc.getById(le.id());

        String assessmentComment = "A comment to add to the assessment rating";

        String assessmentHeader = format("%s / %s", defnA.externalId().get(), "TEST_RED");
        String baseString = format("%s\t%s\t%s\t%s\n", ENTITY_IDENTIFIER, LEGAL_ENTITY_IDENTIFIER, COMMENT, assessmentHeader);
        String inputString = format(baseString +
                        "%s\t%s\t%s\t%s\n",
                app.externalId().get(),
                legalEntity.externalId(),
                "A test comment string",
                assessmentComment);

        BulkUploadLegalEntityRelationshipCommand uploadCmd = ImmutableBulkUploadLegalEntityRelationshipCommand.builder()
                .inputString(inputString)
                .legalEntityRelationshipKindId(leRelKindId)
                .build();


        ResolveBulkUploadLegalEntityRelationshipResponse resolvedCommand = service.resolve(uploadCmd);

        Map<String, Integer> colIdByInputString = MapUtilities.indexBy(resolvedCommand.assessmentHeaders(), AssessmentHeaderCell::inputString, AssessmentHeaderCell::columnId);
        Integer colId = colIdByInputString.get(assessmentHeader);

        assertEquals(1, resolvedCommand.rows().size(), "Should identify 1 row of data has been provided");

        Set<ResolvedUploadRow> rowsWithAssessments = SetUtilities.filter(resolvedCommand.rows(), d -> notEmpty(d.assessmentRatings()));
        assertTrue(notEmpty(rowsWithAssessments), "Data rows should have assessment associated");

        Optional<ResolvedUploadRow> dataRow = find(resolvedCommand.rows(), d -> d.rowNumber() == 2L);

        assertTrue(dataRow.isPresent(), "Should find row at index 1");

        Set<AssessmentCell> resolvedAssessmentRatings = dataRow.get().assessmentRatings();

        assertEquals(1, resolvedAssessmentRatings.size(), "Should correctly resolve 1 assessment definition column");

        Optional<AssessmentCell> firstAssessment = find(resolvedAssessmentRatings, d -> d.columnId() == colId);
        assertTrue(firstAssessment.isPresent(), "Should find assessment");
        assertFalse((firstAssessment.get().statuses().contains(ResolutionStatus.ERROR)), "Should resolve assessment header with no errors");

        Set<AssessmentCellRating> resolvedRatings = firstAssessment.get().ratings();
        assertEquals(1, resolvedRatings.size(), "Should only find 1 assessment ratings for this row and definition");

        Set<AssessmentCellRating> erroredRatings = SetUtilities.filter(resolvedRatings, d -> notEmpty(d.errors()));
        assertTrue(isEmpty(erroredRatings), "Should resolve all rating values");

        Set<RatingSchemeItem> resolvedRatingValues = resolvedRatings
                .stream()
                .filter(d -> d.resolvedRating().isPresent())
                .map(d -> d.resolvedRating().get())
                .collect(Collectors.toSet());

        assertEquals(assessmentComment, first(resolvedRatings).comment().orElse(""), "Should be able to parse the comment string from a specific rating column");
        assertEquals(asSet("TEST_RED"), SetUtilities.map(resolvedRatingValues, d -> d.externalId().get()), "Should be able to identify the correct rating scheme item");
    }

    @Test
    public void reportsErrorsOnAssessmentHeaderForAllRowsButRelationshipCanStillBeParsed() {
        String name = mkName("reportsErrorsOnAssessmentHeaderForAllRowsButRelationshipCanStillBeParsed");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long leRelKindId = legalEntityHelper.createLegalEntityRelationshipKind(name);
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP, Cardinality.ZERO_MANY, Optional.of(mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, leRelKindId)));
        long greenRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Green", 10, "green", "G", "TEST_GREEN");
        long redRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Red", 20, "red", "R", "TEST_RED");

        AssessmentDefinition defnA = defnSvc.getById(defnIdA);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));

        Application app = appSvc.getById(a.id());
        LegalEntity legalEntity = legalEntitySvc.getById(le.id());

        String assessmentComment = "A comment to add to the assessment rating";

        String assessmentHeader = format("%s / %s", "UNKNOWN DEFN IDENTIFIER", "TEST_RED");
        String baseString = format("%s\t%s\t%s\t%s\n", ENTITY_IDENTIFIER, LEGAL_ENTITY_IDENTIFIER, COMMENT, assessmentHeader);
        String inputString = format(baseString +
                        "%s\t%s\t%s\t%s\n",
                app.externalId().get(),
                legalEntity.externalId(),
                "A test comment string",
                assessmentComment);

        BulkUploadLegalEntityRelationshipCommand uploadCmd = ImmutableBulkUploadLegalEntityRelationshipCommand.builder()
                .inputString(inputString)
                .legalEntityRelationshipKindId(leRelKindId)
                .build();


        ResolveBulkUploadLegalEntityRelationshipResponse resolvedCommand = service.resolve(uploadCmd);

        Map<String, Integer> colIdByInputString = MapUtilities.indexBy(resolvedCommand.assessmentHeaders(), AssessmentHeaderCell::inputString, AssessmentHeaderCell::columnId);
        Integer colId = colIdByInputString.get(assessmentHeader);

        assertEquals(1, resolvedCommand.rows().size(), "Should identify 1 row of data has been provided");

        Set<ResolvedUploadRow> rowsWithAssessments = SetUtilities.filter(resolvedCommand.rows(), d -> notEmpty(d.assessmentRatings()));
        assertTrue(notEmpty(rowsWithAssessments), "Data rows should have assessment associated");

        Optional<ResolvedUploadRow> dataRow = find(resolvedCommand.rows(), d -> d.rowNumber() == 2L);

        assertTrue(dataRow.isPresent(), "Should find row at index 1");

        Set<AssessmentCell> resolvedAssessmentRatings = dataRow.get().assessmentRatings();
        assertEquals(1, resolvedAssessmentRatings.size(), "Should correctly resolve 1 assessment definition column");

        Optional<AssessmentCell> firstAssessment = find(resolvedAssessmentRatings, d -> d.columnId() == colId);
        assertTrue(firstAssessment.isPresent(), "Should find assessment");
        assertFalse(firstAssessment.get().statuses().contains(ResolutionStatus.ERROR), "Should resolve assessment header with no errors");

        Set<AssessmentCellRating> resolvedRatings = firstAssessment.get().ratings();
        assertEquals(1, resolvedRatings.size(), "Should only find 1 assessment ratings for this row and definition");

        Set<AssessmentCellRating> erroredRatings = SetUtilities.filter(resolvedRatings, d -> notEmpty(d.errors()));
        assertTrue(notEmpty(erroredRatings), "Should report error as definition could not be identified");

        assertFalse(any(resolvedRatings, d -> d.resolvedRating().isPresent()), "Should be unable to resolve a rating where the definition is unknown");
    }


    @Test
    public void reportsWhereSingleValuedAssessmentAllowedOnlyAndDuplicateInRow() {
        String name = mkName("reportsWhereSingleValuedAssessmentAllowedOnlyAndDuplicateInRow");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long leRelKindId = legalEntityHelper.createLegalEntityRelationshipKind(name);
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP, Cardinality.ZERO_ONE, Optional.of(mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, leRelKindId)));
        long greenRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Green", 10, "green", "G", "TEST_GREEN");
        long redRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Red", 20, "red", "R", "TEST_RED");

        AssessmentDefinition defnA = defnSvc.getById(defnIdA);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));

        Application app = appSvc.getById(a.id());
        LegalEntity legalEntity = legalEntitySvc.getById(le.id());

        String assessmentComment = "TEST_GREEN;TEST_RED";
        String assessmentHeader = format("%s", defnA.externalId().get());

        String baseString = format("%s\t%s\t%s\t%s\n", ENTITY_IDENTIFIER, LEGAL_ENTITY_IDENTIFIER, COMMENT, assessmentHeader);
        String inputString = format(baseString +
                        "%s\t%s\t%s\t%s\n",
                app.externalId().get(),
                legalEntity.externalId(),
                "A test comment string",
                assessmentComment);

        BulkUploadLegalEntityRelationshipCommand uploadCmd = ImmutableBulkUploadLegalEntityRelationshipCommand.builder()
                .inputString(inputString)
                .legalEntityRelationshipKindId(leRelKindId)
                .build();


        ResolveBulkUploadLegalEntityRelationshipResponse resolvedCommand = service.resolve(uploadCmd);

        Map<String, Integer> colIdByInputString = MapUtilities.indexBy(resolvedCommand.assessmentHeaders(), AssessmentHeaderCell::inputString, AssessmentHeaderCell::columnId);
        Integer colId = colIdByInputString.get(assessmentHeader);

        assertEquals(1, resolvedCommand.rows().size(), "Should identify 1 row of data has been provided");

        Set<ResolvedUploadRow> rowsWithAssessments = SetUtilities.filter(resolvedCommand.rows(), d -> notEmpty(d.assessmentRatings()));
        assertTrue(notEmpty(rowsWithAssessments), "Data rows should have assessment associated");

        Optional<ResolvedUploadRow> dataRow = find(resolvedCommand.rows(), d -> d.rowNumber() == 2L);
        assertTrue(dataRow.isPresent(), "Should find row at index 1");

        Set<AssessmentCell> resolvedAssessmentRatings = dataRow.get().assessmentRatings();
        assertEquals(1, resolvedAssessmentRatings.size(), "Should correctly resolve 1 assessment definition columns");

        Optional<AssessmentCell> firstAssessment = find(resolvedAssessmentRatings, d -> d.columnId() == colId);

        assertTrue(firstAssessment.isPresent(), "Should find assessment at index 3");

        assertTrue(all(
                        firstAssessment.get().ratings(),
                        d -> SetUtilities.map(d.errors(), RatingResolutionError::errorCode).contains(RatingResolutionErrorCode.MULTIPLE_RATINGS_DISALLOWED)),
                "Single valued assessments can only be provided one rating value, reports an error otherwise");
    }

    @Test
    public void reportsWhereSingleValuedAssessmentAllowedOnlyAndDuplicateDueToColumnHeaders() {
        String name = mkName("reportsWhereSingleValuedAssessmentAllowedOnlyAndDuplicateDueToColumnHeaders");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "Scheme");
        long leRelKindId = legalEntityHelper.createLegalEntityRelationshipKind(name);
        long defnIdA = assessmentHelper.createDefinition(schemeId, name + "DefinitionA", "", AssessmentVisibility.PRIMARY, "Test", EntityKind.LEGAL_ENTITY_RELATIONSHIP, Cardinality.ZERO_ONE, Optional.of(mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, leRelKindId)));
        long greenRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Green", 10, "green", "G", "TEST_GREEN");
        long redRating = ratingSchemeHelper.saveRatingItem(schemeId, "Test Red", 20, "red", "R", "TEST_RED");

        AssessmentDefinition defnA = defnSvc.getById(defnIdA);

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference le = legalEntityHelper.create(mkName("le"));

        Application app = appSvc.getById(a.id());
        LegalEntity legalEntity = legalEntitySvc.getById(le.id());

        String assessmentHeaderRating1 = format("%s / %s", defnA.externalId().get(), "TEST_GREEN");
        String assessmentHeaderRating2 = format("%s / %s", defnA.externalId().get(), "TEST_RED");
        String assessmentComment = "y";

        String baseString = format("%s\t%s\t%s\t%s\t%s\n", ENTITY_IDENTIFIER, LEGAL_ENTITY_IDENTIFIER, COMMENT, assessmentHeaderRating1, assessmentHeaderRating2);
        String inputString = format(baseString +
                        "%s\t%s\t%s\t%s\t%s\n",
                app.externalId().get(),
                legalEntity.externalId(),
                "A test comment string",
                assessmentComment,
                assessmentComment);

        BulkUploadLegalEntityRelationshipCommand uploadCmd = ImmutableBulkUploadLegalEntityRelationshipCommand.builder()
                .inputString(inputString)
                .legalEntityRelationshipKindId(leRelKindId)
                .build();


        ResolveBulkUploadLegalEntityRelationshipResponse resolvedCommand = service.resolve(uploadCmd);

        Map<String, Integer> colIdByInputString = MapUtilities.indexBy(resolvedCommand.assessmentHeaders(), AssessmentHeaderCell::inputString, AssessmentHeaderCell::columnId);

        assertEquals(1, resolvedCommand.rows().size(), "Should identify 1 row of data has been provided");

        Set<ResolvedUploadRow> rowsWithAssessments = SetUtilities.filter(resolvedCommand.rows(), d -> notEmpty(d.assessmentRatings()));
        assertTrue(notEmpty(rowsWithAssessments), "Data rows should have assessment associated");

        Optional<ResolvedUploadRow> dataRow = find(resolvedCommand.rows(), d -> d.rowNumber() == 2L);
        assertTrue(dataRow.isPresent(), "Should find row at index 1");

        Set<AssessmentCell> resolvedAssessmentRatings = dataRow.get().assessmentRatings();
        assertEquals(2, resolvedAssessmentRatings.size(), "Should correctly resolve 2 assessment definition columns");

        Optional<AssessmentCell> firstAssessment = find(resolvedAssessmentRatings, d -> d.inputString().equalsIgnoreCase(assessmentHeaderRating1));
        Optional<AssessmentCell> secondAssessment = find(resolvedAssessmentRatings, d -> d.inputString().equalsIgnoreCase(assessmentHeaderRating2));

        assertTrue(firstAssessment.isPresent(), "Should find assessment from header with rating 1");
        assertTrue(secondAssessment.isPresent(), "Should find assessment from header with rating 1");

        assertTrue(all(
                        firstAssessment.get().ratings(),
                        d -> SetUtilities.map(d.errors(), RatingResolutionError::errorCode).contains(RatingResolutionErrorCode.MULTIPLE_RATINGS_DISALLOWED)),
                "Single valued assessments can only be provided one rating value, reports an error otherwise");

        assertTrue(all(
                        secondAssessment.get().ratings(),
                        d -> SetUtilities.map(d.errors(), RatingResolutionError::errorCode).contains(RatingResolutionErrorCode.MULTIPLE_RATINGS_DISALLOWED)),
                "Single valued assessments can only be provided one rating value, reports an error otherwise");
    }

}