/*
 * Waltz - Enterprise Architecture
 *  Copyright (C) 2016, 2017 Waltz open source project
 *  See README.md for more information
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import moment from "moment";


import template from "./survey-instance-list.html";


const bindings = {
    parentEntityRef: '<'
};


const initialState = {
    surveys: [],
    visibility: {
        dataTab: 0
    }
};


function mkTableData(surveyRuns = [], surveyInstances = []) {
    const runsById = _.keyBy(surveyRuns, 'id');

    const surveys = _.map(surveyInstances, instance => {
        return {
            'surveyInstance': instance,
            'surveyRun': runsById[instance.surveyRunId],
            'surveyEntity': instance.surveyEntity
        }
    });

    const now = moment();
    const grouped = _.groupBy(surveys, s => {
        const subMoment = moment(s.surveyInstance.submittedAt);
        return s.surveyInstance.status == "WITHDRAWN" || now.diff(subMoment, 'months') >= 12 ? 'ARCHIVE' : 'CURRENT'
    });
    return grouped;
}


function controller($q, serviceBroker) {

    const vm = initialiseData(this, initialState);

    vm.showTab = (idx) => {
        vm.visibility.dataTab = idx;
    };


    vm.$onChanges = () => {
        if (vm.parentEntityRef) {
            const runsPromise = serviceBroker
                .loadViewData(CORE_API.SurveyRunStore.findByEntityReference, [vm.parentEntityRef])
                .then(r => r.data);

            const instancesPromise = serviceBroker
                .loadViewData(CORE_API.SurveyInstanceStore.findByEntityReference, [vm.parentEntityRef])
                .then(r => r.data);

            $q.all([runsPromise, instancesPromise])
                .then(([runs, instances]) =>
                    vm.surveys = mkTableData(runs, instances));
        }
    };

}


controller.$inject = [
    '$q',
    'ServiceBroker'
];


const component = {
    bindings,
    template,
    controller
};

export default {
    component,
    id: 'waltzSurveyInstanceList'
};



