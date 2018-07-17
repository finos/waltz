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
import {initialiseData} from "../common/index";
import {timeFormat} from "d3-time-format";
import template from './survey-run-create.html';


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


function controller($document,
                    $interval,
                    $location,
                    $state,
                    $stateParams,
                    surveyRunStore,
                    surveyTemplateStore) {

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
            .join(';');

        const surveyEmailSubject = `Survey invitation: ${surveyRun.name}`;
        const surveyLink = encodeURIComponent(
            _.replace($location.absUrl(), '#' + $location.url(), '')
            + $state.href('main.survey.instance.user'));

        const newLine = '%0D%0A';
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
            involvementKindIds: _.map(surveyRun.involvementKinds, kind => kind.id),
            issuanceKind: surveyRun.issuanceKind,
            dueDate: surveyRun.dueDate ? timeFormat('%Y-%m-%d')(surveyRun.dueDate) : null,
            contactEmail: surveyRun.contactEmail
        };

        if (surveyRun.id) {
            surveyRunStore.update(surveyRun.id, command)
                .then(() => {
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
                .then(() => {
                    vm.step = 'COMPLETED';
                    generateEmailLink(surveyRun, includedRecipients);
                })
            );
    };

    vm.goBack = () => {
        if (vm.step === 'RECIPIENT') vm.step = 'GENERAL';
    };
}


controller.$inject = [
    '$document',
    '$interval',
    '$location',
    '$state',
    '$stateParams',
    'SurveyRunStore',
    'SurveyTemplateStore'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};

