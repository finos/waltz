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
import {kindToViewState} from "../common/link-utils";
import {timeFormat} from "d3-time-format";
import _ from "lodash";

const initialState = {
    surveyQuestions: [],
    surveyResponses: {},
};

const template = require('./survey-instance-response-edit.html');


function groupQuestions(questions = []) {
    const sections = _.chain(questions)
        .map(q => q.sectionName || "Other")
        .uniq()
        .value();

    const groupedQuestions = _.groupBy(questions, q => q.sectionName || "Other");

    return _.map(sections, s => {
        return {
            'sectionName': s,
            'questions': groupedQuestions[s]
        }
    });
}


function groupResponses(responses = []) {
    return _.chain(responses)
        .map(r => r.questionResponse)
        .map(qr => {
            if (!_.isNil(qr.booleanResponse) && !_.isString(qr.booleanResponse)) {
                qr.booleanResponse = qr.booleanResponse
                                    ? 'true'
                                    : 'false';
            }
            return qr;
        })
        .keyBy('questionId')
        .value();
}


function controller($q,
                    $state,
                    $window,
                    surveyInstance,
                    notification,
                    surveyInstanceStore,
                    surveyRunStore,
                    surveyQuestionStore) {

    const vm = initialiseData(this, initialState);

    vm.surveyInstance = surveyInstance;

    surveyRunStore.getById(surveyInstance.surveyRunId)
        .then(sr => vm.surveyRun = sr);

    const questionPromise = surveyQuestionStore.findForInstance(surveyInstance.id);
    const responsePromise = surveyInstanceStore.findResponses(surveyInstance.id);
    $q.all([questionPromise, responsePromise])
        .then(([questions, responses = {}]) => {
            vm.surveyQuestions = groupQuestions(questions);
            vm.surveyResponses = groupResponses(responses);
        });

    vm.saveResponse = (questionId) => {
        surveyInstanceStore.saveResponse(
            surveyInstance.id,
            Object.assign({'questionId': questionId}, vm.surveyResponses[questionId])
        );
    };

    vm.saveComment = (questionId, valObj) => {
        if (! vm.surveyResponses[questionId]) {
            vm.surveyResponses[questionId] = {};
        }
        vm.surveyResponses[questionId].comment = valObj.newVal;

        return surveyInstanceStore.saveResponse(
            surveyInstance.id,
            Object.assign({'questionId': questionId}, vm.surveyResponses[questionId])
        );
    };

    vm.submit = () => {
        surveyInstanceStore.updateStatus(
            surveyInstance.id,
            { newStatus: 'COMPLETED' }
        )
        .then(result => notification.success('Survey response submitted successfully'));
    };

    vm.goToParent = () => {
        try {
            const nextState = kindToViewState(surveyInstance.surveyEntity.kind);
            $state.go(nextState, surveyInstance.surveyEntity);
        } catch (e) {
            $window.history.back();
        }
    };
}


controller.$inject = [
    '$q',
    '$state',
    '$window',
    'surveyInstance',
    'Notification',
    'SurveyInstanceStore',
    'SurveyRunStore',
    'SurveyQuestionStore'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};

