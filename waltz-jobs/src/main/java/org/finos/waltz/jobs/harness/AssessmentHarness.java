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

package org.finos.waltz.jobs.harness;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.assessment_rating.AssessmentRatingDetail;
import org.finos.waltz.service.DIConfiguration;
import org.finos.waltz.service.assessment_rating.AssessmentRatingViewService;
import org.finos.waltz.service.permission.permission_checker.RatingPermissionChecker;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Set;

import static org.finos.waltz.model.EntityReference.mkRef;


public class AssessmentHarness {

    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        AssessmentRatingViewService viewSvc = ctx.getBean(AssessmentRatingViewService.class);
        RatingPermissionChecker permChecker = ctx.getBean(RatingPermissionChecker.class);


        Set<Operation> perms = permChecker.findRatingPermissions(mkRef(EntityKind.APPLICATION, 90L), 14, "admin");
        System.out.println(perms);


        System.out.println("done");
    }

}
