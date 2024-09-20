package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.assessment_definition.AssessmentVisibility;
import org.finos.waltz.service.permission.permission_checker.AssessmentRatingPermissionChecker;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.finos.waltz.test_common.helpers.AssessmentHelper;
import org.finos.waltz.test_common.helpers.InvolvementHelper;
import org.finos.waltz.test_common.helpers.PermissionGroupHelper;
import org.finos.waltz.test_common.helpers.PersonHelper;
import org.finos.waltz.test_common.helpers.RatingSchemeHelper;
import org.finos.waltz.test_common.helpers.UserHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.finos.waltz.test_common.helpers.NameHelper.mkName;

public class AssessmentRipplerTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private AssessmentRatingPermissionChecker assessmentRatingPermissionChecker;

    @Autowired
    private PersonHelper personHelper;

    @Autowired
    private AssessmentHelper assessmentHelper;

    @Autowired
    private RatingSchemeHelper ratingSchemeHelper;

    @Autowired
    private InvolvementHelper involvementHelper;

    @Autowired
    private PermissionGroupHelper permissionHelper;

    @Autowired
    private UserHelper userHelper;

    private final String stem = "ripple";

    @Test
    public void cannotRippleBetweenAssessmentsWithDifferingRatingSchemes() {
        long schemeA = ratingSchemeHelper.createEmptyRatingScheme(mkName(stem, "bad_ripple_a"));
        long schemeB = ratingSchemeHelper.createEmptyRatingScheme(mkName(stem, "bad_ripple_b"));

        assessmentHelper.createDefinition(schemeA, mkName(stem, "ripple assmt A"), null, AssessmentVisibility.SECONDARY, stem);
        assessmentHelper.createDefinition(schemeB, mkName(stem, "ripple assmt B"), null, AssessmentVisibility.SECONDARY, stem);
    }


}
