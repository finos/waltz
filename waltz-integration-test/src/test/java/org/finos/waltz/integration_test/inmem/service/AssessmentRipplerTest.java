package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.data.assessment_rating.AssessmentRatingRippler;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.assessment_definition.AssessmentVisibility;
import org.finos.waltz.schema.tables.AssessmentRating;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.finos.waltz.test_common.helpers.AssessmentHelper;
import org.finos.waltz.test_common.helpers.MeasurableHelper;
import org.finos.waltz.test_common.helpers.RatingSchemeHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.ASSESSMENT_RATING;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AssessmentRipplerTest extends BaseInMemoryIntegrationTest {

    private static final AssessmentRating ar = ASSESSMENT_RATING;

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private MeasurableHelper measurableHelper;

    @Autowired
    private AssessmentHelper assessmentHelper;

    @Autowired
    private RatingSchemeHelper ratingSchemeHelper;


    private final String stem = "ripple";

    @Test
    public void cannotRippleBetweenAssessmentsWithDifferingRatingSchemes() {
        long schemeA = ratingSchemeHelper.createEmptyRatingScheme(mkName(stem, "bad_ripple_a"));
        long schemeB = ratingSchemeHelper.createEmptyRatingScheme(mkName(stem, "bad_ripple_b"));

        long assmtA = assessmentHelper.createDefinition(schemeA, mkName(stem, "ripple assmt A"), null, AssessmentVisibility.SECONDARY, stem);
        long assmtB = assessmentHelper.createDefinition(schemeB, mkName(stem, "ripple assmt B"), null, AssessmentVisibility.SECONDARY, stem);
        String assmtA_extId = mkName(stem, "ASSMT_A");
        String assmtB_extId = mkName(stem, "ASSMT_B");
        assessmentHelper.setDefExtId(assmtA, assmtA_extId);
        assessmentHelper.setDefExtId(assmtB, assmtB_extId);

        assertThrows(
                IllegalArgumentException.class,
                () -> AssessmentRatingRippler.rippleAssessment(
                        getDsl(),
                        "admin",
                        "rippler_test",
                        assmtA_extId,
                        assmtB_extId));

    }


    @Test
    public void canRippleBetweenAssessmentsWithSameRatingSchemes() {
        // setup app, measurable, and rating
        EntityReference appRef = appHelper.createNewApp(mkName(stem, "ripple_app"), ouIds.root);
        EntityReference unrelatedRef = appHelper.createNewApp(mkName(stem, "ripple_unrelated_app"), ouIds.root);
        long categoryId = measurableHelper.createMeasurableCategory(mkName(stem, "ripple_mc"));
        long measurableId = measurableHelper.createMeasurable(mkName(stem, "ripple_m"), categoryId);

        // link app to measurable
        measurableHelper.createRating(appRef, measurableId);

        // create schemes, rating items and assessments
        long scheme = ratingSchemeHelper.createEmptyRatingScheme(mkName(stem, "good_ripple_scheme"));
        Long rsiId = ratingSchemeHelper.saveRatingItem(scheme, mkName(stem, "ripple_rsi"), 0, "pink", "P");
        long assmtA = assessmentHelper.createDefinition(scheme, mkName(stem, "ripple assmt A"), null, AssessmentVisibility.SECONDARY, stem, EntityKind.MEASURABLE, mkRef(EntityKind.MEASURABLE_CATEGORY, categoryId));
        long assmtB = assessmentHelper.createDefinition(scheme, mkName(stem, "ripple assmt B"), null, AssessmentVisibility.SECONDARY, stem, EntityKind.APPLICATION, null);
        String assmtA_extId = mkName(stem, "ASSMT_A");
        String assmtB_extId = mkName(stem, "ASSMT_B");
        assessmentHelper.setDefExtId(assmtA, assmtA_extId);
        assessmentHelper.setDefExtId(assmtB, assmtB_extId);

        // link assessment rating to measurable
        assessmentHelper.createAssessment(assmtA, mkRef(EntityKind.MEASURABLE, measurableId), rsiId);

        // ripple
        AssessmentRatingRippler.rippleAssessment(
                getDsl(),
                "admin",
                "rippler_test",
                assmtA_extId,
                assmtB_extId);

        // verify
        assertEquals(
                rsiId,
                fetchAssessmentRatingItemId(appRef, assmtB),
                "Rating will have rippled from measurable to application");

        assertNull(
                fetchAssessmentRatingItemId(unrelatedRef, assmtB),
                "Rating won't have rippled to an unrelated app");
    }


    // --- helpers -------------

    private Long fetchAssessmentRatingItemId(EntityReference ref,
                                             long assessmentDefinitionId) {
        return getDsl()
                .select(ar.RATING_ID)
                .from(ar)
                .where(ar.ENTITY_KIND.eq(ref.kind().name()))
                .and(ar.ENTITY_ID.eq(ref.id()))
                .and(ar.ASSESSMENT_DEFINITION_ID.eq(assessmentDefinitionId))
                .fetchOne(ar.RATING_ID);
    }


}
