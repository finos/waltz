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
import * as actions from "./survey-actions";
import moment from "moment";


const bindings = {
    instanceId: "<"
};


const initialState = {};


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


function controller($q,
                    $state,
                    serviceBroker,
                    userService,
                    notification) {
    const vm = initialiseData(this, initialState);


    function reload(force = false) {
        return surveyUtils
            .loadSurveyInfo($q,  serviceBroker, userService, vm.instanceId, true)
            .then(details => {
                vm.surveyDetails = details;

                vm.description = surveyUtils.mkDescription([details.template.description, details.run.description]);
                vm.people = _.map(details.recipients, d => d.person);
                vm.availableStatusActions = actions.determineAvailableStatusActions(
                    details.instance,
                    details.permissions,
                    details.isLatest);

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
        const cmd = mkCreateRecipientCommand(vm.instanceId, p);
        notification.info(`Adding ${p.name} as a recipient`);
        return serviceBroker
            .execute(CORE_API.SurveyInstanceStore.addRecipient, [ vm.instanceId, cmd ])
            .then(r => {
                if(r.data) {
                    reload(true);
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
        const recipient = findMatchingRecipient(vm.surveyDetails.recipients, p);
        notification.info(`Removing ${p.name} as a recipient`);
        console.log({recipient})
        if (recipient) {
            return serviceBroker
                .execute(
                    CORE_API.SurveyInstanceStore.deleteRecipient,
                    [vm.surveyDetails.instance.id, recipient.id])
                .then(r => {
                    if(r.data) {
                        reload(true);
                        notification.success(`Removed ${p.name} from recipients`);
                    } else {
                        notification.error(`Failed to remove ${p.name}`);
                    }
                })
                .catch(e => displayError(notification, `${p.name} not removed from recipients`, e));
        } else {
            // we couldn't find the recipient so lets reload in case something happened elsewhere
            return reload();
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
                    reload(true);
                })
                .catch(e => displayError(notification, "Failed to update survey instance due date", e));
        }
    };

    vm.invokeStatusAction = (action) => {
        action
            .onPerform(serviceBroker, vm.surveyDetails.instance, notification)
            .then(() => reload(true))
            .catch(msg => notification.warning(msg))
    };

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