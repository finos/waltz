import _ from "lodash";
import {CORE_API} from "../../../common/services/core-api-utils";

const statusActions = [
    {
        name: "Save",
        severity: "info",
        helpText: "....",
        view: "main.survey.instance.user",
        predicate: (instance, permissions, isLatest) => isLatest,
        onPerform: (serviceBroker, instance, notification) => {
            /**
             * This is a bit of fakery as the questions are saved each time a response is updated.
             * Therefore this method merely moves the user back to their instance list.
             */
            return notification.success("Survey response saved successfully");
        }
    },{
        name: "Submit",
        severity: "success",
        helpText: "....",
        view: "main.survey.instance.response.view",
        predicate: (instance, permissions, isLatest) => isLatest
            && (permissions.admin || permissions.owner),
        onPerform: (serviceBroker, instance, notification) => {
            const reason = prompt("Are you sure you want to approve this survey? Please enter a reason below (optional):");
            if (!_.isNil(reason)) {
                return serviceBroker
                    .execute(
                        CORE_API.SurveyInstanceStore.updateStatus,
                        [ instance.id, {action: "SUBMITTING", reason: reason}])
                    .then(result => {
                        notification.success("Survey response submitted successfully");
                        // we force a reload of the notification store to update any listeners that the number
                        // of open surveys may have changed (i.e. the counter in the profile menu)
                        serviceBroker.loadAppData(
                            CORE_API.NotificationStore.findAll,
                            [],
                            {force: true});
                    });
            } else {
                return Promise.reject("Approval cancelled");
            }
        }
    }, {
        name: "Approve",
        severity: "success",
        helpText: "....",
        predicate: (instance, permissions, isLatest) => isLatest
            && (permissions.admin || permissions.owner)
            && _.isNil(instance.approvedAt),
        onPerform: (serviceBroker, instance, notification) => {
            const reason = prompt("Are you sure you want to approve this survey? Please enter a reason below (optional):");
            if (!_.isNil(reason)) {
                return serviceBroker
                    .execute(
                        CORE_API.SurveyInstanceStore.updateStatus,
                        [ instance.id, {action: "APPROVING", reason: reason}])
                    .then(result => {
                        notification.success("Survey response approved");
                    });
            } else {
                return Promise.reject("Approval cancelled");
            }
        }
    }, {
        name: "Withdraw",
        severity: "danger",
        helpText: "....",
        predicate: (instance, permissions, isLatest) => isLatest
            && (permissions.admin || permissions.owner),
        onPerform: (serviceBroker, instance, notification) => {
            if (confirm("Are you sure you want to withdraw this survey instance? ")) {
                return serviceBroker
                    .execute(
                        CORE_API.SurveyInstanceStore.updateStatus,
                        [instance.id, {action: "WITHDRAWING"} ])
                    .then(() => {
                        notification.success("Survey instance withdrawn");
                    });
            } else {
                Promise.reject("Withdraw cancelled");
            }
        }
    }, {
        name: "Reject",
        severity: "danger",
        helpText: "....",
        predicate: (instance, permissions, isLatest) => isLatest
            && (permissions.admin || permissions.owner),
        onPerform: (serviceBroker, instance, notification) => {
            const reason = prompt("Are you sure you want reject this survey? Please enter a reason below (mandatory):");
            if (reason) {
                return serviceBroker
                    .execute(
                        CORE_API.SurveyInstanceStore.updateStatus,
                        [instance.id, {action: "REJECTING", reason: reason}])
                    .then(() => {
                        notification.success("Survey response rejected");
                    });
            } else {
                return Promise.reject("Reject cancelled");
            }
        }
    }, {
        name: "Reopen",
        severity: "warning",
        helpText: "....",
        view: "main.survey.instance.response.edit",
        predicate: (instance, permissions, isLatest) => isLatest
            && (permissions.admin || permissions.owner || permissions.participant),
        onPerform: (serviceBroker, instance, notification) => {
            if (confirm("Are you sure you want reopen this survey?")) {
                return serviceBroker
                    .execute(
                        CORE_API.SurveyInstanceStore.updateStatus,
                        [instance.id, {action: "REOPENING"}])
                    .then(() => {
                        notification.success("Survey response reopened");
                    });
            } else {
                return Promise.reject("Reopen cancelled");
            }
        }
    },
];


export function determineAvailableStatusActions(surveyInstance, permissions, isLatest, possibleActions) {
    return statusActions
        .filter(act => possibleActions.includes(act.name))
        .filter(act => act.predicate(surveyInstance, permissions,isLatest));
}
