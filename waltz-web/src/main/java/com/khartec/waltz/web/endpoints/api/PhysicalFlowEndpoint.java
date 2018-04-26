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

import com.fasterxml.jackson.databind.JsonMappingException;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.SetAttributeCommand;
import com.khartec.waltz.model.physical_flow.*;
import com.khartec.waltz.model.user.Role;
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

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class PhysicalFlowEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(PhysicalFlowEndpoint.class);
    private static final String BASE_URL = mkPath("api", "physical-flow");

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

        String findByLogicalFlowIdPath = mkPath(
                BASE_URL,
                "logical-flow",
                ":id");

        String getByIdPath = mkPath(
                BASE_URL,
                "id",
                ":id");

        String deletePath = mkPath(
                BASE_URL,
                ":id"
                );

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

        ListRoute<PhysicalFlow> findByLogicalFlowIdRoute =
                (request, response) -> physicalFlowService
                        .findByLogicalFlowId(
                                getId(request));

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

        getForDatum(getByIdPath, getByIdRoute);
        getForList(findByEntityRefPath, findByEntityRefRoute);
        getForList(findByProducerEntityRefPath, findByProducerEntityRefRoute);
        getForList(findByConsumerEntityRefPath, findByConsumerEntityRefRoute);
        getForList(findBySpecificationIdPath, findBySpecificationIdRoute);
        postForList(findBySelectorPath, findBySelectorRoute);
        getForList(findByLogicalFlowIdPath, findByLogicalFlowIdRoute);
        getForList(searchReportsPath, searchReportsRoute);
        postForDatum(createPath, this::createFlow);
        postForDatum(updateSpecDefinitionIdPath, this::updateSpecDefinitionId);
        postForDatum(updateAttributePath, this::updateAttribute);
        postForDatum(validateUploadPath, this::validateUpload);
        postForDatum(uploadPath, this::upload);

        deleteForDatum(deletePath, this::deleteFlow);
        getForDatum(cleanupOrphansPath, this::cleanupOrphansRoute);
    }


    private PhysicalFlowCreateCommandResponse createFlow(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, Role.LOGICAL_DATA_FLOW_EDITOR);
        String username = getUsername(request);

        PhysicalFlowCreateCommand command = readBody(request, PhysicalFlowCreateCommand.class);
        PhysicalFlowCreateCommandResponse cmdResponse = physicalFlowService.create(command, username);

        return cmdResponse;
    }


    private int updateSpecDefinitionId(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, Role.LOGICAL_DATA_FLOW_EDITOR);
        String username = getUsername(request);
        long flowId = getId(request);

        PhysicalFlowSpecDefinitionChangeCommand command
                = readBody(request, PhysicalFlowSpecDefinitionChangeCommand.class);

        return physicalFlowService.updateSpecDefinitionId(username, flowId, command);
    }


    private int updateAttribute(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, Role.LOGICAL_DATA_FLOW_EDITOR);
        String username = getUsername(request);
        SetAttributeCommand command
                = readBody(request, SetAttributeCommand.class);

        return physicalFlowService.updateAttribute(username, command);
    }


    private PhysicalFlowDeleteCommandResponse deleteFlow(Request request, Response response) {
        requireRole(userRoleService, request, Role.LOGICAL_DATA_FLOW_EDITOR);

        long flowId = getId(request);
        String username = getUsername(request);

        ImmutablePhysicalFlowDeleteCommand deleteCommand = ImmutablePhysicalFlowDeleteCommand.builder()
                .flowId(flowId)
                .build();

        return physicalFlowService.delete(deleteCommand, username);
    }


    private List<PhysicalFlowUploadCommandResponse> validateUpload(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, Role.LOGICAL_DATA_FLOW_EDITOR);
        try {
            List<PhysicalFlowUploadCommand> commands = Arrays.asList(readBody(request, PhysicalFlowUploadCommand[].class));
            return physicalFlowUploadService.validate(commands);
        } catch (JsonMappingException ex) {
            Throwable cause = ex.getCause();
            if(cause != null) {
                throw new IOException(cause.getMessage(), ex);
            }
            throw ex;
        }
    }


    private List<PhysicalFlowUploadCommandResponse> upload(Request request, Response response) throws IOException, Exception {
        requireRole(userRoleService, request, Role.LOGICAL_DATA_FLOW_EDITOR);
        List<PhysicalFlowUploadCommand> commands = Arrays.asList(readBody(request, PhysicalFlowUploadCommand[].class));
        String username = getUsername(request);

        return physicalFlowUploadService.upload(username, commands);
    }


    private Integer cleanupOrphansRoute(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, Role.ADMIN);

        String username = getUsername(request);

        LOG.info("User: {}, requested physical flow cleanup", username);
        return physicalFlowService.cleanupOrphans();
    }

}
