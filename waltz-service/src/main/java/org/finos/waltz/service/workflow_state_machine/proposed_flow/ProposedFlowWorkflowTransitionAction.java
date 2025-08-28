package org.finos.waltz.service.workflow_state_machine.proposed_flow;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ProposedFlowWorkflowTransitionAction {

    //TODO.. Remove verb?
    PROPOSE("Propose", "propose"),
    APPROVE("Approve", "approve"),
    REJECT("Reject", "reject");

    private final String display;
    private final String verb;

    ProposedFlowWorkflowTransitionAction(String display,
                                         String verb) {
        this.display = display;
        this.verb = verb;
    }

    public String getDisplay() {
        return display;
    }

    public String getName() {
        return name();
    }

    public static ProposedFlowWorkflowTransitionAction findByDisplay(String display) {
        for (ProposedFlowWorkflowTransitionAction action : ProposedFlowWorkflowTransitionAction.values()) {
            if (action.display.equalsIgnoreCase(display)) {
                return action;
            }
        }
        return null;
    }

    public static ProposedFlowWorkflowTransitionAction findByVerb(String verb) {
        if (verb == null) {
            return null;
        }
        for (ProposedFlowWorkflowTransitionAction action : ProposedFlowWorkflowTransitionAction.values()) {
            if (action.verb.equalsIgnoreCase(verb)) {
                return action;
            }
        }
        return null;
    }

    public String getVerb() {
        return verb;
    }
}