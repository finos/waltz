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
import {initialiseData} from "../../common/index";
import _ from "lodash";
import template from "./survey-run-create-recipient.html";
import {instanceCreateCommand} from "./survey-run-create-store";


const bindings = {
    surveyTemplate: "<",
    surveyRun: "<",
    instanceCreate: "<",
    onSave: "<",
    onGoBack: "<"
};


const initialState = {
    surveyRunRecipients: [],
    includedRecipients: [],
    excludedRecipients: [],
};


function controller(surveyRunStore, $scope) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        instanceCreateCommand.subscribe(d => $scope.$applyAsync(() => {
            vm.instanceCreate = d;
            surveyRunStore
                .generateSurveyRunRecipients(vm.instanceCreate)
                .then(r => {
                    vm.surveyRunRecipients = r.data;
                    vm.includedRecipients = [].concat(vm.surveyRunRecipients);
                    vm.excludedRecipients = [];
                });
        }));
    };

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

    vm.onSubmit = () =>{
        vm.instanceCreate.excludedRecipients = vm.excludedRecipients;
        vm.onSave(vm.instanceCreate, vm.includedRecipients);
    }

    vm.goBack = () => {
        vm.onGoBack();
    }
}


controller.$inject = [
    "SurveyRunStore",
    "$scope"
];


export default {
    bindings,
    template,
    controller
};
