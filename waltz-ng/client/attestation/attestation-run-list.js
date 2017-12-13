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

import _ from 'lodash';
import moment from 'moment';

import {formats, initialiseData} from "../common/index";

import template from './attestation-run-list.html';
import {CORE_API} from "../common/services/core-api-utils";


const initialState = {
    runs: [],
    responseSummaries: {}
};

function isOverdue(run = {}) {
    const now = moment();
    const dueDate = moment.utc(run.dueDate, formats.parse);
    return now > dueDate;
}

function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.getPendingBarType = (run) => {
        if(isOverdue(run)) {
            return 'danger';
        } else {
            return 'warning';
        }
    };

    vm.isOverdue = (run) => isOverdue(run);

    const loadData = () => {
        serviceBroker.loadViewData(CORE_API.AttestationRunStore.findAll)
            .then(r => vm.runs = r.data);

        serviceBroker.loadViewData(CORE_API.AttestationRunStore.findResponseSummaries)
            .then(r => vm.responseSummaries = _.keyBy(r.data, 'runId'));
    };

    loadData();
}


controller.$inject = [
    'ServiceBroker'
];


const page = {
    controller,
    controllerAs: 'ctrl',
    template
};


export default page;