package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_definition.AssessmentVisibility;
import org.finos.waltz.model.assessment_rating.bulk_upload.AssessmentRatingValidationResult;
import org.finos.waltz.model.assessment_rating.bulk_upload.ValidationError;
import org.finos.waltz.service.application.ApplicationService;
import org.finos.waltz.service.assessment_definition.AssessmentDefinitionService;
import org.finos.waltz.service.assessment_rating.BulkAssessmentRatingService;
import org.finos.waltz.test_common.helpers.ActorHelper;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.finos.waltz.test_common.helpers.AssessmentHelper;
import org.finos.waltz.test_common.helpers.RatingSchemeHelper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.finos.waltz.common.CollectionUtilities.all;
import static org.finos.waltz.common.CollectionUtilities.isEmpty;
import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.junit.jupiter.api.Assertions.*;

public class BulkAssessmentRatingServiceTest extends BaseInMemoryIntegrationTest {
    private static final Logger LOG = LoggerFactory.getLogger(BulkAssessmentRatingServiceTest.class);

    @Autowired
    private AssessmentHelper assessmentHelper;
    @Autowired
    private RatingSchemeHelper ratingSchemeHelper;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private BulkAssessmentRatingService bulkAssessmentRatingService;

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private ActorHelper actorHelper;

    @Autowired
    private AssessmentDefinitionService assessmentDefinitionService;

    private static final String stem = "BAR";

    @Test
    public void previewAdds() {
        /**
         * Entity Kind: APPLICATION
         */
        String name = mkName(stem, "previewApp");
        String kindExternalId = mkName(stem, "previewAppCode");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "SchemeApp");
        ratingSchemeHelper.saveRatingItem(schemeId, "Yes", 0, "green", "Y");
        appHelper.createNewApp(
                mkName(stem, "previewUpdatesApp"),
                ouIds.root,
                kindExternalId);
        AssessmentDefinition def1 = assessmentDefinitionService.getById(getAssessmentDefinition(EntityKind.APPLICATION, schemeId, name));

        AssessmentRatingValidationResult result1 = bulkAssessmentRatingService.bulkPreview(
                mkRef(def1.entityKind(), def1.id().get()),
                mkGoodTsv(kindExternalId));

        assertNotNull(result1, "Expected a result");
        assertNoErrors(result1);
        assertExternalIdsMatch(result1, asList(kindExternalId));

        /**
         *EntityKind: ACTOR
         */
        String actorName = mkName(stem,"previewActor");
        Long actorSchemeId = ratingSchemeHelper.createEmptyRatingScheme(name+"SchemeActor");
        ratingSchemeHelper.saveRatingItem(actorSchemeId,"Yes",0,"green","Y");
        actorHelper.createActor(actorName);
        AssessmentDefinition def2 = assessmentDefinitionService.getById(getAssessmentDefinition(EntityKind.ACTOR,actorSchemeId,actorName));

        AssessmentRatingValidationResult result2 = bulkAssessmentRatingService.bulkPreview(
                mkRef(def2.entityKind(),def2.id().get()),
                mkGoodTsv(actorName));

