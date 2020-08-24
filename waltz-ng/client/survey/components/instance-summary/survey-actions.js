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
