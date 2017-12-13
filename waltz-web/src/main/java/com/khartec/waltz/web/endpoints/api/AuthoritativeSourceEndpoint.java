/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
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


import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Entry;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.authoritativesource.AuthoritativeSource;
import com.khartec.waltz.model.authoritativesource.AuthoritativeSourceCreateCommand;
import com.khartec.waltz.model.authoritativesource.AuthoritativeSourceUpdateCommand;
import com.khartec.waltz.model.authoritativesource.NonAuthoritativeSource;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.authoritative_source.AuthoritativeSourceService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class AuthoritativeSourceEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(AuthoritativeSourceEndpoint.class);
    private static final String BASE_URL = mkPath("api", "authoritative-source");

    private final AuthoritativeSourceService authoritativeSourceService;
    private final UserRoleService userRoleService;


    @Autowired
    public AuthoritativeSourceEndpoint(
            AuthoritativeSourceService authoritativeSourceService,
            UserRoleService userRoleService) {
        checkNotNull(authoritativeSourceService, "authoritativeSourceService must not be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.authoritativeSourceService = authoritativeSourceService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {

        // -- PATHS

        String recalculateFlowRatingsPath = mkPath(BASE_URL, "recalculate-flow-ratings");
        String findNonAuthSourcesPath = mkPath(BASE_URL, "non-auth-for", ":kind", ":id");
        String findAuthSourcesPath = mkPath(BASE_URL, "auth-for", ":kind", ":id");
        String calculateConsumersForDataTypeIdSelectorPath = mkPath(BASE_URL, "data-type", "consumers");
        String findByEntityReferencePath = mkPath(BASE_URL, "entity-ref", ":kind", ":id");
        String findByApplicationIdPath = mkPath(BASE_URL, "app", ":id");
        String deletePath = mkPath(BASE_URL, "id", ":id");
        String cleanupOrphansPath = mkPath(BASE_URL, "cleanup-orphans");


        // -- ROUTES

        ListRoute<AuthoritativeSource> findByEntityReferenceRoute = (request, response)
                -> authoritativeSourceService.findByEntityReference(getEntityReference(request));

        ListRoute<AuthoritativeSource> findByApplicationIdRoute = (request, response)
                -> authoritativeSourceService.findByApplicationId(getId(request));

        ListRoute<NonAuthoritativeSource> findNonAuthSourcesRoute = (request, response)
                -> authoritativeSourceService.findNonAuthSources(getEntityReference(request));

        ListRoute<AuthoritativeSource> findAuthSourcesRoute = (request, response)
                -> authoritativeSourceService.findAuthSources(getEntityReference(request));

        ListRoute<AuthoritativeSource> findAllRoute = (request, response)
                -> authoritativeSourceService.findAll();

        getForDatum(recalculateFlowRatingsPath, this::recalculateFlowRatingsRoute);
        getForDatum(cleanupOrphansPath, this::cleanupOrphansRoute);
        postForList(calculateConsumersForDataTypeIdSelectorPath, this::calculateConsumersForDataTypeIdSelectorRoute);
        getForList(findNonAuthSourcesPath, findNonAuthSourcesRoute);
        getForList(findByEntityReferencePath, findByEntityReferenceRoute);
        getForList(findByApplicationIdPath, findByApplicationIdRoute);
        getForList(findAuthSourcesPath, findAuthSourcesRoute);
        getForList(BASE_URL, findAllRoute);
        putForDatum(BASE_URL, this::updateRoute);
        deleteForDatum(deletePath, this::deleteRoute);
        postForDatum(BASE_URL, this::insertRoute);
    }


    private Integer cleanupOrphansRoute(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, Role.ADMIN);

        String username = getUsername(request);

        LOG.info("User: {}, requested auth source cleanup", username);
        return authoritativeSourceService.cleanupOrphans(username);
    }


    private String insertRoute(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, Role.AUTHORITATIVE_SOURCE_EDITOR);
        AuthoritativeSourceCreateCommand command = readBody(request, AuthoritativeSourceCreateCommand.class);
        authoritativeSourceService.insert(command, getUsername(request));
        return "done";
    }


    private String deleteRoute(Request request, Response response) {
        requireRole(userRoleService, request, Role.AUTHORITATIVE_SOURCE_EDITOR);
        long id = getId(request);
        authoritativeSourceService.remove(id, getUsername(request));

        return "done";
    }


    private String updateRoute(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, Role.AUTHORITATIVE_SOURCE_EDITOR);
        AuthoritativeSourceUpdateCommand command = readBody(request, AuthoritativeSourceUpdateCommand.class);
        authoritativeSourceService.update(command, getUsername(request));
        return "done";
    }


    private boolean recalculateFlowRatingsRoute(Request request, Response response) {
        requireRole(userRoleService, request, Role.ADMIN);

        String username = getUsername(request);
        LOG.info("Recalculating all flow ratings (requested by: {})", username);

        return authoritativeSourceService.recalculateAllFlowRatings();
    }


    private List<Entry<EntityReference, Collection<EntityReference>>> calculateConsumersForDataTypeIdSelectorRoute(
            Request request,
            Response response) throws IOException
    {
        IdSelectionOptions options = readIdSelectionOptionsFromBody(request);

        Map<EntityReference, Collection<EntityReference>> result = authoritativeSourceService
                .calculateConsumersForDataTypeIdSelector(options);

        return simplifyMapToList(result);
    }

}
