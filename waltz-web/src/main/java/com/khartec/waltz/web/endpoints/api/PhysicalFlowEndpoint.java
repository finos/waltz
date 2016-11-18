package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.physical_flow.*;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.physical_flow.PhysicalFlowService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class PhysicalFlowEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "physical-flow");


    private final PhysicalFlowService physicalFlowService;
    private final UserRoleService userRoleService;


    @Autowired
    public PhysicalFlowEndpoint(PhysicalFlowService physicalFlowService,
                                UserRoleService userRoleService) {
        checkNotNull(physicalFlowService, "physicalFlowService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.physicalFlowService = physicalFlowService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String findByEntityRefPath = mkPath(
                BASE_URL,
                "entity",
                ":kind",
                ":id");

        String findBySpecificationIdPath = mkPath(
                BASE_URL,
                "specification",
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


        ListRoute<PhysicalFlow> findByEntityRefRoute =
                (request, response) -> physicalFlowService
                        .findByEntityReference(
                            getEntityReference(request));

        ListRoute<PhysicalFlow> findBySpecificationIdRoute =
                (request, response) -> physicalFlowService
                        .findBySpecificationId(
                                getId(request));

        ListRoute<EntityReference> searchReportsRoute =
                (request, response) -> physicalFlowService
                        .searchReports(
                                request.params("query"));

        DatumRoute<PhysicalFlow> getByIdRoute =
                (request, response) -> physicalFlowService
                        .getById(getId(request));

        getForDatum(getByIdPath, getByIdRoute);
        getForList(findByEntityRefPath, findByEntityRefRoute);
        getForList(findBySpecificationIdPath, findBySpecificationIdRoute);
        getForList(searchReportsPath, searchReportsRoute);
        postForDatum(createPath, this::createFlow);

        deleteForDatum(deletePath, this::deleteFlow);
    }


    private PhysicalFlowCreateCommandResponse createFlow(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, Role.LOGICAL_DATA_FLOW_EDITOR);
        String username = getUsername(request);

        PhysicalFlowCreateCommand command = readBody(request, PhysicalFlowCreateCommand.class);
        PhysicalFlowCreateCommandResponse cmdResponse = physicalFlowService.create(command, username);

        return cmdResponse;
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

}
