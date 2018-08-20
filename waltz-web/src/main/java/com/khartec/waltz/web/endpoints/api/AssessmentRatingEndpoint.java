/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.web.endpoints.api;


import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.model.LastUpdate;
import com.khartec.waltz.model.assessment_definition.AssessmentDefinition;
import com.khartec.waltz.model.assessment_rating.*;
import com.khartec.waltz.service.assessment_definition.AssessmentDefinitionService;
import com.khartec.waltz.service.assessment_rating.AssessmentRatingService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Map;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class AssessmentRatingEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "assessment-rating");

    private final AssessmentRatingService assessmentRatingService;
    private final AssessmentDefinitionService assessmentDefinitionService;
    private final UserRoleService userRoleService;


    @Autowired
    public AssessmentRatingEndpoint(AssessmentRatingService assessmentRatingService,
                                    AssessmentDefinitionService assessmentDefinitionService,
                                    UserRoleService userRoleService) {

        checkNotNull(assessmentRatingService, "assessmentRatingService cannot be null");
        checkNotNull(assessmentDefinitionService, "assessmentDefinitionService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.assessmentRatingService = assessmentRatingService;
        this.assessmentDefinitionService = assessmentDefinitionService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String findForEntityPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String modifyPath = mkPath(BASE_URL, "entity", ":kind", ":id", ":assessmentDefinitionId");

        ListRoute<AssessmentRating> findForEntityRoute = (request, response) -> assessmentRatingService.findForEntity(getEntityReference(request));

        getForList(findForEntityPath, findForEntityRoute);
        postForDatum(modifyPath, this::createRoute);
        putForDatum(modifyPath, this::updateRoute);
        deleteForDatum(modifyPath, this::removeRoute);
    }


    private boolean updateRoute(Request request, Response z) throws IOException {
        SaveAssessmentRatingCommand command = mkCommand(request);
        AssessmentDefinition def = assessmentDefinitionService.getById(command.assessmentDefinitionId());
        def.permittedRole().ifPresent(r -> requireRole(userRoleService, request, r));
        return assessmentRatingService.update(command);
    }


    private boolean createRoute(Request request, Response z) throws IOException {
        SaveAssessmentRatingCommand command = mkCommand(request);
        AssessmentDefinition def = assessmentDefinitionService.getById(command.assessmentDefinitionId());
        def.permittedRole().ifPresent(r -> requireRole(userRoleService, request, r));
        return assessmentRatingService.create(command);
    }


    private boolean removeRoute(Request request, Response z) throws IOException {
        String username = getUsername(request);
        LastUpdate lastUpdate = LastUpdate.mkForUser(username);
        RemoveAssessmentRatingCommand command = ImmutableRemoveAssessmentRatingCommand.builder()
                .entityReference(getEntityReference(request))
                .assessmentDefinitionId(getLong(request, "assessmentDefinitionId"))
                .lastUpdatedAt(lastUpdate.at())
                .lastUpdatedBy(lastUpdate.by())
                .build();

        AssessmentDefinition def = assessmentDefinitionService.getById(command.assessmentDefinitionId());
        def.permittedRole().ifPresent(r -> requireRole(userRoleService, request, r));
        return assessmentRatingService.remove(command);
    }


    private SaveAssessmentRatingCommand mkCommand(Request request) throws IOException {
        String username = getUsername(request);

        Map<String, Object> body = readBody(request, Map.class);
        LastUpdate lastUpdate = LastUpdate.mkForUser(username);

        return ImmutableSaveAssessmentRatingCommand.builder()
                .entityReference(getEntityReference(request))
                .assessmentDefinitionId(getLong(request, "assessmentDefinitionId"))
                .ratingId(Long.valueOf(body.getOrDefault("ratingId", "").toString()))
                .description(StringUtilities.mkSafe((String) body.get("description")))
                .lastUpdatedAt(lastUpdate.at())
                .lastUpdatedBy(lastUpdate.by())
                .provenance("waltz")
                .build();
    }
}

