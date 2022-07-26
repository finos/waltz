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

import com.fasterxml.jackson.databind.JsonMappingException;
import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.SetAttributeCommand;
import org.finos.waltz.model.physical_flow.*;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.service.logical_flow.LogicalFlowService;
import org.finos.waltz.service.permission.permission_checker.FlowPermissionChecker;
import org.finos.waltz.service.physical_flow.PhysicalFlowService;
import org.finos.waltz.service.physical_flow.PhysicalFlowUploadService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class PhysicalFlowEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(PhysicalFlowEndpoint.class);
    private static final String BASE_URL = WebUtilities.mkPath("api", "physical-flow");
    private static final Pattern ERROR_PATTERN = Pattern.compile(".*required attributes.*\\[(.*)\\].*");

    private final PhysicalFlowService physicalFlowService;
    private final UserRoleService userRoleService;
    private final LogicalFlowService logicalFlowService;
    private final PhysicalFlowUploadService physicalFlowUploadService;
    private final FlowPermissionChecker flowPermissionChecker;


    @Autowired
    public PhysicalFlowEndpoint(PhysicalFlowService physicalFlowService,
                                PhysicalFlowUploadService physicalFlowUploadService,
                                LogicalFlowService logicalFlowService,
                                UserRoleService userRoleService,
                                FlowPermissionChecker flowPermissionChecker) {
        checkNotNull(physicalFlowService, "physicalFlowService cannot be null");
        checkNotNull(physicalFlowUploadService, "physicalFlowUploadService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");
        checkNotNull(logicalFlowService, "logicalFlowService cannot be null");
        checkNotNull(flowPermissionChecker, "flowPermissionService cannot be null");

        this.physicalFlowService = physicalFlowService;
        this.physicalFlowUploadService = physicalFlowUploadService;
        this.userRoleService = userRoleService;
        this.logicalFlowService = logicalFlowService;
        this.flowPermissionChecker = flowPermissionChecker;
    }


    @Override
    public void register() {
        String findByEntityRefPath = WebUtilities.mkPath(
                BASE_URL,
                "entity",
                ":kind",
                ":id");

        String findByProducerEntityRefPath = WebUtilities.mkPath(
                BASE_URL,
                "entity",
                ":kind",
                ":id",
                "produces");

        String findBySelectorPath = WebUtilities.mkPath(
                BASE_URL,
                "selector");

        String findByConsumerEntityRefPath = WebUtilities.mkPath(
                BASE_URL,
                "entity",
                ":kind",
                ":id",
                "consumes");

        String findBySpecificationIdPath = WebUtilities.mkPath(
                BASE_URL,
                "specification",
                ":id");

        String findByExternalIdPath = WebUtilities.mkPath(
                BASE_URL,
                "external-id",
                "*");

        String getByIdPath = WebUtilities.mkPath(
                BASE_URL,
                "id",
                ":id");

        String mergePath = WebUtilities.mkPath(
                BASE_URL,
                "merge",
                "from",
                ":fromId",
                "to",
                ":toId");

        String deletePath = WebUtilities.mkPath(
                BASE_URL,
                ":id");


        String findUnderlyingPhysicalFlowsPath = WebUtilities.mkPath(
                BASE_URL,
                "underlying",
                "logical-flow",
                ":flowId");

        String createPath = BASE_URL;

        String updateSpecDefinitionIdPath = WebUtilities.mkPath(
                BASE_URL,
                "id",
                ":id",
                "spec-definition");

        String updateAttributePath = WebUtilities.mkPath(
                BASE_URL,
                "id",
                ":id",
                "attribute");

        String validateUploadPath = WebUtilities.mkPath(
                BASE_URL,
                "upload",
                "validate");

        String uploadPath = WebUtilities.mkPath(
                BASE_URL,
                "upload");

        String cleanupOrphansPath = WebUtilities.mkPath(BASE_URL, "cleanup-orphans");


        ListRoute<PhysicalFlow> findByEntityRefRoute =
                (request, response) -> physicalFlowService.findByEntityReference(WebUtilities.getEntityReference(request));

        ListRoute<PhysicalFlow> findByProducerEntityRefRoute =
                (request, response) -> physicalFlowService.findByProducerEntityReference(WebUtilities.getEntityReference(request));

        ListRoute<PhysicalFlow> findByConsumerEntityRefRoute =
                (request, response) -> physicalFlowService.findByConsumerEntityReference(WebUtilities.getEntityReference(request));

        ListRoute<PhysicalFlow> findBySpecificationIdRoute =
                (request, response) -> physicalFlowService
                        .findBySpecificationId(
                                WebUtilities.getId(request));

        ListRoute<PhysicalFlow> findByExternalIdRoute =
                (request, response) -> physicalFlowService
                        .findByExternalId(request.splat()[0]);

        ListRoute<PhysicalFlow> findBySelectorRoute =
                (request, response) -> physicalFlowService
                        .findBySelector(WebUtilities.readIdSelectionOptionsFromBody(request));

        ListRoute<PhysicalFlowInfo> findUnderlyingPhysicalFlowsRoute =
                (request, response) -> physicalFlowService
                        .findUnderlyingPhysicalFlows(WebUtilities.getLong(request, "flowId"));

        DatumRoute<PhysicalFlow> getByIdRoute =
                (request, response) -> physicalFlowService
                        .getById(WebUtilities.getId(request));

        DatumRoute<Boolean> mergeRoute =
                (request, response) -> physicalFlowService
                        .merge(WebUtilities.getLong(request, "fromId"),
                                WebUtilities.getLong(request,"toId"),
                                WebUtilities.getUsername(request));

        EndpointUtilities.getForDatum(getByIdPath, getByIdRoute);
        EndpointUtilities.getForList(findByEntityRefPath, findByEntityRefRoute);
        EndpointUtilities.getForList(findByProducerEntityRefPath, findByProducerEntityRefRoute);
        EndpointUtilities.getForList(findByConsumerEntityRefPath, findByConsumerEntityRefRoute);
        EndpointUtilities.getForList(findBySpecificationIdPath, findBySpecificationIdRoute);
        EndpointUtilities.postForList(findBySelectorPath, findBySelectorRoute);
        EndpointUtilities.getForList(findByExternalIdPath, findByExternalIdRoute);
        EndpointUtilities.getForList(findUnderlyingPhysicalFlowsPath, findUnderlyingPhysicalFlowsRoute);

        EndpointUtilities.postForDatum(mergePath, mergeRoute);
        EndpointUtilities.postForDatum(createPath, this::createFlow);
        EndpointUtilities.postForDatum(updateSpecDefinitionIdPath, this::updateSpecDefinitionId);
        EndpointUtilities.postForDatum(updateAttributePath, this::updateAttribute);
        EndpointUtilities.postForDatum(validateUploadPath, this::validateUpload);
        EndpointUtilities.postForDatum(uploadPath, this::upload);

        EndpointUtilities.deleteForDatum(deletePath, this::deleteFlow);
        EndpointUtilities.getForDatum(cleanupOrphansPath, this::cleanupOrphansRoute);
    }


    private PhysicalFlowCreateCommandResponse createFlow(Request request, Response response) throws IOException, InsufficientPrivelegeException {
        String username = WebUtilities.getUsername(request);
        PhysicalFlowCreateCommand command = WebUtilities.readBody(request, PhysicalFlowCreateCommand.class);

        checkLogicalFlowPermission(EntityReference.mkRef(EntityKind.LOGICAL_DATA_FLOW, command.logicalFlowId()), username);

        return physicalFlowService.create(command, username);
    }


    private int updateSpecDefinitionId(Request request, Response response) throws IOException {
        WebUtilities.requireRole(userRoleService, request, SystemRole.LOGICAL_DATA_FLOW_EDITOR);
        String username = WebUtilities.getUsername(request);
        long flowId = WebUtilities.getId(request);

        PhysicalFlowSpecDefinitionChangeCommand command
                = WebUtilities.readBody(request, PhysicalFlowSpecDefinitionChangeCommand.class);

        return physicalFlowService.updateSpecDefinitionId(username, flowId, command);
    }


    private int updateAttribute(Request request, Response response) throws IOException, InsufficientPrivelegeException {
        String username = WebUtilities.getUsername(request);
        SetAttributeCommand command
                = WebUtilities.readBody(request, SetAttributeCommand.class);

        checkHasPermission(command.entityReference().id(), username);

        return physicalFlowService.updateAttribute(username, command);
    }


    private PhysicalFlowDeleteCommandResponse deleteFlow(Request request, Response response) throws InsufficientPrivelegeException {
        long flowId = WebUtilities.getId(request);
        String username = WebUtilities.getUsername(request);

        checkHasPermission(flowId, username);

        ImmutablePhysicalFlowDeleteCommand deleteCommand = ImmutablePhysicalFlowDeleteCommand.builder()
                .flowId(flowId)
                .build();

        return physicalFlowService.delete(deleteCommand, username);
    }


    private List<PhysicalFlowUploadCommandResponse> validateUpload(Request request, Response response) throws IOException {
        WebUtilities.requireRole(userRoleService, request, SystemRole.BULK_FLOW_EDITOR);
        try {
            List<PhysicalFlowUploadCommand> commands = Arrays.asList(WebUtilities.readBody(request, PhysicalFlowUploadCommand[].class));
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
        WebUtilities.requireRole(userRoleService, request, SystemRole.BULK_FLOW_EDITOR);
        List<PhysicalFlowUploadCommand> commands = Arrays.asList(WebUtilities.readBody(request, PhysicalFlowUploadCommand[].class));
        String username = WebUtilities.getUsername(request);

        return physicalFlowUploadService.upload(username, commands);
    }


    private Integer cleanupOrphansRoute(Request request, Response response) throws IOException {
        WebUtilities.requireRole(userRoleService, request, SystemRole.ADMIN);

        String username = WebUtilities.getUsername(request);

        LOG.info("User: {}, requested physical flow cleanup", username);
        return physicalFlowService.cleanupOrphans();
    }


    private void checkLogicalFlowPermission(EntityReference ref, String username) throws InsufficientPrivelegeException {
        Set<Operation> permissions = flowPermissionChecker.findPermissionsForFlow(ref.id(), username);
        flowPermissionChecker.verifyEditPerms(permissions, EntityKind.PHYSICAL_FLOW, username);
    }


    private void checkHasPermission(long flowId, String username) throws InsufficientPrivelegeException {
        PhysicalFlow physFlow = physicalFlowService.getById(flowId);
        checkLogicalFlowPermission(EntityReference.mkRef(EntityKind.LOGICAL_DATA_FLOW, physFlow.logicalFlowId()), username);
    }


}
