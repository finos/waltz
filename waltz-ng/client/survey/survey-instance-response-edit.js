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

import {formats, initialiseData} from "../common/index";
import * as SurveyUtils from "./survey-utils";
import _ from "lodash";
import {CORE_API} from "../common/services/core-api-utils";
import moment from "moment";
import {dynamicSections} from "../dynamic-section/dynamic-section-definitions";
import template from "./survey-instance-response-edit.html";
import * as actions from "./survey-actions";


const initialState = {
    changeLogSection: dynamicSections.changeLogSection,
    instanceCanBeEdited: false,
    groupedQuestions: [],
    surveyResponses: {},
    user: {}
};

const submissionConfirmationPrompt = `The survey cannot be edited once submitted.
Please ensure you have saved any comments you may have entered (by clicking 'Save' on each comment field).
Are you sure you want to submit your responses?`;


const statusesWhichSupportEditing = [
    "NOT_STARTED",
    "IN_PROGRESS",
    "REJECTED"
];


function controller($location,
                    $q,
                    $state,
                    $stateParams,
                    $timeout,
                    notification,
                    serviceBroker,
                    userService) {

    const vm = initialiseData(this, initialState);

    const id = $stateParams.id;

    vm.entityReference = {
        id,
        kind: "SURVEY_INSTANCE"
    };

    function reload() {
        const responsePromise = serviceBroker
            .loadViewData(CORE_API.SurveyInstanceStore.findResponses, [id])
            .then(r => vm.surveyResponses = SurveyUtils.indexResponses(r.data));

        SurveyUtils
            .loadSurveyInfo($q, serviceBroker, userService, id, true)
            .then(details => {
                vm.surveyDetails = details;
                vm.instanceCanBeEdited = _.includes(statusesWhichSupportEditing, details.instance.status);
                vm.availableStatusActions = actions.determineAvailableStatusActions(
                    details.isLatest,
                    details.possibleActions);
            });

        reloadQuestions();
    }

    function reloadQuestions() {
        const questionPromise = serviceBroker
            .loadViewData(CORE_API.SurveyQuestionStore.findForInstance, [id], { force: true })
            .then(r => {
                vm.groupedQuestions = SurveyUtils.groupQuestions(r.data);
            });
    }


    vm.saveResponse = (questionId) => {
        const questionResponse = vm.surveyResponses[questionId];

        const saveParams = Object.assign(
            {questionId},
            questionResponse,
            {
                dateResponse : questionResponse && questionResponse.dateResponse
                    ? moment(questionResponse.dateResponse).format(formats.parseDateOnly)
                    : null
            });

        serviceBroker
            .execute(
                CORE_API.SurveyInstanceStore.saveResponse,
                [vm.surveyDetails.instance.id, saveParams])
            .then(() => reloadQuestions());

    };

    vm.saveEntityResponse = (entity, questionId) => {
        const entityResponse = entity
            ? _.pick(entity, ["id", "kind", "name"])
            : null;

        vm.surveyResponses[questionId] = {
            entityResponse
        };

        vm.saveResponse(questionId);
    };

    vm.saveDateResponse = (questionId, dateVal) => {
        vm.surveyResponses[questionId] = {
            dateResponse: dateVal
        };
        vm.saveResponse(questionId);
    };

    vm.saveComment = (valObj, question) => {
        // get the current response, or note
        const questionResponse = _.get(vm.surveyResponses, [question.id], {});
        questionResponse.comment = valObj.newVal;

        const saveParams = [
            vm.surveyDetails.instance.id,
            Object.assign({"questionId": question.id}, questionResponse)
        ];

        return serviceBroker
            .execute(
                CORE_API.SurveyInstanceStore.saveResponse,
                saveParams);
    };

    vm.invokeStatusAction = actions.invokeStatusAction(serviceBroker, notification, reload, $timeout, $state)

    // --- BOOT
    reload();

}

controller.$inject = [
    "$location",
    "$q",
    "$state",
    "$stateParams",
    "$timeout",
    "Notification",
    "ServiceBroker",
    "UserService"
];


export default {
    template,
    controller,
    controllerAs: "ctrl"
};

