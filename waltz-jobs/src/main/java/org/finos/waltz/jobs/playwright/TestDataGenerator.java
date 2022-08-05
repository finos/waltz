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

package org.finos.waltz.jobs.playwright;

import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.assessment_definition.AssessmentVisibility;
import org.finos.waltz.service.DIConfiguration;
import org.finos.waltz.test_common_again.helpers.AppHelper;
import org.finos.waltz.test_common_again.helpers.AssessmentHelper;
import org.finos.waltz.test_common_again.helpers.OrgUnitHelper;
import org.finos.waltz.test_common_again.helpers.RatingSchemeHelper;
import org.jooq.tools.json.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class TestDataGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(TestDataGenerator.class);

    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        AppHelper apphelper = ctx.getBean(AppHelper.class);
        OrgUnitHelper orgHelper = ctx.getBean(OrgUnitHelper.class);
        RatingSchemeHelper ratingSchemeHelper = ctx.getBean(RatingSchemeHelper.class);
        AssessmentHelper assessmentHelper = ctx.getBean(AssessmentHelper.class);

        Long rootOU = orgHelper.createOrgUnit("Root", null);
        Long orgA = orgHelper.createOrgUnit("Org Unit A", rootOU);
        Long orgB = orgHelper.createOrgUnit("Org Unit B", rootOU);
        Long orgC = orgHelper.createOrgUnit("Org Unit C", orgA);

        EntityReference testApp = apphelper.createNewApp("Test Application", orgC);

        long schemeId = ratingSchemeHelper.createEmptyRatingScheme("Test Scheme");

        Long y = ratingSchemeHelper.saveRatingItem(schemeId, "Yes", 10, "green", 'Y');
        Long n = ratingSchemeHelper.saveRatingItem(schemeId, "No", 20, "red", 'N');
        Long m = ratingSchemeHelper.saveRatingItem(schemeId, "Maybe", 30, "yellow", 'M');

        long defA = assessmentHelper.createDefinition(schemeId, "Test Definition A", null, AssessmentVisibility.PRIMARY);
        long defB = assessmentHelper.createDefinition(schemeId, "Test Definition B", null, AssessmentVisibility.SECONDARY);

        try {
            assessmentHelper.createAssessment(defA, testApp, y, "test");
            assessmentHelper.createAssessment(defB, testApp, n, "test");
        } catch (InsufficientPrivelegeException e) {
            LOG.error("Could not create assessments for playwright tests", e);
        }
    }

}
