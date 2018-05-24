/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import _ from "lodash";
import {initialiseData} from "../../common/index";
import {CORE_API} from "../../common/services/core-api-utils";
import template from "./survey-section.html";
import {timeFormat} from "d3-time-format";


const initialState = {
    visibility: {
        mode: 'list' // list | issue
    },
    selectedTemplate: null,
    surveyRunForm: {
        dueDate: null,
        contactEmail: null,
        issuanceKind: "GROUP",
        recipients: []
    }
};


const bindings = {
    parentEntityRef: '<'
};


function controller(notification, serviceBroker, userService) {

    const vm = initialiseData(this, initialState);

    vm.onSelectTemplate = (template) => {
        vm.selectedTemplate = template;
        vm.surveyRunForm.name = template.name;
        vm.surveyRunForm.description = template.description;
    };

    vm.onDeselectTemplate = (template) => {
        vm.selectedTemplate = null;
    };

    vm.onShowCreateForm = () => {
        vm.visibility.mode = 'create';
        vm.selectedTemplate = null;
        serviceBroker
            .loadViewData(CORE_API.SurveyTemplateStore.findAll)
            .then(r => vm.templates = _
                .chain(r.data)
                .filter(t => t.targetEntityKind === vm.parentEntityRef.kind)
                .filter(t => t.status === 'ACTIVE')
                .sortBy('name')
                .value());
    };

    vm.onDismissCreateForm = () => {
        vm.visibility.mode = 'list';
    };

    vm.submitRun = () => {
        save();
    };

    vm.addRecipient = (p) => {
        if (! p) return;
        const recipients = vm.surveyRunForm.recipients;
        vm.surveyRunForm.recipients = _.concat(recipients ? recipients : [], [ p ])
    };

    vm.removeRecipient = (p) => {
        if (! p) return;
        const recipients = vm.surveyRunForm.recipients;
        vm.surveyRunForm.recipients = _.reject(recipients, r => r.id == p.id);
    };

    vm.$onInit = () => {
        userService
            .whoami()
            .then(me => vm.surveyRunForm.contactEmail = me.userName);
    };

    function save () {
        const recipientIds = _.map(vm.surveyRunForm.recipients, 'id');

        if (recipientIds.length == 0) {
            alert("Please provide at least one recipient");
            return;
        }
        const command = {
            name: vm.surveyRunForm.name,
            description: vm.surveyRunForm.description,
            surveyTemplateId: vm.selectedTemplate.id,
            selectionOptions: {
                entityReference: vm.parentEntityRef,
                scope: 'EXACT',
            },
            involvementKindIds: [],
            issuanceKind: vm.surveyRunForm.issuanceKind,
            dueDate: vm.surveyRunForm.dueDate ? timeFormat('%Y-%m-%d')(vm.surveyRunForm.dueDate) : null,
            contactEmail: vm.surveyRunForm.contactEmail
        };


        serviceBroker
            .execute(CORE_API.SurveyRunStore.create, [command])
            .then(r => r.data.id)
            .then(runId => serviceBroker
                .execute(CORE_API.SurveyRunStore.createSurveyInstances, [ runId, recipientIds ])
                .then(() => runId))
            .then(runId => serviceBroker
                .execute(CORE_API.SurveyRunStore.updateStatus, [runId, {newStatus: 'ISSUED'}]))
            .then(() => {
                notification.success('Survey issued successfully');
                vm.onDismissCreateForm();
            });
    }

}


controller.$inject = [
    'Notification',
    'ServiceBroker',
    'UserService'
];


const component = {
    bindings,
    template,
    controller
};

export default {
    component,
    id: 'waltzSurveySection'
};



