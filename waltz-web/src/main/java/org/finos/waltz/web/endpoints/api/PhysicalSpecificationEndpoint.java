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

import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.SetAttributeCommand;
import org.finos.waltz.model.command.CommandResponse;
import org.finos.waltz.model.entity_search.ImmutableEntitySearchOptions;
import org.finos.waltz.model.physical_specification.ImmutablePhysicalSpecificationDeleteCommand;
import org.finos.waltz.model.physical_specification.PhysicalSpecification;
import org.finos.waltz.model.physical_specification.PhysicalSpecificationDeleteCommand;
import org.finos.waltz.service.permission.permission_checker.FlowPermissionChecker;
import org.finos.waltz.service.physical_specification.PhysicalSpecificationService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class PhysicalSpecificationEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "physical-specification");

    private final PhysicalSpecificationService specificationService;
    private final FlowPermissionChecker flowPermissionChecker;


    @Autowired
    public PhysicalSpecificationEndpoint(PhysicalSpecificationService specificationService, FlowPermissionChecker flowPermissionChecker) {
        checkNotNull(specificationService, "specificationService cannot be null");
        checkNotNull(flowPermissionChecker, "flowPermissionChecker cannot be null");

        this.specificationService = specificationService;
        this.flowPermissionChecker = flowPermissionChecker;
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

        String findByExternalIdPath = mkPath(
                BASE_URL,
                "external-id",
                ":externalId");

        String deletePath = mkPath(BASE_URL,
                ":id");

        String updateAttributePath = WebUtilities.mkPath(
                BASE_URL,
                "id",
                ":id",
                "attribute");


        String findPermissionsForSpecPath = WebUtilities.mkPath(
                BASE_URL,
                "id",
                ":id",
                "permissions");

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

        ListRoute<PhysicalSpecification> findByExternalIdRoute =
                (request, response) -> specificationService.findByExternalId(request.params("externalId"));

        ListRoute<Operation> findPermissionsForSpecRoute = (request, response) -> flowPermissionChecker.findPermissionsForSpec(
                getId(request),
                getUsername(request));

        postForList(findBySelectorPath, findBySelectorRoute);
        postForList(findByIdsPath, findByIdsRoute);

        getForList(findByAppPath, findByAppRoute);
        postForList(findByIdsPath, findByIdsRoute);
        postForList(searchPath, searchRoute);
        getForDatum(getByIdPath, getByIdRoute);
        getForList(findPermissionsForSpecPath, findPermissionsForSpecRoute);
        getForList(findByExternalIdPath, findByExternalIdRoute);
        postForDatum(updateAttributePath, this::updateAttribute);

        deleteForDatum(deletePath, this::deleteSpecification);
    }


    private CommandResponse<PhysicalSpecificationDeleteCommand> deleteSpecification(Request request, Response response) throws InsufficientPrivelegeException {

        long specId = getId(request);
        String username = getUsername(request);

        checkHasPermission(EntityReference.mkRef(EntityKind.PHYSICAL_SPECIFICATION, specId), username);

        ImmutablePhysicalSpecificationDeleteCommand deleteCommand = ImmutablePhysicalSpecificationDeleteCommand.builder()
                .specificationId(specId)
                .build();

        return specificationService.markRemovedIfUnused(deleteCommand, username);
    }


    private int updateAttribute(Request request, Response response) throws IOException, InsufficientPrivelegeException {
        String username = WebUtilities.getUsername(request);
        SetAttributeCommand command = WebUtilities.readBody(request, SetAttributeCommand.class);

        checkHasPermission(command.entityReference(), username);
        return specificationService.updateAttribute(username, command);
    }


    private void checkHasPermission(EntityReference ref, String username) throws InsufficientPrivelegeException {

        Set<Operation> permissions = flowPermissionChecker.findPermissionsForSpec(ref.id(), username);
        flowPermissionChecker.verifyEditPerms(permissions, EntityKind.PHYSICAL_SPECIFICATION, username);
    }

}
