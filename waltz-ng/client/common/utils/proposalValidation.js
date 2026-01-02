import {PROPOSAL_OUTCOMES, PROPOSAL_TYPES} from "../constants";
import toasts from "../../svelte-stores/toast-store";
import {
    deleteFlowReason,
    duplicateProposeFlowMessage,
    editDataTypeReason,
    existingProposeFlowId
} from "../../data-flow/components/svelte/propose-data-flow/propose-data-flow-store";

export function handleProposalValidation(response, commandLaunched, resetStore, resetNeeded, goToWorkflow, type) {
    switch (response.outcome) {
        case PROPOSAL_OUTCOMES.FAILURE:
            duplicateProposeFlowMessage.set(response.message);
            if (response.proposedFlowId) {
                existingProposeFlowId.set("proposed-flow/" + response.proposedFlowId);
            } else if (response.physicalFlowId) {
                existingProposeFlowId.set("physical-flow/" + response.physicalFlowId);
            }
            break;

        case PROPOSAL_OUTCOMES.SUCCESS:
            if (response.proposedFlowId) {
                toasts.success("Data Flow Proposed");
                if (type === PROPOSAL_TYPES.DELETE) {
                    deleteFlowReason.set(null);
                }
                if (type === PROPOSAL_TYPES.EDIT) {
                    editDataTypeReason.set(null)
                }
                if (resetNeeded) {
                    resetStore();
                }
                setTimeout(goToWorkflow, 500, response.proposedFlowId);
            } else {
                toasts.error("Error proposing data flow");
                commandLaunched = false;
            }
            break;

        default:
            toasts.error("Error proposing data flow");
            commandLaunched = false;
            break;
            return commandLaunched;
    }
}