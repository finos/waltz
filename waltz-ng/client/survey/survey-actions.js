import {CORE_API} from "../common/services/core-api-utils";

const statusActions = [
    {
        name: "Save",
        verb: "saved",
        severity: "info",
        helpText: "....",
        view: "main.survey.instance.user",
    },{
        name: "Submit",
        verb: "submitted",
        severity: "success",
        helpText: "....",
        view: "main.survey.instance.response.view",
    }, {
        name: "Approve",
        verb: "approved",
        severity: "success",
        helpText: "....",
    }, {
        name: "Withdraw",
        verb: "withdrawn",
        severity: "danger",
        helpText: "....",
        view: "main.survey.instance.response.view",
    }, {
        name: "Reject",
        verb: "rejected",
        severity: "danger",
        helpText: "....",
    }, {
        name: "Reopen",
        verb: "reopened",
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
            return act;
        });
}

export function invokeStatusAction(serviceBroker, notification, reloader, $timeout, $state)  {
    console.log("building invokeStatus")
    return function(action, id) {
        const display = action.actionDisplay
        const verb = action.verb
        const name = action.actionName
        console.log("Doing " + name + " on " + id)
        
        // SHOW MESSAGE
        const msg = "Are you sure you want " + display + " this survey?"
        const reason = action.isCommentMandatory
            ? prompt(msg + " Please enter a reason below (mandatory):")
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
            .catch(err => notification.warning(err))
    }
};
