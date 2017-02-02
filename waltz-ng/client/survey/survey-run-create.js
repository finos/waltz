/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
import {initialiseData} from "../common/index";
import {timeFormat} from "d3-time-format";

const initialState = {
    step: 'GENERAL',
    surveyRun: {
        selectorEntityKind: 'APP_GROUP',
        selectorScope: 'EXACT',
        issuanceKind: 'GROUP'
    },
    onSaveGeneral: (s) => {},
    onSaveRecipient: (r) => {}
};

const template = require('./survey-run-create.html');

function controller($location,
                    $state,
                    surveyTemplate,
                    surveyRunStore) {

    const vm = initialiseData(this, initialState);

    vm.surveyTemplate = surveyTemplate;
    vm.surveyRun.surveyTemplate = surveyTemplate;

    const generateEmailLink = (surveyRun, includedRecipients) => {
        vm.surveyEmailRecipients = _
            .chain(includedRecipients)
            .map(r => r.person.email)
            .join(';');

        vm.surveyEmailSubject = 'Survey invitation: ' + surveyRun.name;
        const surveyLink = _.replace($location.absUrl(), '#' + $location.url(), '')
            + $state.href('main.survey-run.response', {id: surveyRun.id});

        const newLine = '%0D%0A';
        vm.surveyEmailBody = surveyRun.description
            + newLine
            + newLine
            + 'Please click the link to fill the survey: '
            + surveyLink
    };

    vm.onSaveGeneral = (surveyRun) => {
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
            involvementKindIds: surveyRun.involvementKinds,
            issuanceKind: surveyRun.issuanceKind,
            dueDate: timeFormat('%Y-%m-%d')(surveyRun.dueDate),
            contactEmail: surveyRun.contactEmail
        };


        if (surveyRun.id) {
            surveyRunStore.update(surveyRun.id, command)
                .then(r => {
                    vm.step = 'RECIPIENT';
                });

        } else {
            surveyRunStore.create(command)
                .then(r => {
                    vm.surveyRun.id = r.id;
                    vm.step = 'RECIPIENT';
                });
        }
    };

    vm.onSaveRecipient = (surveyRun, includedRecipients, excludedRecipients) => {
        surveyRunStore.createSurveyRunInstancesAndRecipients(surveyRun.id, excludedRecipients)
            .then(r => surveyRunStore.updateStatus(surveyRun.id, {newStatus: 'ISSUED'})
                .then(r => {
                    vm.step = 'COMPLETED';
                    generateEmailLink(surveyRun, includedRecipients);
                })
            );
    };

    vm.goBack = () => {
        if (vm.step === 'RECIPIENT') vm.step = 'GENERAL';
    };
}

controller.$inject = ['$location', '$state', 'surveyTemplate', 'SurveyRunStore'];

export default {
    template,
    controller,
    controllerAs: 'ctrl'
};

