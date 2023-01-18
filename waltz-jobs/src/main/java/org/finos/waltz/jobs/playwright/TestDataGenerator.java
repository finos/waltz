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
import org.finos.waltz.test_common.helpers.*;
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
        PersonHelper personHelper = ctx.getBean(PersonHelper.class);

        LOG.error("Creating data for ui testing");

        Long admin = personHelper.createAdmin();

        Long rootOU = orgHelper.createOrgUnit("Root", null);
        Long orgA = orgHelper.createOrgUnit("Org Unit A", rootOU);
        Long orgB = orgHelper.createOrgUnit("Org Unit B", rootOU);
        Long orgC = orgHelper.createOrgUnit("Org Unit C", orgA);

        EntityReference testApp = apphelper.createNewApp("Test Application", orgC);

        long schemeId = ratingSchemeHelper.createEmptyRatingScheme("Test Scheme");

        Long y = ratingSchemeHelper.saveRatingItem(schemeId, "Yes", 10, "green", "Y");
        Long n = ratingSchemeHelper.saveRatingItem(schemeId, "No", 20, "red", "N");
        Long m = ratingSchemeHelper.saveRatingItem(schemeId, "Maybe", 30, "yellow", "M");

        long defA = assessmentHelper.createDefinition(schemeId, "Test Definition A", null, AssessmentVisibility.PRIMARY, "Edit Favourites");
        long defB = assessmentHelper.createDefinition(schemeId, "Test Definition B", null, AssessmentVisibility.SECONDARY, "Edit Favourites");
        long defC = assessmentHelper.createDefinition(schemeId, "Test Definition C", null, AssessmentVisibility.PRIMARY, "Edit Favourites");
        long defD = assessmentHelper.createDefinition(schemeId, "Test Definition D", null, AssessmentVisibility.PRIMARY, "Toggle Group");
        long defE = assessmentHelper.createDefinition(schemeId, "Test Definition E", null, AssessmentVisibility.PRIMARY, "Update Rating");
        long defF = assessmentHelper.createDefinition(schemeId, "Test Definition F", null, AssessmentVisibility.PRIMARY, "Delete Rating");

        try {

            assessmentHelper.createAssessment(defA, testApp, y, "test");
            assessmentHelper.createAssessment(defB, testApp, n, "test");
            assessmentHelper.createAssessment(defC, testApp, y, "test");
            assessmentHelper.createAssessment(defD, testApp, y, "test");
            assessmentHelper.createAssessment(defE, testApp, y, "test");
            assessmentHelper.createAssessment(defF, testApp, y, "test");

        } catch (InsufficientPrivelegeException e) {
            LOG.error("Could not create assessments for playwright tests", e);
        }

        LOG.error("Test data creation completed");

    }

}
