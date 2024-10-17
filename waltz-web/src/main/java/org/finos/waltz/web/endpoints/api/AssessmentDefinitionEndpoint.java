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

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_definition.ImmutableAssessmentDefinition;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.service.assessment_definition.AssessmentDefinitionService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.web.WebUtilities.*;

@Service
public class AssessmentDefinitionEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "assessment-definition");

    private final AssessmentDefinitionService assessmentDefinitionService;
    private final UserRoleService userRoleService;


    @Autowired
    public AssessmentDefinitionEndpoint(AssessmentDefinitionService assessmentDefinitionService, UserRoleService userRoleService) {
        checkNotNull(assessmentDefinitionService, "assessmentDefinitionService cannot be null");

        this.assessmentDefinitionService = assessmentDefinitionService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String getByIdPath = WebUtilities.mkPath(BASE_URL, "id", ":id");
        String favouriteByIdPath = WebUtilities.mkPath(BASE_URL, "id", ":id", "favourite");
        String findFavouritesForUserPath = WebUtilities.mkPath(BASE_URL, "favourites");
        String findAllPath = WebUtilities.mkPath(BASE_URL);
        String savePath = WebUtilities.mkPath(BASE_URL);
        String findByKindPath = WebUtilities.mkPath(BASE_URL, "kind", ":kind");
        String findByRefPath = WebUtilities.mkPath(BASE_URL, "kind", ":kind", "id", ":id");
        String removeByIdPath = WebUtilities.mkPath(BASE_URL, "id", ":id");


        DatumRoute<AssessmentDefinition> getByIdRoute = (request, response) -> assessmentDefinitionService.getById(WebUtilities.getId(request));
        ListRoute<AssessmentDefinition> findAllRoute = (request, response) -> assessmentDefinitionService.findAll();
        ListRoute<AssessmentDefinition> findByKindRoute = (request, response) -> assessmentDefinitionService.findByEntityKind(WebUtilities.getKind(request));
        ListRoute<AssessmentDefinition> findByRefRoute = (request, response) -> assessmentDefinitionService.findByEntityReference(WebUtilities.getEntityReference(request));

        EndpointUtilities.getForDatum(getByIdPath, getByIdRoute);
        EndpointUtilities.getForList(findAllPath, findAllRoute);
        EndpointUtilities.getForList(findByKindPath, findByKindRoute);
        EndpointUtilities.getForList(findByRefPath, findByRefRoute);
        EndpointUtilities.putForDatum(savePath, this::saveRoute);
        EndpointUtilities.deleteForDatum(removeByIdPath, this::removeByIdRoute);

        EndpointUtilities.getForList(findFavouritesForUserPath, this::findFavouritesForUser);
        EndpointUtilities.putForList(favouriteByIdPath, this::addFavourite);
        EndpointUtilities.deleteForList(favouriteByIdPath, this::removeFavourite);
    }

    private Set<AssessmentDefinition> findFavouritesForUser(Request request, Response response) {
        return assessmentDefinitionService.findFavouritesForUser(getUsername(request));
    }


    private Set<AssessmentDefinition> addFavourite(Request request, Response response) {
        return assessmentDefinitionService.addFavourite(
                getId(request),
                getUsername(request));
    }


    private Set<AssessmentDefinition> removeFavourite(Request request, Response response) {
        return assessmentDefinitionService.removeFavourite(
                getId(request),
                getUsername(request));
    }


    private long saveRoute(Request request, Response response) throws IOException {
        ensureUserHasEditRights(request);
        AssessmentDefinition def = ImmutableAssessmentDefinition
                .copyOf(WebUtilities.readBody(request, AssessmentDefinition.class))
                .withLastUpdatedAt(DateTimeUtilities.nowUtc())
                .withLastUpdatedBy(getUsername(request));

        return assessmentDefinitionService.save(def);
    }

    private boolean removeByIdRoute(Request request, Response response) {
        ensureUserHasEditRights(request);
        long definitionId = WebUtilities.getId(request);

        return assessmentDefinitionService.remove(definitionId);
    }


    private void ensureUserHasEditRights(Request request) {
        WebUtilities.requireAnyRole(userRoleService, request, SystemRole.ASSESSMENT_DEFINITION_ADMIN, SystemRole.ADMIN);
    }

}
