package org.finos.waltz.web.endpoints.api;

import org.finos.waltz.common.exception.FlowCreationException;
import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.proposed_flow.ProposeFlowPermission;
import org.finos.waltz.model.proposed_flow.ProposedFlowActionCommand;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommand;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommandResponse;
import org.finos.waltz.model.proposed_flow.ProposedFlowResponse;
import org.finos.waltz.service.proposed_flow_workflow.ProposedFlowWorkflowService;
import org.finos.waltz.service.workflow_state_machine.exception.TransitionNotFoundException;
import org.finos.waltz.service.workflow_state_machine.exception.TransitionPredicateFailedException;
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
import java.util.List;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.service.workflow_state_machine.proposed_flow.ProposedFlowWorkflowTransitionAction.PROPOSE;
import static org.finos.waltz.service.workflow_state_machine.proposed_flow.ProposedFlowWorkflowTransitionAction.findByVerb;
import static org.finos.waltz.web.WebUtilities.getEntityReference;
import static org.finos.waltz.web.WebUtilities.mkPath;
import static org.finos.waltz.web.WebUtilities.readBody;
import static org.finos.waltz.web.WebUtilities.readIdSelectionOptionsFromBody;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static org.finos.waltz.web.endpoints.EndpointUtilities.postForDatum;
import static org.finos.waltz.web.endpoints.EndpointUtilities.postForList;


@Service
public class ProposedFlowWorkflowEndpoint implements Endpoint {
    private static final Logger LOG = LoggerFactory.getLogger(ProposedFlowWorkflowEndpoint.class);
    private static final String BASE_URL = mkPath("api", "mc");
    private final ProposedFlowWorkflowService proposedFlowWorkflowService;

    @Autowired
    public ProposedFlowWorkflowEndpoint(ProposedFlowWorkflowService proposedFlowWorkflowService) {
        this.proposedFlowWorkflowService = proposedFlowWorkflowService;
    }

    @Override
    public void register() {
        // propose a new MC flow
        postForDatum(mkPath(BASE_URL, PROPOSE.getVerb()), this::proposeNewFlow);

        getForDatum(mkPath(BASE_URL, "propose-flow", "id", ":id"), this::getProposedFlowById);
        getForDatum(mkPath(BASE_URL, ":entityKind", ":entityId", "permissions", "user"), this::getUserPermissionsForEntityRef);

        postForDatum(mkPath(BASE_URL, ":id", ":action"), this::proposedFlowAction);

        postForList(mkPath(BASE_URL, "propose-flow"), this::findProposedFlows);
    }

    public ProposedFlowCommandResponse proposeNewFlow(Request request, Response response) throws IOException {
        String username = WebUtilities.getUsername(request);
        ProposedFlowCommand proposedFlowCommand = readBody(request, ProposedFlowCommand.class);
        return proposedFlowWorkflowService.proposeNewFlow(username, proposedFlowCommand);
    }

    public ProposedFlowResponse getProposedFlowById(Request request, Response response) {
        long proposedFlowId = WebUtilities.getLong(request, "id");
        return proposedFlowWorkflowService.getProposedFlowResponseById(proposedFlowId);
    }

    public List<ProposedFlowResponse> findProposedFlows(Request request, Response response) throws IOException {
        return proposedFlowWorkflowService.getProposedFlows(readIdSelectionOptionsFromBody(request));
    }

    public ProposedFlowResponse proposedFlowAction(Request request, Response response) throws IOException, FlowCreationException, TransitionNotFoundException, TransitionPredicateFailedException {
        String action = checkNotNull(request.params("action"), "Action not specified");
        ProposedFlowWorkflowTransitionAction proposedFlowAction = checkNotNull(findByVerb(action), "Invalid action");
        ProposedFlowActionCommand proposedFlowActionCommand = readBody(request, ProposedFlowActionCommand.class);

        return proposedFlowWorkflowService.proposedFlowAction(
                WebUtilities.getLong(request, "id"),
                proposedFlowAction,
                WebUtilities.getUsername(request),
                proposedFlowActionCommand);
    }

    public ProposeFlowPermission getUserPermissionsForEntityRef(Request request, Response response) throws IOException, InsufficientPrivelegeException {
        String username = WebUtilities.getUsername(request);
        EntityReference entityRef = getEntityReference(request, "entityKind", "entityId");
        return proposedFlowWorkflowService.getUserPermissionsForEntityRef(username, entityRef);
    }
}
