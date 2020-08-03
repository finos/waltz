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
import {groupQuestions, indexResponses, mkSurveyExpressionEvaluator, refreshQuestions} from "./survey-utils";
import _ from "lodash";
import {CORE_API} from "../common/services/core-api-utils";
import moment from "moment";
import {dynamicSections} from "../dynamic-section/dynamic-section-definitions";
import template from "./survey-instance-response-edit.html";


const initialState = {
    changeLogSection: dynamicSections.changeLogSection,
    isUserInstanceRecipient: false,
    instanceCanBeEdited: false,
    surveyInstance: {},
    surveyQuestionInfos: [],
    surveyResponses: {},
    user: {}
};

const submissionConfirmationPrompt = `The survey cannot be edited once submitted.
Please ensure you have saved any comments you may have entered (by clicking 'Save' on each comment field).
Are you sure you want to submit your responses?`;



const statusesWhichSupportEditing = ["NOT_STARTED", "IN_PROGRESS", "REJECTED"];


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

    function loadAll() {
        const recipientsPromise = serviceBroker
            .loadViewData(CORE_API.SurveyInstanceStore.findRecipients, [id])
            .then(r => r.data);

        const instancePromise = serviceBroker
            .loadViewData(CORE_API.SurveyInstanceStore.getById, [id])
            .then(r => {
                vm.surveyInstance = r.data;
                vm.instanceCanBeEdited = _.includes(statusesWhichSupportEditing, vm.surveyInstance.status);
            });

        const runPromise = instancePromise
            .then(() => serviceBroker
                .loadViewData(CORE_API.SurveyRunStore.getById, [vm.surveyInstance.surveyRunId]))
            .then(r => vm.surveyRun = r.data);

        const questionPromise = serviceBroker
            .loadViewData(CORE_API.SurveyQuestionStore.findForInstance, [id])
            .then(r => vm.allQuestions = r.data);

        const responsePromise = serviceBroker
            .loadViewData(CORE_API.SurveyInstanceStore.findResponses, [id])
            .then(r => vm.surveyResponses = indexResponses(r.data));

        $q.all([questionPromise, responsePromise])
            .then(() => vm.surveyQuestionInfos = refreshQuestions(vm.allQuestions, vm.surveyResponses));

        $q.all([userService.whoami(), recipientsPromise])
            .then(([user = {}, recipients = []]) => {
                vm.user = user;
                const [currentRecipients = [], otherRecipients = []] = _.partition(
                    recipients,
                    r => _.toLower(r.person.email) === _.toLower(user.userName));

                vm.isUserInstanceRecipient = currentRecipients.length > 0;
                vm.otherRecipients = otherRecipients.map(r => r.person);
            });
    }

    loadAll();

    function reloadQuestions() {
        const questionPromise = serviceBroker
            .loadViewData(CORE_API.SurveyQuestionStore.findForInstance, [id], { force: true })
            .then(r => {
                vm.allQuestions = r.data;
                vm.surveyQuestionInfos = groupQuestions(r.data);
            });
    }


    vm.saveResponse = (questionId) => {
        const questionResponse = vm.surveyResponses[questionId];
        vm.surveyQuestionInfos = refreshQuestions(vm.allQuestions, vm.surveyResponses);

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
                [vm.surveyInstance.id, saveParams])
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

        return serviceBroker
            .execute(
                CORE_API.SurveyInstanceStore.saveResponse,
                [vm.surveyInstance.id, Object.assign({"questionId": question.id}, questionResponse)]);
    };

    /**
     * This is a bit of fakery as the questions are saved each time a response is updated.
     * Therefore this method merely moves the user back to their instance list.
     */
    vm.saveForLater = () => {
        $timeout(() => {
            notification.success("Survey response saved successfully");
            $state.go("main.survey.instance.user");
        }, 200); // allow blur events to fire
    };

    const doSubmit = () => {
        serviceBroker
            .execute(
                CORE_API.SurveyInstanceStore.updateStatus,
                [vm.surveyInstance.id, {newStatus: "COMPLETED"}])
            .then(() => {
                notification.success("Survey response submitted successfully");
                serviceBroker.loadAppData(
                    CORE_API.NotificationStore.findAll,
                    [],
                    {force: true});
                $state.go("main.survey.instance.response.view", {id});
            });
    };

    vm.submit = () => {
        $timeout(() => {
            if (confirm(submissionConfirmationPrompt)) {
                doSubmit();
            }
        }, 200); // allow blur events to fire, because 'confirm' blocks events
    };

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

