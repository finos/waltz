/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

import {initialiseData} from "../../../common";
import template from "./survey-instance-summary.html";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import {timeFormat} from "d3-time-format";
import {loadEntity, sameRef} from "../../../common/entity-utils";
import {displayError} from "../../../common/error-utils";
import roles from "../../../user/system-roles";
import {mkDescription} from "../../survey-utils";


const bindings = {
    instanceId: "<"
};


const initialState = {};

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
                return Promise.reject();
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
                Promise.reject();
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
                Promise.reject();
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
                Promise.reject();
            }
        }
    },
];


function mkUpdateRecipientCommand(instanceRecipientId, person, surveyInstanceId) {
    return {
        instanceRecipientId,
        surveyInstanceId,
        personId: person.id,
    }
}


function mkCreateRecipientCommand(instanceId, person) {
    return {
        surveyInstanceId: instanceId,
        personId: person.id
    };
}


function findMatchingRecipient(recipients = [], person) {
    return _.find(recipients, r => sameRef(r.person, person))
}


function controller($state, serviceBroker, userService, notification) {
    const vm = initialiseData(this, initialState);


    function loadRoles() {
        serviceBroker.loadAppData(CORE_API.RoleStore.findAllRoles)
            .then(r => {
                const rolesByKey = _.keyBy(r.data, d => d.key);
                vm.owningRole = rolesByKey[vm.surveyInstance.owningRole];
            });
    }

    function reload(force = false) {
        loadInstanceAndRun(force)
            .then(loadSubject)
            .then(loadOwner)
            .then(loadRoles)
            .then(() => loadRecipients(force))
            .then(() => loadPreviousVersions(force))
            .then(loadUser)
            .then(determineAvailableStatusActions);
    }

    function determineAvailableStatusActions() {
        vm.availableStatusActions = _.filter(
            statusActions,
            act => act.predicate(
                vm.surveyInstance,
                vm.permissions,
                vm.currentResponseVersion.isLatest));
    }

    function loadUser() {
        userService
            .whoami()
            .then(u => {
                const isOwner = vm.owner.userId === u.userName;
                const isParticipant = _.some(vm.people, p => p.userId === u.userName);
                const hasOwningRole = _.includes(u.roles, vm.surveyInstance.owningRole);
                const isAdmin = userService.hasRole(u, roles.SURVEY_ADMIN);
                vm.permissions = {
                    admin: isAdmin,
                    owner: isOwner || hasOwningRole,
                    participant: isParticipant,
                    metaEdit: vm.currentResponseVersion.isLatest && (isOwner || isAdmin)
                };
            });
    }

    function loadPreviousVersions(force) {
        const latestInstanceId = vm.surveyInstance.originalInstanceId || vm.surveyInstance.id;
        return serviceBroker
            .loadViewData(
                CORE_API.SurveyInstanceStore.findPreviousVersions,
                [ latestInstanceId ],
                { force })
            .then(r => {
                const prevVersions = _.chain(r.data)
                    .sortBy("submittedAt")
                    .map((pv, i) => ({
                        versionNum: `${i + 1}.0`,
                        instanceId: pv.id,
                        isLatest: false
                    }))
                    .reverse()
                    .value();

                const latestResponseVersion = {
                    versionNum: `${prevVersions.length + 1}.0`,
                    instanceId: latestInstanceId,
                    isLatest: true
                };

                const allResponseVersions = [latestResponseVersion].concat(prevVersions);
                vm.currentResponseVersion = _.keyBy(allResponseVersions, "instanceId")[vm.surveyInstance.id];
                vm.otherResponseVersions = _.filter(
                    allResponseVersions,
                    rv => rv.instanceId !== vm.currentResponseVersion.instanceId);
            });
    }

    function loadOwner() {
        return serviceBroker
            .loadViewData(
                CORE_API.PersonStore.getById,
                [ vm.surveyRun.ownerId ])
            .then(r => {
                vm.owner = r.data;
            });
    }

    function loadSubject() {
        return loadEntity(serviceBroker, vm.surveyInstance.surveyEntity)
            .then(entity => vm.subject = entity);
    }

    function loadInstanceAndRun(force = false) {
        return serviceBroker
            .loadViewData(
                CORE_API.SurveyInstanceStore.getById,
                [vm.instanceId],
                { force })
            .then(r => {
                vm.surveyInstance = r.data;
                const runId = vm.surveyInstance.surveyRunId;
                return serviceBroker
                    .loadViewData(CORE_API.SurveyRunStore.getById, [runId])
                    .then(r => vm.surveyRun = r.data);
            }).then(() => serviceBroker
                .loadViewData(CORE_API.SurveyTemplateStore.getById, [vm.surveyRun.surveyTemplateId])
                .then(r => {
                    const surveyTemplate = r.data;
                    vm.description = mkDescription([surveyTemplate.description, vm.surveyRun.description]);
                })
            );
    }

    function loadRecipients(force) {
        return serviceBroker
            .loadViewData(
                CORE_API.SurveyInstanceStore.findRecipients,
                [vm.instanceId],
                { force })
            .then(r => {
                vm.recipients = r.data;
                vm.people = _.map(vm.recipients, r => r.person);
            });
    }


    // -- INTERACT --

    vm.viewOtherResponseVersion = (otherVer) => {
        $state.go(
            "main.survey.instance.response.view",
            {id: otherVer.instanceId});
    };

    vm.onAddRecipient = p => {
        const cmd = mkCreateRecipientCommand(vm.instanceId, p);
        notification.info(`Adding ${p.name} as a recipient`);
        return serviceBroker
            .execute(CORE_API.SurveyInstanceStore.addRecipient, [ vm.instanceId, cmd ])
            .then(r => {
                if(r.data) {
                    loadRecipients(true);
                    notification.success(`${p.name} added as a recipient`);
                } else {
                    notification.error(`${p.name} not added as a recipient`)
                }
            })
            .catch(e => displayError(
                notification,
                `Could not add ${p.name} as a recipient`,
                e));
    };

    vm.onRemoveRecipient = p => {
        const recipient = findMatchingRecipient(vm.recipients, p);
        notification.info(`Removing ${p.name} as a recipient`);
        if (recipient) {
            return serviceBroker
                .execute(
                    CORE_API.SurveyInstanceStore.deleteRecipient,
                    [vm.surveyInstance.id, recipient.id])
                .then(r => {
                    if(r.data) {
                        loadRecipients(true);
                        notification.success(`Removed ${p.name} from recipients`);
                    } else {
                        notification.error(`${p.name} not removed from recipients`)
                    }
                });
        } else {
            // we couldn't find the recipient so lets reload in case something happened elsewhere
            return loadRecipients();
        }
    };

    vm.editRecipient = (data, instanceRecipientId) => {
        const cmd = mkUpdateRecipientCommand(instanceRecipientId, data.newVal, vm.surveyInstance.id);
        serviceBroker
            .execute(
                CORE_API.SurveyInstanceStore.updateRecipient,
                [vm.surveyInstance.id, cmd])
            .then(() => notification.success("Updated survey recipient"))
            .catch(e => displayError(notification, "Failed to update recipient", e))
            .finally(() => loadRecipients(true));
    };

    vm.updateDueDate = (change, instanceId) => {
        if (!change.newVal) {
            notification.error("Due date cannot be blank");
        } else {
            serviceBroker
                .execute(
                    CORE_API.SurveyInstanceStore.updateDueDate,
                    [ instanceId, {newDateVal: timeFormat("%Y-%m-%d")(change.newVal)}])
                .then(() => {
                    notification.success("Survey instance due date updated successfully");
                    loadInstanceAndRun(true);
                })
                .catch(e => displayError(notification, "Failed to update survey instance due date", e));
        }
    };

    vm.invokeStatusAction = (action) => {
        action
            .onPerform(serviceBroker, vm.surveyInstance, notification)
            .then(() => reload(true));
    };

    // -- LIFECYCLE

    vm.$onChanges = (changes) => {
        if (vm.instanceId) {
            reload();
        }
    };

    vm.$onDestroy = () => {
    };
}

controller.$inject = [
    "$state",
    "ServiceBroker",
    "UserService",
    "Notification"
];


const component = {
    template,
    controller,
    bindings
};


export default {
    component,
    id: "waltzSurveyInstanceSummary"
};