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
import {groupQuestions} from "./survey-utils";
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


function indexResponses(responses = []) {
    return _.chain(responses)
        .map(r => r.questionResponse)
        .map(qr => {
            if (!_.isNil(qr.booleanResponse) && !_.isString(qr.booleanResponse)) {
                qr.booleanResponse = qr.booleanResponse
                    ? "true"
                    : "false";
            }
            if (_.isNil(qr.booleanResponse) && !_.isString(qr.booleanResponse)){
                qr.booleanResponse = "null"
            }
            if (!_.isNil(qr.dateResponse)) {
                qr.dateResponse = moment(qr.dateResponse, formats.parseDateOnly).toDate()
            }
            return qr;
        })
        .keyBy("questionId")
        .value();
}



function controller($location,
                    $state,
                    $stateParams,
                    $timeout,
                    notification,
                    serviceBroker,
                    surveyInstanceStore,
                    surveyQuestionStore,
                    userService) {

    const vm = initialiseData(this, initialState);

    const id = $stateParams.id;

    vm.entityReference = {
        id,
        kind: "SURVEY_INSTANCE"
    };

    surveyInstanceStore
        .getById(id)
        .then(r => {
            vm.instanceCanBeEdited = (r.status === "NOT_STARTED" || r.status === "IN_PROGRESS" || r.status === "REJECTED");
            vm.surveyInstance = r;
            serviceBroker
                .loadViewData(CORE_API.SurveyRunStore.getById, [vm.surveyInstance.surveyRunId])
                .then(d => vm.surveyRun = d.data);
            return r;
        });

    surveyQuestionStore
        .findForInstance(id)
        .then(qis => vm.surveyQuestionInfos = groupQuestions(qis));

    Promise
        .all([userService.whoami(), surveyInstanceStore.findRecipients(id)])
        .then(([user = {}, recipients = []]) => {
            vm.user = user;
            const [currentRecipients = [], otherRecipients = []] = _.partition(
                recipients,
                r => _.toLower(r.person.email) === _.toLower(user.userName));

            vm.isUserInstanceRecipient = currentRecipients.length > 0;
            vm.otherRecipients = otherRecipients.map(r => r.person);
        });

    surveyInstanceStore
        .findResponses(id)
        .then(rs => vm.surveyResponses = indexResponses(rs));


    vm.surveyInstanceLink = encodeURIComponent(
        _.replace($location.absUrl(), "#" + $location.url(), "")
        + $state.href("main.survey.instance.view", {id: id}));

    vm.saveResponse = (questionId) => {
        const questionResponse = vm.surveyResponses[questionId];
        surveyInstanceStore.saveResponse(
            vm.surveyInstance.id,
            Object.assign(
                {"questionId": questionId},
                questionResponse,
                {
                    dateResponse : questionResponse && questionResponse.dateResponse
                        ? moment(questionResponse.dateResponse).format(formats.parseDateOnly)
                        : null
                })
        );
    };

    vm.saveEntityResponse = (entity, questionId) => {
        vm.surveyResponses[questionId] = {
            entityResponse: {
                id: entity.id,
                kind: entity.kind,
                name: entity.name
            }
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
        const questionResponse = !vm.surveyResponses[question.id]
            ? {}
            : vm.surveyResponses[question.id];
        questionResponse.comment = valObj.newVal;

        return surveyInstanceStore.saveResponse(
            vm.surveyInstance.id,
            Object.assign({"questionId": question.id}, questionResponse)
        );
    };

    vm.saveForLater = () => {
        $timeout(() => {
            notification.success("Survey response saved successfully");
            $state.go("main.survey.instance.user");
        }, 200); // allow blur events to fire
    };

    vm.submit = () => {
        $timeout(() => {
            if (confirm(
                `The survey cannot be edited once submitted.\nPlease ensure you have saved any comments you may have entered (by clicking 'Save' on each comment field).
            \nAre you sure you want to submit your responses?`)) {
                surveyInstanceStore.updateStatus(
                    vm.surveyInstance.id,
                    {newStatus: "COMPLETED"}
                )
                    .then(() => {
                        notification.success("Survey response submitted successfully");
                        serviceBroker.loadAppData(CORE_API.NotificationStore.findAll, [], {force: true});
                        $state.go("main.survey.instance.response.view", {id: id});
                    });
            }
        }, 200); // allow blur events to fire, because 'confirm' blocks events
    };

}

controller.$inject = [
    "$location",
    "$state",
    "$stateParams",
    "$timeout",
    "Notification",
    "ServiceBroker",
    "SurveyInstanceStore",
    "SurveyQuestionStore",
    "UserService"
];


export default {
    template,
    controller,
    controllerAs: "ctrl"
};

