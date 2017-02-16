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
import {initialiseData} from "../../common/index";
import _ from "lodash";


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


const template = require('./survey-run-create-recipient.html');


function controller(surveyRunStore) {
    const vm = initialiseData(this, initialState);

    surveyRunStore.generateSurveyRunRecipients(vm.surveyRun.id)
        .then(r => {
            vm.surveyRunRecipients = r.data;
            vm.includedRecipients = [].concat(vm.surveyRunRecipients);
            vm.excludeRecipients = [];
        });

    vm.excludeRecipient = (recipient) => {
        _.pull(vm.includedRecipients, recipient);
        vm.excludeRecipients.push(recipient);
    };

    vm.includeRecipient = (recipient) => {
        vm.includedRecipients.push(recipient);
        _.pull(vm.excludeRecipients, recipient);
    };

    vm.isRecipientIncluded = (recipient) =>
        vm.includedRecipients.indexOf(recipient) >= 0;

    vm.onSubmit = () =>
        vm.onSave(this.surveyRun, this.includedRecipients, this.excludedRecipients);

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
