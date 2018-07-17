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
import {initialiseData} from "../../common/index";
import _ from "lodash";
import template from './survey-run-create-recipient.html';


const bindings = {
    surveyTemplate: '<',
    surveyRun: '<',
    onSave: '<',
    onGoBack: '<'
};


const initialState = {
    surveyRunRecipients: [],
    includedRecipients: [],
    excludedRecipients: []
};


function controller(surveyRunStore) {
    const vm = initialiseData(this, initialState);

    surveyRunStore.generateSurveyRunRecipients(vm.surveyRun.id)
        .then(r => {
            vm.surveyRunRecipients = r.data;
            vm.includedRecipients = [].concat(vm.surveyRunRecipients);
            vm.excludedRecipients = [];
        });

    vm.excludeRecipient = (recipient) => {
        _.pull(vm.includedRecipients, recipient);
        vm.excludedRecipients.push(recipient);
    };

    vm.includeRecipient = (recipient) => {
        vm.includedRecipients.push(recipient);
        _.pull(vm.excludedRecipients, recipient);
    };

    vm.isRecipientIncluded = (recipient) =>
        vm.includedRecipients.indexOf(recipient) >= 0;

    vm.onSubmit = () =>
        vm.onSave(vm.surveyRun, vm.includedRecipients, vm.excludedRecipients);

    vm.goBack = () => {
        vm.onGoBack();
    }
}


controller.$inject = [
    'SurveyRunStore'
];


export default {
    bindings,
    template,
    controller
};