        assertNotNull(result2,"Expected a result");
        assertNoErrors(result2);
        assertExternalIdsMatch(result2,asList(actorName));

    }

    @Test
    public void previewAddsForCardinalityChecks() {
        String appName = mkName(stem, "previewApp3");
        String appExternalId = mkName(stem, "previewAppCode3");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(mkName(stem, "SchemeApp1"));
        ratingSchemeHelper.saveRatingItem(schemeId, "Yes", 0, "green", "Y");
        ratingSchemeHelper.saveRatingItem(schemeId, "No", 0, "red", "N");
        appHelper.createNewApp(
                appName,
                ouIds.root,
                appExternalId);
        AssessmentDefinition def = assessmentDefinitionService.getById(getAssessmentDefinition(EntityKind.APPLICATION, schemeId, "Assessment1"));

        /**
         * Zero-One
         */
        String[] externalIds = {appExternalId, appExternalId};
        String[] ratingCodes = {"Y", "N"};
        AssessmentRatingValidationResult result = bulkAssessmentRatingService.bulkPreview(
                mkRef(def.entityKind(), def.id().get()),
                mkTsvWithForCardinalityCheck(externalIds, ratingCodes));

        result
                .validatedItems()
                .forEach(d -> {
                    if (d.parsedItem().ratingCode().equals("N")) {
                        assertTrue(d.errors().contains(ValidationError.DUPLICATE), "Should be complaining about the duplicate entity with rating N");
                    }
                });

        assertEquals(2, result.validatedItems().size(), "Expected 2 items");
    }

    @Test
    public void previewUpdateErrors() {
        String appName = mkName(stem, "previewApp1");
        String appExternalId = mkName(stem, "previewAppCode1");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(mkName(stem, "SchemeApp"));
        ratingSchemeHelper.saveRatingItem(schemeId, "Yes", 0, "green", "Y");
        ratingSchemeHelper.saveRatingItem(schemeId, "No", 0, "red", "N");
        appHelper.createNewApp(
                appName,
                ouIds.root,
                appExternalId);
        AssessmentDefinition def = assessmentDefinitionService.getById(getAssessmentDefinition(EntityKind.APPLICATION, schemeId, "Assessment"));

        AssessmentRatingValidationResult result = bulkAssessmentRatingService.bulkPreview(
                mkRef(def.entityKind(), def.id().get()),
                mkBadTsv(appExternalId));

        result
                .validatedItems()
                .forEach(d -> {
                    if (d.parsedItem().ratingCode().equals("badExternalId")) {
                        assertTrue(d.errors().contains(ValidationError.ENTITY_KIND_NOT_FOUND), "Should be complaining about the entity not found");
                    }
                    if (d.parsedItem().ratingCode().equals("badRatingCode")) {
                        assertTrue(d.errors().contains(ValidationError.RATING_NOT_FOUND), "Should be complaining about the rating code not found");
                    }
                });

        assertEquals(3, result.validatedItems().size(), "Expected 3 items");
    }

    private long getAssessmentDefinition(EntityKind kind, Long schemeId, String name) {
        return assessmentHelper.createDefinition(schemeId, name + "Definition", "", AssessmentVisibility.PRIMARY, "Test", kind, null);
    }

    private void assertNoErrors(AssessmentRatingValidationResult result) {
        assertTrue(
                all(result.validatedItems(), d -> isEmpty(d.errors())),
                "Should have no errors");
    }

    private void assertExternalIdsMatch(AssessmentRatingValidationResult result,
                                        List<String> expectedExternalIds) {
        assertEquals(
                expectedExternalIds,
                ListUtilities.map(result.validatedItems(), d -> d.parsedItem().externalId()),
                "Expected external ids do not match");
    }

    private String mkGoodTsv(String externalId) {
        return "externalId\tratingCode\tisReadOnly\tcomment\n"
                + externalId + "\tY\ttrue\tcomment\n";
    }

    private String mkTsvWithForCardinalityCheck(String[] externalIds, String[] ratingCodes) {
        return "externalId\tratingCode\tisReadOnly\tcomment\n"
                + externalIds[0] + "\t" + ratingCodes[0] + "\ttrue\tcomment\n"
                + externalIds[1] + "\t" + ratingCodes[1] + "\ttrue\tcomment\n";
    }

    private String mkBadTsv(String externalId) {
        return "externalId\tratingCode\tisReadOnly\tcomment\n"
                +"badExternalId\tY\ttrue\tcomment\n"
                + externalId + "\tN\ttrue\tcomment\n"
                + externalId + "\tbadRatingCode\ttrue\tcomment\n";
    }

    @Test
    public void bulkPreviewForCardinalityZeroToMany() {
        String appName = mkName(stem, "previewApp");
        String appExternalId = mkName(stem, "previewAppCode");
        String appName1 = mkName(stem, "previewApp1");
        String appExternalId1 = mkName(stem, "previewAppCode1");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(mkName(stem, "SchemeApp1"));
        ratingSchemeHelper.saveRatingItem(schemeId, "Yes", 0, "green", "Y");
        ratingSchemeHelper.saveRatingItem(schemeId, "No", 0, "red", "N");
        appHelper.createNewApp(
                appName,
                ouIds.root,
                appExternalId);
        appHelper.createNewApp(
                appName1,
                ouIds.root,
                appExternalId1);
        long defId = assessmentHelper.createDefinition(schemeId, "Assessment_DefinitionA", "", AssessmentVisibility.PRIMARY, "Test",
                EntityKind.LEGAL_ENTITY_RELATIONSHIP, Cardinality.ZERO_MANY, Optional.ofNullable(null));
        AssessmentDefinition def = assessmentDefinitionService.getById(defId);
        String[] externalIds = {appExternalId,appExternalId1};
        String[] ratingCodes = {"Y","N"};
        AssessmentRatingValidationResult result = bulkAssessmentRatingService.bulkPreview(
                mkRef(def.entityKind(), def.id().get()),
                mkTsvWithForCardinalityCheck(externalIds, ratingCodes));

        assertEquals(2, result.validatedItems().size(), "Expected 2 items");
    }

    @Test
    public void bulkUpdateForCardinalityZeroToMany() {
        String name = mkName(stem, "previewApp1");
        String kindExternalId = mkName(stem, "previewAppExtId");
        Long schemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "SchemeApp1");
        ratingSchemeHelper.saveRatingItem(schemeId, "Yes", 0, "green", "Y");
        appHelper.createNewApp(
                mkName(stem, "previewUpdatesApp1"),
                ouIds.root,
                kindExternalId);
        long defId = assessmentHelper.createDefinition(schemeId, "Assessment_Definition1", "", AssessmentVisibility.PRIMARY, "Test",
                EntityKind.APPLICATION, Cardinality.ZERO_MANY, Optional.ofNullable(null));
        AssessmentDefinition def1 = assessmentDefinitionService.getById(defId);
        EntityReference cfg = mkRef(EntityKind.APPLICATION, defId);
        SaveAssessmentRatingCommand cmd = ImmutableSaveAssessmentRatingCommand.builder()
                .entityReference(cfg)
                .assessmentDefinitionId(defId)
                .ratingId(26)
                .lastUpdatedBy("test")
                .build();
        try {
            assessmentRatingService.store(cmd, "test");
        } catch (InsufficientPrivelegeException e) {
            e.printStackTrace();
        }
        AssessmentRatingValidationResult result1 = bulkAssessmentRatingService.bulkPreview(
                mkRef(def1.entityKind(), def1.id().get()),
                mkGoodTsv(kindExternalId));

        assertNotNull(result1.validatedItems().get(0).changeOperation(), ChangeOperation.UPDATE.name());
    }
}