package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.LastUpdate;
import com.khartec.waltz.model.command.CommandResponse;
import com.khartec.waltz.model.lineage_report.*;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.lineage_report.LineageReportService;
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
public class LineageReportEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "lineage-report");


    private final LineageReportService lineageReportService;
    private final UserRoleService userRoleService;


    @Autowired
    public LineageReportEndpoint(LineageReportService lineageReportService, UserRoleService userRoleService) {
        checkNotNull(lineageReportService, "lineageReportService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.lineageReportService = lineageReportService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String createPath = mkPath(
                BASE_URL,
                "update");


        String updatePath = mkPath(
                BASE_URL,
                "update");


        String getByIdPath = mkPath(
                BASE_URL,
                "id",
                ":id");

        String findByPhysicalSpecificationIdPath = mkPath(
                BASE_URL,
                "specification",
                ":id");

        String findReportsContributedToBySpecificationPath = mkPath(
                BASE_URL,
                "specification",
                ":id",
                "contributions");


        DatumRoute<LineageReport> getByIdRoute =
                (request, response) -> lineageReportService.getById(getId(request));

        ListRoute<LineageReport> findByPhysicalSpecificationIdRoute =
                (request, response) -> lineageReportService.findByPhysicalSpecificationId(getId(request));

        ListRoute<LineageReportDescriptor> findReportsContributedToBySpecificationRoute =
                (request, response) -> lineageReportService.findReportsContributedToBySpecification(getId(request));


        postForDatum(createPath, this::createRoute);
        putForDatum(createPath, this::updateRoute);
        getForDatum(getByIdPath, getByIdRoute);
        getForList(findByPhysicalSpecificationIdPath, findByPhysicalSpecificationIdRoute);
        getForList(findReportsContributedToBySpecificationPath, findReportsContributedToBySpecificationRoute);
    }


    private Long createRoute(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, Role.LOGICAL_DATA_FLOW_EDITOR);
        LineageReportCreateCommand cmd = readBody(request, LineageReportCreateCommand.class);
        return lineageReportService.create(cmd, getUsername(request));
    }


    private CommandResponse<LineageReportChangeCommand> updateRoute(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, Role.LOGICAL_DATA_FLOW_EDITOR);
        LineageReportChangeCommand cmd = ImmutableLineageReportChangeCommand
                .copyOf(readBody(request, LineageReportChangeCommand.class))
                .withLastUpdate(LastUpdate.mkForUser(getUsername(request)));

        return lineageReportService.update(cmd);
    }

}
