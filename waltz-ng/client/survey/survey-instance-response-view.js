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
import template from './survey-instance-response-view.html';


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
        .map('questionResponse')
        .map(qr => ({
            questionId: qr.questionId,
            answer: extractAnswer(qr),
            comment: qr.comment
        }))
        .keyBy('questionId')
        .value();
}


function controller($state,
                    $stateParams,
                    notification,
                    personStore,
                    surveyInstanceStore,
                    surveyRunStore,
                    surveyQuestionStore) {

    const vm = initialiseData(this, initialState);
    const id = $stateParams.id;

    vm.entityReference = {
        id,
        kind: 'SURVEY_INSTANCE'
    };

    surveyInstanceStore
        .getById(id)
        .then(surveyInstance => {
            vm.surveyInstance = surveyInstance;
            return surveyRunStore
                .getById(surveyInstance.surveyRunId)
        })
        .then(sr => vm.surveyRun = sr)
        .then(sr => personStore
            .getById(sr.ownerId)
            .then(p => vm.owner = p))
        .then(() => surveyInstanceStore.findPreviousVersions(vm.surveyInstance.originalInstanceId || id))
        .then(prevVersionInstances => {
            const prevVersions = _.chain(prevVersionInstances)
                .sortBy('submittedAt')
                .map((pv, i) => ({
                    versionNum: `${i + 1}.0`,
                    instanceId: pv.id,
                    isLatest: false
                }))
                .reverse()
                .value();

            const latestInstanceId = vm.surveyInstance.originalInstanceId || id;
            const latestResponseVersion = {
                versionNum: `${prevVersions.length + 1}.0`,
                instanceId: latestInstanceId,
                isLatest: true
            };

            const allResponseVersions = [latestResponseVersion].concat(prevVersions);
            vm.currentResponseVersion = _.keyBy(allResponseVersions, 'instanceId')[id];
            vm.otherResponseVersions = _.filter(
                allResponseVersions,
                rv => rv.instanceId !== vm.currentResponseVersion.instanceId);
        });


    const loadParticipants = responses => {
        vm.participants = [];
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
        .then(qis => vm.surveyQuestionInfos = groupQuestions(qis));

    surveyInstanceStore
        .findResponses(id)
        .then(responses => {
            vm.answers = indexResponses(responses);
            loadParticipants(responses);
        });

    vm.reject = () => {
        const reason = prompt('Are you sure you want reject this survey? Please enter a reason below (mandatory):');

        if (reason) {
            surveyInstanceStore.updateStatus(
                vm.surveyInstance.id,
                {
                    newStatus: 'REJECTED',
                    reason
                }
            )
            .then(result => {
                notification.success('Survey response rejected');
                $state.reload();
            });
        }
    };

    vm.approve = () => {
        const reason = prompt('Are you sure you want to approve this survey? Please enter a reason below (optional):');

        if (!_.isNil(reason)) {
            surveyInstanceStore.markApproved(vm.surveyInstance.id, {
                newStringVal: (_.isEmpty(reason) ? null : reason)
            })
            .then(result => {
                notification.success('Survey response approved');
                $state.reload();
            });
        }
    };

    vm.viewOtherResponseVersion = (otherVer) => {
        $state.go('main.survey.instance.response.view', {id: otherVer.instanceId});
    };
}


controller.$inject = [
    '$state',
    '$stateParams',
    'Notification',
    'PersonStore',
    'SurveyInstanceStore',
    'SurveyRunStore',
    'SurveyQuestionStore'
];


const view = {
    controller,
    controllerAs: 'ctrl',
    template
};

export default view;