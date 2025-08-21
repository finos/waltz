package org.finos.waltz.web.endpoints.api;

import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommand;
import org.finos.waltz.model.utils.Approver;
import org.finos.waltz.model.utils.Checker;
import org.finos.waltz.service.maker_checker.MakerCheckerPermissionService;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Set;

import static org.finos.waltz.web.WebUtilities.mkPath;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForDatum;


@Service
public class MakerCheckerPermissionEndpoint implements Endpoint {
    private static final Logger LOG = LoggerFactory.getLogger(MakerCheckerPermissionEndpoint.class);
    private static final String BASE_URL = mkPath("api","mc");
    private final MakerCheckerPermissionService makerCheckerPermissionService;

    @Autowired
    public MakerCheckerPermissionEndpoint(MakerCheckerPermissionService makerCheckerPermissionService) {
        this.makerCheckerPermissionService = makerCheckerPermissionService;
    }
    @Override
    public void register() {
        getForDatum(mkPath(BASE_URL, "check-permission"), this::checkPermission);
        getForDatum(mkPath(BASE_URL, "checkUserPermission"), this::checkUserPermission);

    }
    public Set<Approver> checkPermission(Request request, Response response) throws IOException, InsufficientPrivelegeException {
        String username = WebUtilities.getUsername(request);
        ProposedFlowCommand proposedFlowCommand = WebUtilities.readBody(request, ProposedFlowCommand.class);
        return makerCheckerPermissionService.checkPermission(username, proposedFlowCommand.source(), proposedFlowCommand.target());
    }

    public Checker checkUserPermission(Request request, Response response) throws IOException, InsufficientPrivelegeException {
        String username = WebUtilities.getUsername(request);
        ProposedFlowCommand proposedFlowCommand = WebUtilities.readBody(request, ProposedFlowCommand.class);
        return makerCheckerPermissionService.checkUserPermission(username, proposedFlowCommand.source(), proposedFlowCommand.target());
    }


}