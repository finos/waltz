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
import {initialiseData, termSearch} from "../../common/index";
import {CORE_API} from "../../common/services/core-api-utils";
import template from "./survey-section.html";
import {timeFormat} from "d3-time-format";
import {displayError} from "../../common/error-utils";
import {isSurveyTargetKind} from "../survey-utils";
import toasts from "../../svelte-stores/toast-store";


const initialState = {
    visibility: {
        mode: "list", // list | issue,
        showIssueSurveyBtn: false
    },
    selectedTemplate: null,
    filteredTemplates: [],
    surveyRunForm: {
        dueDate: null,
        contactEmail: null,
        issuanceKind: "GROUP",
        recipients: [],
        owners: [],
        owningRole: null,
        recipientInvolvementKinds: [],
        ownerInvolvementKinds: []
    },
    templateQuery: ""
};


const bindings = {
    parentEntityRef: "<"
};


function controller(serviceBroker, userService) {

    const vm = initialiseData(this, initialState);

    vm.onSelectTemplate = (template) => {
        vm.selectedTemplate = template;
        vm.surveyRunForm.name = template.name;
        vm.surveyRunForm.description = template.description;
    };

    vm.onDeselectTemplate = () => {
        vm.selectedTemplate = null;
    };

    vm.onShowCreateForm = () => {
        vm.visibility.mode = "create";
        vm.selectedTemplate = null;
        serviceBroker
            .loadViewData(CORE_API.SurveyTemplateStore.findAll)
            .then(r => vm.templates = _
                .chain(r.data)
                .filter(t => t.targetEntityKind === vm.parentEntityRef.kind)
                .filter(t => t.status === "ACTIVE")
                .sortBy("name")
                .value())
            .then(vm.onQueryChange);
    };

    vm.onDismissCreateForm = () => {
        vm.visibility.mode = "list";
    };

    vm.submitRun = () => {
        save();
    };

    vm.onAddRecipient = (p) => {
        if (!p) return;
        const recipients = vm.surveyRunForm.recipients;
        vm.surveyRunForm.recipients = _.concat(recipients ? recipients : [], [p])
    };

    vm.onAddOwner = (p) => {
        if (!p) return;
        const owners = vm.surveyRunForm.owners;
        vm.surveyRunForm.owners = _.concat(owners ? owners : [], [p])
    };

    vm.onQueryChange = () => {
        vm.filteredTemplates = termSearch(vm.templates, vm.templateQuery, ["name", "description", "externalId"]);
    };

    vm.onRemoveRecipient = (p) => {
        if (!p) return;
        const recipients = vm.surveyRunForm.recipients;
        vm.surveyRunForm.recipients = _.reject(recipients, r => r.id === p.id);
    };

    vm.onRemoveOwner = (p) => {
        if (!p) return;
        const owners = vm.surveyRunForm.owners;
        vm.surveyRunForm.owners = _.reject(owners, r => r.id === p.id);
    };

    vm.$onInit = () => {
        serviceBroker.loadAppData(CORE_API.RoleStore.findAllRoles)
            .then(r => vm.customRoles = _.filter(r.data, d => d.isCustom === true));

        userService
            .whoami()
            .then(me => vm.surveyRunForm.contactEmail = me.userName);

        serviceBroker
            .loadAppData(CORE_API.InvolvementKindStore.findAll, [])
            .then(r => vm.availibleInvolvementKinds = r.data);

        vm.visibility.showIssueSurveyBtn = isSurveyTargetKind(vm.parentEntityRef.kind);
    };

    function toDate(fieldRef) {
        return fieldRef
            ? timeFormat("%Y-%m-%d")(fieldRef)
            : null
    }

    function save() {
        const recipientIds = _.map(vm.surveyRunForm.recipients, "id");
        const ownerIds = _.map(vm.surveyRunForm.owners, "id");

        const involvementKindIds = _.map(vm.surveyRunForm.recipientInvolvementKinds, "id");
        const ownerInvKindIds = _.map(vm.surveyRunForm.ownerInvolvementKinds, "id");

        if (_.isEmpty(recipientIds) && _.isEmpty(involvementKindIds)) {
            toasts.error("Please provide at least one recipient or recipient involvement kind");
            return;
        } else if (_.isEmpty(recipientIds)) {
            toasts.warning("If there are no recipients found for the selected involvement kinds the survey owner will be added to allow manual addition of recipients");
        }

        const submissionDueDate = toDate(vm.surveyRunForm.dueDate);
        const approvalDueDate = toDate(vm.surveyRunForm.approvalDueDate) || submissionDueDate;
        const command = {
            name: vm.surveyRunForm.name,
            description: vm.surveyRunForm.description,
            surveyTemplateId: vm.selectedTemplate.id,
            selectionOptions: {
                entityReference: vm.parentEntityRef,
                scope: "EXACT",
            },
            involvementKindIds,
            ownerInvKindIds,
            issuanceKind: vm.surveyRunForm.issuanceKind,
            dueDate: submissionDueDate,
            approvalDueDate: approvalDueDate,
            contactEmail: vm.surveyRunForm.contactEmail
        };


        serviceBroker
            .execute(CORE_API.SurveyRunStore.create, [command])
            .then(r => r.data.id)
            .then(runId => serviceBroker
                .execute(CORE_API.SurveyRunStore.createSurveyInstances, [runId, {
                    recipientPersonIds: recipientIds,
                    ownerPersonIds: ownerIds,
                    owningRole: vm.surveyRunForm.owningRole
                }])
                .then(() => runId))
            .then(runId => serviceBroker
                .execute(CORE_API.SurveyRunStore.updateStatus, [runId, {newStatus: "ISSUED"}]))
            .then(() => {
                toasts.success("Survey issued successfully");
                vm.onDismissCreateForm();
            })
            .catch(e => displayError("Could not create survey", e));
    }

}


controller.$inject = [
    "ServiceBroker",
    "UserService"
];


const component = {
    bindings,
    template,
    controller
};

export default {
    component,
    id: "waltzSurveySection"
};



