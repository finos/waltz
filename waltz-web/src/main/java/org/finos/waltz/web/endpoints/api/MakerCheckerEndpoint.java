package org.finos.waltz.web.endpoints.api;

import org.finos.waltz.model.entity_workflow.EntityWorkflowTransition;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommand;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommandResponse;
import org.finos.waltz.service.makerchecker.MakerCheckerService;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.List;

import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class MakerCheckerEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(MakerCheckerEndpoint.class);
    private static final String BASE_URL = WebUtilities.mkPath("api", "mc");
    private final MakerCheckerService makerCheckerService;



    @Autowired
    public MakerCheckerEndpoint(MakerCheckerService makerCheckerService) {
        this.makerCheckerService = makerCheckerService;
    }


    @Override
    public void register() {
        // propose a new MC flow
        postForDatum(mkPath(BASE_URL, "proposed-flow"), this:: proposeNewFlow);
    }


    public ProposedFlowCommandResponse proposeNewFlow(Request request, Response response) throws IOException {
        String username = WebUtilities.getUsername(request);
        ProposedFlowCommand proposedFlowCommand = WebUtilities.readBody(request, ProposedFlowCommand.class);
        return makerCheckerService.proposedNewFlow(request.body(), username, proposedFlowCommand);
    }
}
