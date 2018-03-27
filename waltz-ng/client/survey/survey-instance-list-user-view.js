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
import {initialiseData} from "../common/index";
import _ from "lodash";

const initialState = {
    surveyInstancesAndRuns: [],
    showSurveyTemplateButton: false
};

const template = require('./survey-instance-list-user-view.html');

function mkSurveyData(surveyRuns = [], surveyInstances = []) {
    const runsById = _.keyBy(surveyRuns, 'id');

    const mappedData = _.map(surveyInstances, instance => {
            return {
                'surveyInstance': instance,
                'surveyRun': runsById[instance.surveyRunId]
            }
        });

    const [incomplete = [], complete = []] = _.partition(mappedData,
        data => data.surveyInstance.status == 'NOT_STARTED'
        || data.surveyInstance.status == 'IN_PROGRESS'
        || data.surveyInstance.status == 'REJECTED');

    return {
        'incomplete': incomplete,
        'complete': complete
    };
}


function controller($q,
                    surveyInstanceStore,
                    surveyRunStore,
                    userService) {

    const vm = initialiseData(this, initialState);

    userService.whoami()
        .then(user => vm.user = user)
        .then(() => vm.showSurveyTemplateButton = userService.hasRole(vm.user, 'SURVEY_ADMIN')
            || userService.hasRole(vm.user, 'SURVEY_TEMPLATE_ADMIN'));

    const surveyRunsPromise = surveyRunStore.findForUser();
    const surveyInstancesPromise = surveyInstanceStore.findForUser();

    $q.all([surveyRunsPromise, surveyInstancesPromise])
        .then(([surveyRuns, surveyInstances]) => {
            vm.surveys = mkSurveyData(surveyRuns, surveyInstances);
        });
}


controller.$inject = [
    '$q',
    'SurveyInstanceStore',
    'SurveyRunStore',
    'UserService'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};

