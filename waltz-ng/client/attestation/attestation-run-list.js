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

import _ from 'lodash';
import moment from 'moment';

import {formats, initialiseData} from "../common/index";

import template from './attestation-run-list.html';
import {CORE_API} from "../common/services/core-api-utils";


const initialState = {
    runs: [],
    responseSummaries: {}
};

function mkColumnDefs() {
    return [
        {
            field: 'name',
            displayName: 'Name',
            width: '25%',
            cellTemplate: `
                <div class="ui-grid-cell-contents">
                    <a ng-if="row.entity.entityReference.id"
                       ng-bind="row.entity.parsedFlow.name"
                       ui-sref="main.physical-flow.view ({ id: row.entity.entityReference.id })"
                       target="_blank"></a>
                </div>`
        }, {
            field: 'entityKind',
            displayName: 'Attested Kind',
            width: '25%',
            cellTemplate: `<waltz-attested-kind run="row"></waltz-attested-kind>`
        },
        {
            field: 'description',
            displayName: 'Description',
            width: '25%',
            cellTemplate: ``
        },
        {
            field: 'description',
            displayName: 'Description',
            width: '25%',
            cellTemplate: `
                    <div uib-popover="{{
                            ctrl.responseSummaries[r.id].completeCount + ' completed'
                            + ', '
                            + ctrl.responseSummaries[r.id].pendingCount + ' pending'
                        }}"
                         popover-trigger="mouseenter">
                        <uib-progress max="ctrl.responseSummaries[r.id].completeCount + ctrl.responseSummaries[r.id].pendingCount"
                                      animate="false">
                            <uib-bar value="ctrl.responseSummaries[r.id].completeCount"
                                     ng-bind="ctrl.responseSummaries[r.id].completeCount"
                                     type="success">
                            </uib-bar>
                            <uib-bar value="ctrl.responseSummaries[r.id].pendingCount"
                                     ng-bind="ctrl.responseSummaries[r.id].pendingCount"
                                     type="{{ctrl.getPendingBarType(r)}}">
                            </uib-bar>
                        </uib-progress>
                    </div>`
        }
    ];
}

function isOverdue(run = {}) {
    const now = moment();
    const dueDate = moment.utc(run.dueDate, formats.parse);
    return now > dueDate;
}

function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.columnDefs = mkColumnDefs();

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
    controllerAs: '$ctrl',
    template
};


export default page;