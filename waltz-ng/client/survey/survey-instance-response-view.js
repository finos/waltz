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
import _ from 'lodash';
import {groupQuestions} from './survey-utils';


function indexResponses(rs = []) {
    return _.chain(rs)
        .map('questionResponse')
        .map(qr => ({
            questionId: qr.questionId,
            answer: qr.stringResponse || qr.numberResponse || qr.booleanResponse,
            comment: qr.comment
        }))
        .keyBy('questionId')
        .value();
}


function controller($stateParams,
                    personStore,
                    surveyInstanceStore,
                    surveyRunStore,
                    surveyQuestionStore) {

    const vm = this;
    const id = $stateParams.id;

    surveyInstanceStore
        .getById(id)
        .then(surveyInstance => {
            vm.surveyInstance = surveyInstance;
            return surveyRunStore
                .getById(surveyInstance.surveyRunId)
        })
        .then(sr => vm.surveyRun = sr)


    const loadParticipants = responses => {
        vm.participants = []
        _.chain(responses)
            .map('personId')
            .uniq()
            .map(pid => personStore
                .getById(pid)
                .then(p => vm.participants.push(p)))
            .value();
    };

    surveyQuestionStore
        .findForInstance(id)
        .then(questions => vm.surveyQuestions = groupQuestions(questions));

    surveyInstanceStore
        .findResponses(id)
        .then(responses => {
            vm.answers = indexResponses(responses);
            global.xs = vm.answers
            loadParticipants(responses);
        });
}


controller.$inject = [
    '$stateParams',
    'PersonStore',
    'SurveyInstanceStore',
    'SurveyRunStore',
    'SurveyQuestionStore'
];


const view = {
    controller,
    controllerAs: 'ctrl',
    template: require('./survey-instance-response-view.html')
};

export default view;