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

package org.finos.waltz.web.endpoints.api;


import org.finos.waltz.service.assessment_rating.AssessmentRatingViewService;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.assessment_rating.AssessmentGroupedEntities;
import org.finos.waltz.model.assessment_rating.AssessmentRatingDetail;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.finos.waltz.common.Checks.checkNotNull;


@Service
public class AssessmentRatingViewEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "assessment-rating-view");

    private final AssessmentRatingViewService assessmentRatingViewService;


    @Autowired
    public AssessmentRatingViewEndpoint(AssessmentRatingViewService assessmentRatingViewService) {

        checkNotNull(assessmentRatingViewService, "assessmentRatingViewService cannot be null");

        this.assessmentRatingViewService = assessmentRatingViewService;
    }


    @Override
    public void register() {
        String findGroupedByDefinitionAndOutcomePath = WebUtilities.mkPath(BASE_URL, "kind", ":kind", "grouped");
        String findFavouriteAssessmentsForEntityPath = WebUtilities.mkPath(BASE_URL, "kind", ":kind", "id", ":id");

        ListRoute<AssessmentGroupedEntities> findGroupedByDefinitionAndOutcomeRoute = (req, res) -> {
            List<Long> entityIds = WebUtilities.readIdsFromBody(req);
            return assessmentRatingViewService.findGroupedByDefinitionAndOutcomes(WebUtilities.getKind(req), entityIds);
        };

        ListRoute<AssessmentRatingDetail> findFavouriteAssessmentsForEntityRoute = (req, res) -> assessmentRatingViewService.findFavouriteAssessmentsForEntityAndUser(WebUtilities.getEntityReference(req), WebUtilities.getUsername(req));

        EndpointUtilities.postForList(findGroupedByDefinitionAndOutcomePath, findGroupedByDefinitionAndOutcomeRoute);
        EndpointUtilities.getForList(findFavouriteAssessmentsForEntityPath, findFavouriteAssessmentsForEntityRoute);
    }

}

