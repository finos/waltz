package org.finos.waltz.web.endpoints.api;

import org.finos.waltz.model.proposed_flow.ProposedFlowActionCommand;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommand;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommandResponse;
import org.finos.waltz.model.proposed_flow.ProposedFlowResponse;
import org.finos.waltz.service.maker_checker.MakerCheckerService;
import org.finos.waltz.service.workflow_state_machine.proposed_flow.ProposedFlowWorkflowTransitionAction;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.service.workflow_state_machine.proposed_flow.ProposedFlowWorkflowTransitionAction.PROPOSE;
import static org.finos.waltz.service.workflow_state_machine.proposed_flow.ProposedFlowWorkflowTransitionAction.findByVerb;
import static org.finos.waltz.web.WebUtilities.mkPath;
import static org.finos.waltz.web.WebUtilities.readBody;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static org.finos.waltz.web.endpoints.EndpointUtilities.postForDatum;


@Service
public class MakerCheckerEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(MakerCheckerEndpoint.class);
    private static final String BASE_URL = mkPath("api", "mc");
    private final MakerCheckerService makerCheckerService;


    @Autowired
    public MakerCheckerEndpoint(MakerCheckerService makerCheckerService) {
        this.makerCheckerService = makerCheckerService;
    }


    @Override
    public void register() {
        // propose a new MC flow
        //TODO.. we will let this action "propose" come from UI
        postForDatum(mkPath(BASE_URL, PROPOSE.getVerb()), this::proposeNewFlow);

        getForDatum(mkPath(BASE_URL, "propose-flow", "id", ":id"), this::getProposedFlowById);

        postForDatum(mkPath(BASE_URL, ":id", ":action"), this::proposedFlowAction);
    }

    public ProposedFlowCommandResponse proposeNewFlow(Request request, Response response) throws IOException {
        String username = WebUtilities.getUsername(request);
        ProposedFlowCommand proposedFlowCommand = readBody(request, ProposedFlowCommand.class);
        return makerCheckerService.proposeNewFlow(request.body(), username, proposedFlowCommand);
    }

    public ProposedFlowResponse getProposedFlowById(Request request, Response response) {
        long proposedFlowId = WebUtilities.getLong(request, "id");
        return makerCheckerService.getProposedFlowById(proposedFlowId);
    }

    public ProposedFlowResponse proposedFlowAction(Request request, Response response) throws IOException {
        String action = checkNotNull(request.params("action"), "Action not specified");
        ProposedFlowWorkflowTransitionAction proposedFlowAction = checkNotNull(findByVerb(action), "Invalid action");
        ProposedFlowActionCommand proposedFlowActionCommand = readBody(request, ProposedFlowActionCommand.class);

        return makerCheckerService.proposedFlowAction(
                WebUtilities.getLong(request, "id"),
                proposedFlowAction,
                WebUtilities.getUsername(request),
                proposedFlowActionCommand);
    }
}
