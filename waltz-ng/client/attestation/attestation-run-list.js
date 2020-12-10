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
    entityRuns: [],
    groupRuns: [],
    responseSummaries: {}
};

const attestedKindColumnDef = {
        field: 'entityKind',
        displayName: 'Attested Kind',
        width: '10%',
        cellTemplate: `
                <div class="ui-grid-cell-contents"
                     style="vertical-align: baseline;">
                    <waltz-attested-kind run="row.entity"></waltz-attested-kind>
                </div>`
};

const descriptionColumnDef = {
        field: 'description',
        displayName: 'Description',
        width: '20%',
        cellTemplate: `
                <div class="ui-grid-cell-contents"
                     style="vertical-align: baseline;">
                     <div ng-bind="COL_FIELD | truncate:50"
                             uib-popover="{{ COL_FIELD }}"
                             popover-class="waltz-popover-width-500"
                             popover-append-to-body="true"
                             popover-placement="right"
                             popover-trigger="mouseenter">
                    </div>
                </div>`
};

const issuedByColumnDef = {
            field: 'issuedBy',
            displayName: 'Issuer',
            width: '10%',
};

const issuedOnColumnDef = {
            field: 'issuedOn',
            displayName: 'Issued',
            width: '10%',
            cellTemplate: `
                   <div class="ui-grid-cell-contents"
                        style="vertical-align: baseline;">
                        <waltz-from-now timestamp="COL_FIELD"
                                        days-only="true">
                        </waltz-from-now>
                    </div>`
};

const dueDateColumnDef = {
            field: 'dueDate',
            displayName: 'Due',
            width: '10%',
            cellTemplate: `
                    <div class="ui-grid-cell-contents"
                         style="vertical-align: baseline;">
                        <div ng-class="{'text-danger': grid.appScope.isOverdue(row.entity)}">
                            <waltz-from-now ng-if='COL_FIELD'
                                            timestamp="COL_FIELD"
                                            days-only="true">
                            </waltz-from-now>
                        </div>
                    </div>`
};

function mkGroupAttestationColumnDefs() {
    return [
        {
            field: 'name',
            displayName: 'Name',
            width: '30%',
            cellTemplate: `
                <div class="ui-grid-cell-contents"
                     style="vertical-align: baseline;">
                        <a ng-bind="COL_FIELD"
                           class="clickable"
                           ui-sref="main.attestation.run.view ({id: row.entity.id})">
                        </a> 
                </div>`
        },
        attestedKindColumnDef,
        descriptionColumnDef,
        {
            field: 'id',
            displayName: 'Responses',
            width: '10%',
            cellTemplate: `
                <div class="ui-grid-cell-contents"
                     style="vertical-align: baseline;">
                    <div uib-popover="{{
                            grid.appScope.responseSummaries[COL_FIELD].completeCount + ' completed'
                            + ', '
                            + grid.appScope.responseSummaries[COL_FIELD].pendingCount + ' pending'
                        }}"
                         popover-trigger="mouseenter">
                        <uib-progress max="grid.appScope.responseSummaries[COL_FIELD].completeCount + grid.appScope.responseSummaries[COL_FIELD].pendingCount"
                                      animate="false">
                            <uib-bar value="grid.appScope.responseSummaries[COL_FIELD].completeCount"
                                     ng-bind="grid.appScope.responseSummaries[COL_FIELD].completeCount"
                                     type="success">
                            </uib-bar>
                            <uib-bar value="grid.appScope.responseSummaries[COL_FIELD].pendingCount"
                                     ng-bind="grid.appScope.responseSummaries[COL_FIELD].pendingCount"
                                     type="{{grid.appScope.getPendingBarType(row.entity)}}">
                            </uib-bar>
                        </uib-progress>
                    </div>
                </div>`
        },
        issuedByColumnDef,
        issuedOnColumnDef,
        dueDateColumnDef
    ];
}

function mkEntityAttestationColumnDefs() {
    return [
        {
            field: 'name',
            displayName: 'Name',
            width: '10%',
            cellTemplate: `
                <div class="ui-grid-cell-contents"
                     style="vertical-align: baseline;">
                        <a ng-bind="COL_FIELD"
                           class="clickable"
                           ui-sref="main.attestation.run.view ({id: row.entity.id})">
                        </a> 
                </div>`
        },{
            field: 'selectionOptions.entityReference.name',
            displayName: 'Entity',
            width: '20%',
            cellTemplate: `
                <div class="ui-grid-cell-contents"
                     style="vertical-align: baseline;">
                        <div ng-bind="COL_FIELD"
                        </div> 
                </div>`
        },
        attestedKindColumnDef,
        descriptionColumnDef,
        {
            field: 'id',
            displayName: 'Responses',
            width: '10%',
            cellTemplate: `
                    <div class="ui-grid-cell-contents"
                         style="vertical-align: baseline;">
                         <div ng-bind="{{grid.appScope.responseSummaries[COL_FIELD].pendingCount}} == 0 ? 'Completed' : 'Pending'">
                         </div>
                     </div>`
        },
        issuedByColumnDef,
        issuedOnColumnDef,
        dueDateColumnDef
    ];
}

function isOverdue(run = {}) {
    const now = moment();
    const dueDate = moment.utc(run.dueDate, formats.parse);
    return now > dueDate;
}

function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.groupColumnDefs = mkGroupAttestationColumnDefs();
    vm.entityColumnDefs = mkEntityAttestationColumnDefs();

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
            .then(r => {
                vm.groupRuns = _.filter(r.data, r => r.name !== 'Entity Attestation');
                vm.entityRuns = _.filter(r.data, r => r.name === 'Entity Attestation');
            });

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