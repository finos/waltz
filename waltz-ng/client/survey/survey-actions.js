import {CORE_API} from "../common/services/core-api-utils";
import {displayError} from "../common/error-utils";

const statusActions = [
    {
        name: "Save",
        verb: "saved",
        severity: "info",
        helpText: "....",
        type: ["response"],
        view: "main.survey.instance.user",
    },{
        name: "Submit",
        verb: "submitted",
        severity: "success",
        helpText: "....",
        type: ["response"],
        validation: ["isFormValid"],
        view: "main.survey.instance.response.view",
    }, {
        name: "Approve",
        verb: "approved",
        type: ["instance"],
        severity: "success",
        helpText: "....",
    }, {
        name: "Withdraw",
        verb: "withdrawn",
        type: ["instance"],
        severity: "danger",
        helpText: "....",
        view: "main.survey.instance.response.view",
    }, {
        name: "Reject",
        verb: "rejected",
        type: ["instance"],
        severity: "danger",
        helpText: "....",
    }, {
        name: "Reopen",
        verb: "reopened",
        type: ["instance"],
        severity: "warning",
        helpText: "....",
        view: "main.survey.instance.response.edit",
    },
];


export function determineAvailableStatusActions(isLatest, possibleActions) {
    return statusActions
        .filter(act => possibleActions.some(pa => isLatest && pa.display === act.name))
        .map(act => {
            let possibleAction = possibleActions.find(pa => pa.display === act.name)
            act.actionName =  possibleAction.name;
            act.actionDisplay =  possibleAction.display;
            act.isCommentMandatory =  possibleAction.commentMandatory;
            act.isDisabled = (form) => form && form.$invalid && act.validation && act.validation.includes("isFormValid")
            return act;
        });
}

export function invokeStatusAction(serviceBroker, notification, reloader, $timeout, $state)  {
    return function(action, id) {
        const display = action.actionDisplay
        const verb = action.verb
        const name = action.actionName

        // SHOW MESSAGE
        const msg = `Are you sure you want to ${_.toLower(display)} this survey?`;
        const reason = action.isCommentMandatory
            ? prompt(msg + " Please enter a reason below (mandatory):", verb)
            : confirm(msg);

        // SEND API SERVER CALL
        const prom = reason
            ? serviceBroker
                .execute(
                    CORE_API.SurveyInstanceStore.updateStatus,
                    [id, {action: name, reason: reason}])
                .then(() => {
                    notification.success("Survey response " + verb + " successfully");
                })
                .catch(e => displayError("Failed to update survey status", e))
            : Promise.reject(display+ " cancelled")

        // PREPARE RENDERING
        return prom
            .then(() => reloader(true))
            .then(() => {
                if (action.view) {
                    $timeout(() => $state.go(action.view, {id: id}), 200);
                } else {
                    console.log("No view for " + name)
                }

            })
            .catch(e => displayError("Failed to render survey", e))
    }
}
