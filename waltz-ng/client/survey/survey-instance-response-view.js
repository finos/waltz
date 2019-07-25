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
import {initialiseData} from "../common";
import {groupQuestions} from "./survey-utils";
import {dynamicSections} from "../dynamic-section/dynamic-section-definitions";
import template from "./survey-instance-response-view.html";
import {CORE_API} from "../common/services/core-api-utils";


const initialState = {
    changeLogSection: dynamicSections.changeLogSection
};


function extractAnswer(response = {}) {
    return !_.isNil(response.booleanResponse)
            ? response.booleanResponse
            : (response.stringResponse || response.numberResponse || response.dateResponse || response.entityResponse)
}


function indexResponses(rs = []) {
    return _.chain(rs)
        .map("questionResponse")
        .map(qr => ({
            questionId: qr.questionId,
            answer: extractAnswer(qr),
            comment: qr.comment
        }))
        .keyBy("questionId")
        .value();
}


function controller($stateParams,
                    serviceBroker) {

    const vm = initialiseData(this, initialState);
    const id = $stateParams.id;

    vm.entityReference = {
        id,
        kind: "SURVEY_INSTANCE"
    };

    serviceBroker
        .loadViewData(
            CORE_API.SurveyInstanceStore.getById,
            [ id ])
        .then(r => {
            vm.surveyInstance = r.data;
            return serviceBroker
                .loadViewData(
                    CORE_API.SurveyRunStore.getById,
                    [ vm.surveyInstance.surveyRunId ]);
        })
        .then(r => vm.surveyRun = r.data);

    serviceBroker
        .loadViewData(
            CORE_API.SurveyQuestionStore.findForInstance,
            [ id ])
        .then(r => vm.surveyQuestionInfos = groupQuestions(r.data));

    serviceBroker
        .loadViewData(
            CORE_API.SurveyInstanceStore.findResponses,
            [ id ])
        .then(r => {
            vm.answers = indexResponses(r.data);
        });

}


controller.$inject = [
    "$stateParams",
    "ServiceBroker"
];


const view = {
    controller,
    controllerAs: "ctrl",
    template
};

export default view;