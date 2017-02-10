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
import {mkEntityLinkGridCell, mkLinkGridCell} from "../common/link-utils";
import {timeFormat} from "d3-time-format";
import _ from "lodash";

const initialState = {
    surveyInstancesAndRuns: []
};

const template = require('./survey-instance-list-user-view.html');

function mkGridData(surveyRuns = [], surveyInstances = []) {
    const runsById = _.keyBy(surveyRuns, 'id');

    return _.map(surveyInstances, instance => {
        return {
            'surveyInstance': instance,
            'surveyRun': runsById[instance.surveyRunId],
            'surveyEntity': instance.surveyEntity
        }
    });
}


function controller($q,
                    surveyInstanceStore,
                    surveyRunStore,
                    userService) {

    const vm = initialiseData(this, initialState);

    userService.whoami()
        .then(user => vm.user = user);

    const surveyRunsPromise = surveyRunStore.findForUser();
    const surveyInstancesPromise = surveyInstanceStore.findForUser();

    $q.all([surveyRunsPromise, surveyInstancesPromise])
        .then(([surveyRuns, surveyInstances]) => {
            vm.gridData = mkGridData(surveyRuns, surveyInstances);
        });

    vm.columnDefs = [
        Object.assign(
            mkLinkGridCell('Survey', 'surveyRun.name', 'surveyInstance.id', 'main.survey.instance.response.edit'),
            { width: "30%" }
        ),
        Object.assign(
            mkEntityLinkGridCell('Survey Subject', 'surveyEntity'),
            { width: "30%" }
        ),
        { field: 'surveyInstance.status', displayName: 'Status', cellFilter: "toDisplayName:'surveyInstanceStatus'", width: "13%"},
        { field: 'surveyRun.issuedOn', displayName: 'Issued On', width: "13%"},
        { field: 'surveyRun.dueDate', displayName: 'Due Date', sort: { direction: 'desc' }, width: "13%" }
    ];
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

