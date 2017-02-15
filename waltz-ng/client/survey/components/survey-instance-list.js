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
import {initialiseData} from "../../common/index";
import {mkLinkGridCell} from "../../common/link-utils";
import _ from "lodash";

const initialState = {
    surveyInstancesAndRuns: []
};

const bindings = {
    surveyInstances: '<',
    surveyRuns: '<'
};


const template = require('./survey-instance-list.html');

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


function controller() {

    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        if (vm.surveyInstances && vm.surveyRuns) {
            vm.gridData = mkGridData(vm.surveyRuns, vm.surveyInstances);
        }
    };

    vm.columnDefs = [
        Object.assign(
            mkLinkGridCell('Title', 'surveyRun.name', 'surveyInstance.id', 'main.survey.instance.view'),
            {width: "30%"}
        ),
        {field: 'surveyRun.description', displayName: 'Description', width: "44%"},
        {field: 'surveyRun.issuedOn', displayName: 'Issued On', width: "13%"},
        {field: 'surveyRun.dueDate', displayName: 'Due Date', sort: {direction: 'desc'}, width: "13%"}
    ];
}


controller.$inject = [];


export default {
    bindings,
    template,
    controller
};


