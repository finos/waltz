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
import _ from "lodash";
import {initialiseData} from "../common/index";
import {timeFormat} from "d3-time-format";
import template from "./survey-run-create.html";
import {instanceCreateCommand} from "./components/survey-run-create-store";
import {displayError} from "../common/error-utils";


const initialState = {
    step: "GENERAL",
    surveyRun: {
        selectorEntityKind: "APP_GROUP",
        selectorScope: "EXACT",
        issuanceKind: "GROUP"
    },
    dueDate: null,
    approvalDueDate: null,
    onSaveGeneral: (s) => {},
    onSaveRecipient: (r) => {}
};


function controller($document,
                    $interval,
                    $location,
                    $state,
                    $stateParams,
                    surveyRunStore,
                    surveyTemplateStore,
                    serviceBroker) {

    const vm = initialiseData(this, initialState);
    const templateId = $stateParams.id;

    surveyTemplateStore.getById(templateId)
        .then(t => {
            vm.surveyTemplate = t;
            vm.surveyRun.surveyTemplate = t;

            // copy name and description from the template if they are not set
            if (!vm.surveyRun.name) vm.surveyRun.name = t.name;
            if (!vm.surveyRun.description) vm.surveyRun.description = t.description;
        });

    const generateEmailLink = (surveyRun, includedRecipients) => {
        const surveyEmailRecipients = _
            .chain(includedRecipients)
            .map(r => r.person.email)
            .uniq()
            .join(";");

        const surveyEmailSubject = `Survey invitation: ${surveyRun.name}`;
        const surveyLink = $state.href("main.survey.instance.user", {}, {absolute: true});

        const newLine = "%0D%0A";
        const surveyEmailBody = `You have been invited to participate in the following survey. ${newLine}${newLine}`
                + `Name: ${surveyRun.name} ${newLine}${newLine}`
                + `Description: ${surveyRun.description} ${newLine}${newLine}`
                + `${newLine}${newLine}`
                + `Please use this URL to find and respond to this survey:  ${surveyLink} ${newLine}${newLine}`;

        vm.surveyEmailHref = `mailto:${surveyEmailRecipients}?subject=${surveyEmailSubject}&body=${surveyEmailBody}`;
    };

    // use this as a workaround on IE issue with long email body
    vm.generateEmail = () => {
        const document = $document[0];
        const iframeHack = document.createElement("IFRAME");
        iframeHack.style.width = "0px";
        iframeHack.style.height = "0px";

        iframeHack.src = vm.surveyEmailHref;
        document.body.appendChild(iframeHack);
        $interval(() => document.body.removeChild(iframeHack), 100, 1);
    };

    vm.onSaveGeneral = (surveyRun, surveyInstance) => {
        const command = {
            name: surveyRun.name,
            description: surveyRun.description,
            surveyTemplateId: surveyRun.surveyTemplate.id,
            selectionOptions: {
                entityReference: {
                    kind: surveyRun.selectorEntityKind,
                    id: surveyRun.selectorEntity.id
                },
                scope: surveyRun.selectorScope,
            },
            involvementKindIds: _.map(surveyRun.involvementKinds, kind => kind.id),
            ownerInvKindIds: _.map(surveyRun.ownerInvolvementKinds, kind => kind.id),
            issuanceKind: surveyRun.issuanceKind,
            dueDate: surveyRun.dueDate ? timeFormat("%Y-%m-%d")(surveyRun.dueDate) : null,
            approvalDueDate: surveyRun.approvalDueDate ? timeFormat("%Y-%m-%d")(surveyRun.dueDate) : null,
            contactEmail: surveyRun.contactEmail
        };

        if (surveyRun.id) {
            surveyRunStore
                .update(surveyRun.id, command)
                .then(() => {
                    vm.step = "RECIPIENT";
                    const instancesRecipientsCreateCommand = {
                        surveyRunId: surveyRun.id,
                        dueDate: surveyRun.dueDate,
                        approvalDueDate: surveyRun.approvalDueDate,
                        excludedRecipients: [],
                        owningRole: surveyInstance.owningRole
                    };
                    instanceCreateCommand.set(instancesRecipientsCreateCommand);
                })
                .catch(e => displayError("Could not update survey run", e));
        } else {
            surveyRunStore
                .create(command)
                .then(r => {
                    vm.surveyRun.id = r.id;
                    vm.step = "RECIPIENT";
                    const instancesRecipientsCreateCommand = {
                        surveyRunId: r.id,
                        dueDate: surveyRun.dueDate,
                        approvalDueDate: surveyRun.approvalDueDate,
                        excludedRecipients: [],
                        owningRole: surveyInstance.owningRole
                    };
                    instanceCreateCommand.set(instancesRecipientsCreateCommand);
                })
                .catch(e => displayError("Could not create survey run", e));
        }


    };

    vm.onSaveRecipient = (createInstancesAndRecipientsCommand, includedRecipients) => {
        surveyRunStore
            .createSurveyRunInstancesAndRecipients(createInstancesAndRecipientsCommand)
            .then(() => surveyRunStore.updateStatus(createInstancesAndRecipientsCommand.surveyRunId, {newStatus: "ISSUED"})
                .then(() => {
                    vm.step = "COMPLETED";
                    generateEmailLink(vm.surveyRun, includedRecipients);
                })
            );
    };

    vm.goBack = () => {
        if (vm.step === "RECIPIENT") vm.step = "GENERAL";
    };
}


controller.$inject = [
    "$document",
    "$interval",
    "$location",
    "$state",
    "$stateParams",
    "SurveyRunStore",
    "SurveyTemplateStore",
    "ServiceBroker"
];


export default {
    template,
    controller,
    controllerAs: "ctrl"
};

