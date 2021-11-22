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
import {sameRef} from "../../../common/entity-utils";
import {displayError} from "../../../common/error-utils";
import * as surveyUtils from "../../survey-utils";
import * as actions from "../../survey-actions";
import moment from "moment";
import toasts from "../../../svelte-stores/toast-store";

const bindings = {
    instanceId: "<"
};


const initialState = {
    statusIcon: "fw"
};


function mkUpdateRecipientCommand(instanceRecipientId, person, surveyInstanceId) {
    return {
        instanceRecipientId,
        surveyInstanceId,
        personId: person.id,
    }
}


function mkCreateInstanceInvolvementCommand(instanceId, person) {
    return {
        surveyInstanceId: instanceId,
        personId: person.id
    };
}


function findMatchingInvolvement(involvements = [], person) {
    return _.find(involvements, r => sameRef(r.person, person))
}

const statusToIcon = {
    APPROVED: "thumbs-o-up",
    COMPLETED: "check",
    IN_PROGRESS: "pencil",
    NOT_STARTED: "clock-o",
    REJECTED: "ban",
    WITHDRAWN: "cross"
};


function controller($q,
                    $state,
                    $timeout,
                    serviceBroker,
                    userService) {
    const vm = initialiseData(this, initialState);


    function reload(force = false) {
        return surveyUtils
            .loadSurveyInfo($q,  serviceBroker, userService, vm.instanceId, true)
            .then(details => {
                vm.surveyDetails = details;

                vm.statusIcon = statusToIcon[vm.surveyDetails.instance.status] || "fw";

                vm.description = surveyUtils.mkDescription([details.template.description, details.run.description]);
                vm.recipients = _.map(details.recipients, d => d.person);
                vm.owners = _.map(details.instanceOwners, d => d.person);
                vm.availableStatusActions = actions.determineAvailableStatusActions(
                    details.isLatest,
                    details.possibleActions);

                const prevVersions = _
                    .chain(details.versions)
                    .sortBy(d => d.submittedAt)
                    .map((pv, i) => {
                        const m = moment.utc(pv.submittedAt);
                        return {
                            versionNum: `Version ${i + 1} / Submitted: ${m.fromNow()}`,
                            instanceId: pv.id,
                            isLatest: false
                        };
                    })
                    .reverse()
                    .value();

                const latestResponseVersion = {
                    versionNum: `Version: ${prevVersions.length + 1} / Latest`,
                    instanceId: details.latestInstanceId,
                    isLatest: true
                };

                const allResponseVersions = [latestResponseVersion].concat(prevVersions);
                vm.currentResponseVersion = _.keyBy(allResponseVersions, "instanceId")[details.instance.id];
                vm.otherResponseVersions = _.filter(
                    allResponseVersions,
                    rv => rv.instanceId !== vm.currentResponseVersion.instanceId);
            });
    }

    // -- INTERACT --

    vm.viewOtherResponseVersion = (otherVer) => {
        $state.go(
            "main.survey.instance.response.view",
            {id: otherVer.instanceId});
    };

    vm.onAddRecipient = p => {
        const cmd = mkCreateInstanceInvolvementCommand(vm.instanceId, p);
        toasts.info(`Adding ${p.name} as a recipient`);
        return serviceBroker
            .execute(CORE_API.SurveyInstanceStore.addRecipient, [ vm.instanceId, cmd ])
            .then(r => {
                if(r.data) {
                    reload(true);
                    toasts.success(`${p.name} added as a recipient`);
                } else {
                    toasts.error(`${p.name} not added as a recipient`)
                }
            })
            .catch(e => displayError(`Could not add ${p.name} as a recipient`, e));
    };

    vm.onRemoveRecipient = (p) => {
        const recipient = findMatchingInvolvement(vm.surveyDetails.recipients, p);
        toasts.info(`Removing ${p.name} as a recipient`);
        if (recipient) {
            return serviceBroker
                .execute(
                    CORE_API.SurveyInstanceStore.deleteRecipient,
                    [vm.surveyDetails.instance.id, recipient.id])
                .then(r => {
                    if(r.data) {
                        reload(true);
                        toasts.success(`Removed ${p.name} from recipients`);
                    } else {
                        toasts.error(`Failed to remove ${p.name}`);
                    }
                })
                .catch(e => displayError(`${p.name} not removed from recipients`, e));
        } else {
            // we couldn't find the recipient so lets reload in case something happened elsewhere
            return reload();
        }
    };

    vm.onAddOwner = (p) => {
        const cmd = mkCreateInstanceInvolvementCommand(vm.instanceId, p);
        toasts.info(`Adding ${p.name} as an owner`);
        return serviceBroker
            .execute(CORE_API.SurveyInstanceStore.addOwner, [ vm.instanceId, cmd ])
            .then(r => {
                if(r.data) {
                    reload(true);
                    toasts.success(`${p.name} added as an owner`);
                } else {
                    toasts.error(`${p.name} not added as an owner`)
                }
            })
            .catch(e => displayError(`Could not add ${p.name} as an owner`, e));
    };

    vm.onRemoveOwner = p => {
        const owner = findMatchingInvolvement(vm.surveyDetails.instanceOwners, p);
        toasts.info(`Removing ${p.name} as an owner`);
        if (owner) {
            return serviceBroker
                .execute(
                    CORE_API.SurveyInstanceStore.deleteOwner,
                    [vm.surveyDetails.instance.id, owner.id])
                .then(r => {
                    if(r.data) {
                        reload(true);
                        toasts.success(`Removed ${p.name} from owners`);
                    } else {
                        toasts.error(`Failed to remove ${p.name} from owners`);
                    }
                })
                .catch(e => displayError(`${p.name} not removed from owners`, e));
        } else {
            // we couldn't find the owner so lets reload in case something happened elsewhere
            return reload();
        }
    };

    vm.editRecipient = (data, instanceRecipientId) => {
        const cmd = mkUpdateRecipientCommand(instanceRecipientId, data.newVal, vm.surveyInstance.id);
        serviceBroker
            .execute(
                CORE_API.SurveyInstanceStore.updateRecipient,
                [vm.surveyInstance.id, cmd])
            .then(() => toasts.success("Updated survey recipient"))
            .catch(e => displayError("Failed to update recipient", e))
            .finally(() => loadRecipients(true));
    };

    vm.updateDueDate = (change, instanceId) => {
        if (!change.newVal) {
            toasts.error("Due date cannot be blank");
        } else {
            serviceBroker
                .execute(
                    CORE_API.SurveyInstanceStore.updateDueDate,
                    [ instanceId, {newDateVal: timeFormat("%Y-%m-%d")(change.newVal)}])
                .then(() => {
                    toasts.success("Survey instance due date updated successfully");
                    reload(true);
                })
                .catch(e => displayError("Failed to update survey instance due date", e));
        }
    };


    vm.updateApprovalDueDate = (change, instanceId) => {
        if (!change.newVal) {
            toasts.error("Due date cannot be blank");
        } else {
            serviceBroker
                .execute(
                    CORE_API.SurveyInstanceStore.updateApprovalDueDate,
                    [ instanceId, {newDateVal: timeFormat("%Y-%m-%d")(change.newVal)}])
                .then(() => {
                    toasts.success("Survey instance approval due date updated successfully");
                    reload(true);
                })
                .catch(e => displayError("Failed to update survey instance approval due date", e));
        }
    };

    vm.invokeStatusAction = actions.invokeStatusAction(serviceBroker, toasts, reload, $timeout, $state)

    // -- LIFECYCLE

    vm.$onChanges = (changes) => {
        if (vm.instanceId) {
            reload();
        }
    };

}

controller.$inject = [
    "$q",
    "$state",
    "$timeout",
    "ServiceBroker",
    "UserService"
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