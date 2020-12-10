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

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.command.CommandResponse;
import com.khartec.waltz.model.entity_search.ImmutableEntitySearchOptions;
import com.khartec.waltz.model.physical_specification.ImmutablePhysicalSpecificationDeleteCommand;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import com.khartec.waltz.model.physical_specification.PhysicalSpecificationDeleteCommand;
import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.service.physical_specification.PhysicalSpecificationService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class PhysicalSpecificationEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "physical-specification");

    private final PhysicalSpecificationService specificationService;
    private final UserRoleService userRoleService;


    @Autowired
    public PhysicalSpecificationEndpoint(PhysicalSpecificationService specificationService, UserRoleService userRoleService) {
        checkNotNull(specificationService, "specificationService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.specificationService = specificationService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String findByAppPath = mkPath(
                BASE_URL,
                "application",
                ":kind",
                ":id");

        String findBySelectorPath = mkPath(
                BASE_URL,
                "selector");

        String findByIdsPath = mkPath(
                BASE_URL,
                "ids");

        String searchPath = mkPath(
                BASE_URL,
                "search");

        String getByIdPath = mkPath(
                BASE_URL,
                "id",
                ":id");

        String deletePath = mkPath(BASE_URL,
                ":id");

        ListRoute<PhysicalSpecification> findBySelectorRoute =
                (request, response) -> specificationService.findBySelector(readIdSelectionOptionsFromBody(request));

        ListRoute<PhysicalSpecification> findByAppRoute =
                (request, response) -> specificationService.findByEntityReference(getEntityReference(request));


        ListRoute<PhysicalSpecification> findByIdsRoute =
                (request, response) -> specificationService.findByIds(readIdsFromBody(request));

        ListRoute<PhysicalSpecification> searchRoute =
                (request, response) -> specificationService.search(
                        ImmutableEntitySearchOptions.builder()
                                .userId(getUsername(request))
                                .searchQuery(readBody(request, String.class))
                                .build());

        DatumRoute<PhysicalSpecification> getByIdRoute =
                (request, response) -> specificationService.getById(getId(request));

        postForList(findBySelectorPath, findBySelectorRoute);
        postForList(findByIdsPath, findByIdsRoute);

        getForList(findByAppPath, findByAppRoute);
        postForList(findByIdsPath, findByIdsRoute);
        postForList(searchPath, searchRoute);
        getForDatum(getByIdPath, getByIdRoute);

        deleteForDatum(deletePath, this::deleteSpecification);
    }


    private CommandResponse<PhysicalSpecificationDeleteCommand> deleteSpecification(Request request, Response response) {
        requireRole(userRoleService, request, SystemRole.LOGICAL_DATA_FLOW_EDITOR);

        long specId = getId(request);
        String username = getUsername(request);

        ImmutablePhysicalSpecificationDeleteCommand deleteCommand = ImmutablePhysicalSpecificationDeleteCommand.builder()
                .specificationId(specId)
                .build();

        return specificationService.markRemovedIfUnused(deleteCommand, username);
    }
}
