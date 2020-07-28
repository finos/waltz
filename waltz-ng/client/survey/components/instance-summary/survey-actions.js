import _ from "lodash";
import {CORE_API} from "../../../common/services/core-api-utils";

const statusActions = [
    {
        name: "Approve",
        severity: "success",
        helpText: "....",
        predicate: (instance, permissions, isLatest) => isLatest
            && (permissions.admin || permissions.owner)
            && instance.status === "COMPLETED"
            && _.isNil(instance.approvedAt),
        onPerform: (serviceBroker, instance, notification) => {
            const reason = prompt("Are you sure you want to approve this survey? Please enter a reason below (optional):");
            if (!_.isNil(reason)) {
                return serviceBroker
                    .execute(
                        CORE_API.SurveyInstanceStore.markApproved,
                        [ instance.id, {newStringVal: _.isEmpty(reason) ? null : reason}])
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
            && (permissions.admin || permissions.owner)
            && instance.status !== "WITHDRAWN"
            && instance.status !== "APPROVED"
            && instance.status !== "COMPLETED",
        onPerform: (serviceBroker, instance, notification) => {
            if (confirm("Are you sure you want to withdraw this survey instance? ")) {
                return serviceBroker
                    .execute(
                        CORE_API.SurveyInstanceStore.updateStatus,
                        [instance.id, {newStatus: "WITHDRAWN"} ])
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
            && (permissions.admin || permissions.owner)
            && instance.status === "COMPLETED",
        onPerform: (serviceBroker, instance, notification) => {
            const reason = prompt("Are you sure you want reject this survey? Please enter a reason below (mandatory):");
            if (reason) {
                return serviceBroker
                    .execute(
                        CORE_API.SurveyInstanceStore.updateStatus,
                        [instance.id, {newStatus: "REJECTED", reason}])
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
        predicate: (instance, permissions, isLatest) => isLatest
            && (permissions.admin || permissions.owner || permissions.participant)
            && ( instance.status === "WITHDRAWN" || instance.status === "REJECTED" || instance.status === "APPROVED"),
        onPerform: (serviceBroker, instance, notification) => {
            const confirmation = confirm("Are you sure you want reopen this survey?");
            if (confirmation) {
                return serviceBroker
                    .execute(
                        CORE_API.SurveyInstanceStore.updateStatus,
                        [instance.id, {newStatus: "IN_PROGRESS"}])
                    .then(() => {
                        notification.success("Survey response reopened");
                    });
            } else {
                return Promise.reject("Reopen cancelled");
            }
        }
    },
];


export function determineAvailableStatusActions(surveyInstance, permissions, isLatest) {
    return _.filter(
        statusActions,
        act => act.predicate(
            surveyInstance,
            permissions,
            isLatest));
}
