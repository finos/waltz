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

import com.fasterxml.jackson.databind.JsonMappingException;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.SetAttributeCommand;
import com.khartec.waltz.model.physical_flow.*;
import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.service.physical_flow.PhysicalFlowService;
import com.khartec.waltz.service.physical_flow.PhysicalFlowUploadService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;
import static java.util.Arrays.asList;

@Service
public class PhysicalFlowEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(PhysicalFlowEndpoint.class);
    private static final String BASE_URL = mkPath("api", "physical-flow");
    private static final Pattern ERROR_PATTERN = Pattern.compile(".*required attributes.*\\[(.*)\\].*");

    private final PhysicalFlowService physicalFlowService;
    private final UserRoleService userRoleService;
    private final PhysicalFlowUploadService physicalFlowUploadService;


    @Autowired
    public PhysicalFlowEndpoint(PhysicalFlowService physicalFlowService,
                                PhysicalFlowUploadService physicalFlowUploadService,
                                UserRoleService userRoleService) {
        checkNotNull(physicalFlowService, "physicalFlowService cannot be null");
        checkNotNull(physicalFlowUploadService, "physicalFlowUploadService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.physicalFlowService = physicalFlowService;
        this.physicalFlowUploadService = physicalFlowUploadService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String findByEntityRefPath = mkPath(
                BASE_URL,
                "entity",
                ":kind",
                ":id");

        String findByProducerEntityRefPath = mkPath(
                BASE_URL,
                "entity",
                ":kind",
                ":id",
                "produces");

        String findBySelectorPath = mkPath(
                BASE_URL,
                "selector");

        String findByConsumerEntityRefPath = mkPath(
                BASE_URL,
                "entity",
                ":kind",
                ":id",
                "consumes");

        String findBySpecificationIdPath = mkPath(
                BASE_URL,
                "specification",
                ":id");

        String findByExternalIdPath = mkPath(
                BASE_URL,
                "external-id",
                "*");

        String getByIdPath = mkPath(
                BASE_URL,
                "id",
                ":id");

        String mergePath = mkPath(
                BASE_URL,
                "merge",
                "from",
                ":fromId",
                "to",
                ":toId");

        String deletePath = mkPath(
                BASE_URL,
                ":id");

        String searchReportsPath = mkPath(
                BASE_URL,
                "search-reports",
                ":query");

        String createPath = BASE_URL;

        String updateSpecDefinitionIdPath = mkPath(
                BASE_URL,
                "id",
                ":id",
                "spec-definition");

        String updateAttributePath = mkPath(
                BASE_URL,
                "id",
                ":id",
                "attribute");

        String validateUploadPath = mkPath(
                BASE_URL,
                "upload",
                "validate");

        String uploadPath = mkPath(
                BASE_URL,
                "upload");

        String cleanupOrphansPath = mkPath(BASE_URL, "cleanup-orphans");


        ListRoute<PhysicalFlow> findByEntityRefRoute =
                (request, response) -> physicalFlowService.findByEntityReference(getEntityReference(request));

        ListRoute<PhysicalFlow> findByProducerEntityRefRoute =
                (request, response) -> physicalFlowService.findByProducerEntityReference(getEntityReference(request));

        ListRoute<PhysicalFlow> findByConsumerEntityRefRoute =
                (request, response) -> physicalFlowService.findByConsumerEntityReference(getEntityReference(request));

        ListRoute<PhysicalFlow> findBySpecificationIdRoute =
                (request, response) -> physicalFlowService
                        .findBySpecificationId(
                                getId(request));

        ListRoute<PhysicalFlow> findByExternalIdRoute =
                (request, response) -> physicalFlowService
                        .findByExternalId(request.splat()[0]);

        ListRoute<PhysicalFlow> findBySelectorRoute =
                (request, response) -> physicalFlowService
                        .findBySelector(readIdSelectionOptionsFromBody(request));

        ListRoute<EntityReference> searchReportsRoute =
                (request, response) -> physicalFlowService
                        .searchReports(
                                request.params("query"));

        DatumRoute<PhysicalFlow> getByIdRoute =
                (request, response) -> physicalFlowService
                        .getById(getId(request));

        DatumRoute<Boolean> mergeRoute =
                (request, response) -> physicalFlowService
                        .merge(getLong(request, "fromId"),
                                getLong(request,"toId"),
                                getUsername(request));

        getForDatum(getByIdPath, getByIdRoute);
        getForList(findByEntityRefPath, findByEntityRefRoute);
        getForList(findByProducerEntityRefPath, findByProducerEntityRefRoute);
        getForList(findByConsumerEntityRefPath, findByConsumerEntityRefRoute);
        getForList(findBySpecificationIdPath, findBySpecificationIdRoute);
        postForList(findBySelectorPath, findBySelectorRoute);
        getForList(findByExternalIdPath, findByExternalIdRoute);
        getForList(searchReportsPath, searchReportsRoute);


        postForDatum(mergePath, mergeRoute);
        postForDatum(createPath, this::createFlow);
        postForDatum(updateSpecDefinitionIdPath, this::updateSpecDefinitionId);
        postForDatum(updateAttributePath, this::updateAttribute);
        postForDatum(validateUploadPath, this::validateUpload);
        postForDatum(uploadPath, this::upload);

        deleteForDatum(deletePath, this::deleteFlow);
        getForDatum(cleanupOrphansPath, this::cleanupOrphansRoute);
    }


    private PhysicalFlowCreateCommandResponse createFlow(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, SystemRole.LOGICAL_DATA_FLOW_EDITOR);
        String username = getUsername(request);

        PhysicalFlowCreateCommand command = readBody(request, PhysicalFlowCreateCommand.class);
        return physicalFlowService.create(command, username);
    }


    private int updateSpecDefinitionId(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, SystemRole.LOGICAL_DATA_FLOW_EDITOR);
        String username = getUsername(request);
        long flowId = getId(request);

        PhysicalFlowSpecDefinitionChangeCommand command
                = readBody(request, PhysicalFlowSpecDefinitionChangeCommand.class);

        return physicalFlowService.updateSpecDefinitionId(username, flowId, command);
    }


    private int updateAttribute(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, SystemRole.LOGICAL_DATA_FLOW_EDITOR);
        String username = getUsername(request);
        SetAttributeCommand command
                = readBody(request, SetAttributeCommand.class);

        return physicalFlowService.updateAttribute(username, command);
    }


    private PhysicalFlowDeleteCommandResponse deleteFlow(Request request, Response response) {
        requireRole(userRoleService, request, SystemRole.LOGICAL_DATA_FLOW_EDITOR);

        long flowId = getId(request);
        String username = getUsername(request);

        ImmutablePhysicalFlowDeleteCommand deleteCommand = ImmutablePhysicalFlowDeleteCommand.builder()
                .flowId(flowId)
                .build();

        return physicalFlowService.delete(deleteCommand, username);
    }


    private List<PhysicalFlowUploadCommandResponse> validateUpload(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, SystemRole.LOGICAL_DATA_FLOW_EDITOR);
        try {
            List<PhysicalFlowUploadCommand> commands = Arrays.asList(readBody(request, PhysicalFlowUploadCommand[].class));
            return physicalFlowUploadService.validate(commands);
        } catch (JsonMappingException ex) {
            Throwable cause = ex.getCause();
            if(cause != null) {

                String message = cause.getMessage();

                Matcher match = ERROR_PATTERN.matcher(message);

                String errorMsg = (match.find())
                        ? String.format("Cannot resolve physical flows as the required attributes are missing [%s]", match.group(1))
                        : message;

                int lineNr = ex.getPath().get(0).getIndex() + 1;

                throw new IOException(String.format("%s in line %d",
                        errorMsg,
                        lineNr),
                        cause);
            }
            throw ex;
        }
    }


    private List<PhysicalFlowUploadCommandResponse> upload(Request request, Response response) throws IOException, Exception {
        requireRole(userRoleService, request, SystemRole.LOGICAL_DATA_FLOW_EDITOR);
        List<PhysicalFlowUploadCommand> commands = asList(readBody(request, PhysicalFlowUploadCommand[].class));
        String username = getUsername(request);

        return physicalFlowUploadService.upload(username, commands);
    }


    private Integer cleanupOrphansRoute(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, SystemRole.ADMIN);

        String username = getUsername(request);

        LOG.info("User: {}, requested physical flow cleanup", username);
        return physicalFlowService.cleanupOrphans();
    }

}
