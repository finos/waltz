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

import {CORE_API} from "../common/services/core-api-utils";

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


export function determineAvailableStatusActions(isLatest, possibleActions, form) {
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
