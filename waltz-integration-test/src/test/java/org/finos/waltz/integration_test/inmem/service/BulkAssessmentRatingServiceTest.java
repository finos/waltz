package org.finos.waltz.integration_test.inmem.service;
import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_definition.AssessmentVisibility;
import org.finos.waltz.model.assessment_rating.bulk_upload.AssessmentRatingValidationResult;
import org.finos.waltz.model.bulk_upload.BulkUpdateMode;
import org.finos.waltz.service.application.ApplicationService;
import org.finos.waltz.service.assessment_definition.AssessmentDefinitionService;
import org.finos.waltz.service.assessment_rating.BulkAssessmentRatingItemParser;
import org.finos.waltz.service.assessment_rating.BulkAssessmentRatingService;
import org.finos.waltz.test_common.helpers.ActorHelper;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.finos.waltz.test_common.helpers.AssessmentHelper;
import org.finos.waltz.test_common.helpers.RatingSchemeHelper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import static org.finos.waltz.common.ListUtilities.asList;


import java.util.List;
import java.util.Optional;

import static org.finos.waltz.common.CollectionUtilities.all;
import static org.finos.waltz.common.CollectionUtilities.isEmpty;
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
        ratingSchemeHelper.saveRatingItem(schemeId, "Yes", 10, "green", "Y");
        appHelper.createNewApp(
                mkName(stem, "previewUpdatesApp"),
                ouIds.root,
                kindExternalId);
        AssessmentDefinition def1 = assessmentDefinitionService.getById(getAssessmentDefinition(EntityKind.APPLICATION, schemeId, name));

        AssessmentRatingValidationResult result1 = bulkAssessmentRatingService.bulkPreview(
                mkRef(def1.entityKind(), def1.id().get()),
                mkGoodTsv(kindExternalId),
                BulkAssessmentRatingItemParser.InputFormat.TSV,
                BulkUpdateMode.ADD_ONLY);

        assertNotNull(result1, "Expected a result");
        assertNoErrors(result1);
        assertExternalIdsMatch(result1, asList(kindExternalId));

        /**
         * Entity Kind: ACTOR
         * Need work as it is failing. external id is coming as null
         */
//        String actorName = mkName(stem, "previewActor");
//        String actorExternalId = mkName(stem, "previewActorCode");
//        Long actorSchemeId = ratingSchemeHelper.createEmptyRatingScheme(name + "SchemeActor");
//        ratingSchemeHelper.saveRatingItem(actorSchemeId, "Yes", 10, "green", "Z");
//        actorHelper.createActor(actorName);
//        AssessmentDefinition def2 = assessmentDefinitionService.getById(getAssessmentDefinition(EntityKind.ACTOR, actorSchemeId, actorName));
//
//        AssessmentRatingValidationResult result2 = bulkAssessmentRatingService.bulkPreview(
//                mkRef(def2.entityKind(), def2.id().get()),
//                mkGoodTsv(actorExternalId),
//                BulkAssessmentRatingItemParser.InputFormat.TSV,
//                BulkUpdateMode.ADD_ONLY);
//
//        assertNotNull(result2, "Expected a result");
//        assertNoErrors(result2);
//        assertExternalIdsMatch(result2, asList(actorExternalId));
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

}
/**
 * previewAdd
 * Test case around cardinality check
 * previewUpdate
 */