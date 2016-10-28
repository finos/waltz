package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.command.CommandOutcome;
import com.khartec.waltz.model.physical_flow.ImmutablePhysicalFlowDeleteCommand;
import com.khartec.waltz.model.physical_flow.PhysicalFlow;
import com.khartec.waltz.model.physical_flow.PhysicalFlowDeleteCommandResponse;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.physical_flow.PhysicalFlowService;
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
public class PhysicalFlowEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "physical-flow");


    private final ChangeLogService changeLogService;
    private final PhysicalFlowService physicalFlowService;
    private final PhysicalSpecificationService physicalSpecificationService;
    private final UserRoleService userRoleService;


    @Autowired
    public PhysicalFlowEndpoint(ChangeLogService changeLogService,
                                PhysicalFlowService physicalFlowService,
                                PhysicalSpecificationService physicalSpecificationService,
                                UserRoleService userRoleService) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(physicalFlowService, "physicalFlowService cannot be null");
        checkNotNull(physicalSpecificationService, "physicalSpecificationService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.changeLogService = changeLogService;
        this.physicalFlowService = physicalFlowService;
        this.physicalSpecificationService = physicalSpecificationService;
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

        String deletePath = mkPath(BASE_URL,
                ":id"
                );

        ListRoute<PhysicalFlow> findByEntityRefRoute =
                (request, response) -> physicalFlowService
                        .findByEntityReference(
                            getEntityReference(request));

        ListRoute<PhysicalFlow> findBySpecificationIdRoute =
                (request, response) -> physicalFlowService
                        .findBySpecificationId(
                                getId(request));

        DatumRoute<PhysicalFlow> getByIdRoute =
                (request, response) -> physicalFlowService
                        .getById(getId(request));

        getForDatum(getByIdPath, getByIdRoute);
        getForList(findByEntityRefPath, findByEntityRefRoute);
        getForList(findBySpecificationIdPath, findBySpecificationIdRoute);

        deleteForDatum(deletePath, this::deleteFlow);
    }


    private PhysicalFlowDeleteCommandResponse deleteFlow(Request request, Response response) {
        requireRole(userRoleService, request, Role.LOGICAL_DATA_FLOW_EDITOR);

        long flowId = getId(request);
        String username = getUsername(request);
        PhysicalFlow flow = physicalFlowService.getById(flowId);

        ImmutablePhysicalFlowDeleteCommand deleteCommand = ImmutablePhysicalFlowDeleteCommand.builder()
                .flowId(flowId)
                .build();

        PhysicalFlowDeleteCommandResponse deleteCommandResponse = physicalFlowService.delete(deleteCommand);

        // log changes against source and target entities
        if (deleteCommandResponse.outcome() == CommandOutcome.SUCCESS) {
            PhysicalSpecification specification = physicalSpecificationService.getById(flow.specificationId());

            logChange(username,
                    specification.owningEntity(),
                    "physical flow id: " + flowId + " to " + flow.target().name().get() + " deleted");
            logChange(username,
                    flow.target(),
                    "physical flow id: " + flowId + " from " + specification.owningEntity().name().get() + " deleted");
        }

        return deleteCommandResponse;
    }


    private void logChange(String userId,
                           EntityReference ref,
                           String message) {

        ChangeLog logEntry = ImmutableChangeLog.builder()
                .parentReference(ref)
                .message(message)
                .severity(Severity.INFORMATION)
                .userId(userId)
                .build();

        changeLogService.write(logEntry);
    }
}
