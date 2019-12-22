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
import { CORE_API } from '../../../common/services/core-api-utils';

import { initialiseData } from '../../../common';
import { invokeFunction } from '../../../common/index';

import template from './bulk-physical-flow-uploader.html';


const bindings = {
    flowCommands: '<',

    onInitialise: '<',
    onUploadComplete: '<'
};


const initialState = {
    columnDefs: [],
    loading: false,
    uploadedFlows: [],

    onInitialise: (event) => console.log('default onInitialise handler for bulk-physical-flow-uploader: ', event),
    onUploadComplete: () => console.log('default onUploadComplete handler for bulk-physical-flow-uploader')
};


function mkEntityLinkColumnDef(columnHeading, entityRefField) {
    return {
        field: 'parsedFlow.' + entityRefField + '.name',
        displayName: columnHeading,
        cellTemplate: `
            <div class="ui-grid-cell-contents">
                <waltz-entity-link ng-if="row.entity.parsedFlow.${entityRefField}"
                                   entity-ref="row.entity.parsedFlow.${entityRefField}"
                                   target="_blank">
                </waltz-entity-link>
                <span ng-if="row.entity.errors.${entityRefField}"
                      class="text-danger bg-danger">
                    <strong ng-bind="row.entity.errors.${entityRefField}"></strong>
                </span>
            </div>`
    };
}


function mkColumnDef(columnHeading, entityRefField) {
    return {
        field: 'parsedFlow.' + entityRefField,
        displayName: columnHeading,
        cellTemplate: `
            <div class="ui-grid-cell-contents">
                <span ng-if="row.entity.parsedFlow.${entityRefField}"
                      ng-bind="row.entity.parsedFlow.${entityRefField}">
                </span>
                <span ng-if="row.entity.errors.${entityRefField}"
                      class="text-danger bg-danger">
                    <strong ng-bind="row.entity.errors.${entityRefField}"></strong>
                </span>
            </div>`
    };
}


function mkColumnDefs() {
    return [
        {
            field: 'parsedFlow.name',
            displayName: 'Name',
            width: '25%',
            cellTemplate: `
                <div class="ui-grid-cell-contents">
                    <a ng-if="row.entity.entityReference.id"
                       ng-bind="row.entity.parsedFlow.name"
                       ui-sref="main.physical-flow.view ({ id: row.entity.entityReference.id })"
                       target="_blank"></a>
                </div>`
        },
        mkEntityLinkColumnDef('Source', 'source'),
        mkEntityLinkColumnDef('Target', 'target'),
        mkColumnDef('Format', 'format'),
        mkColumnDef('Frequency', 'frequency'),
        mkColumnDef('Basis Offset', 'basisOffset'),
        mkColumnDef('Transport', 'transport'),
        mkColumnDef('Criticality', 'criticality'),
        mkEntityLinkColumnDef('Data Type', 'dataType'),
        mkColumnDef('External ID', 'externalId'),
    ];
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const uploadFlows = () => {
        if(vm.flowCommands && !_.isEmpty(vm.flowCommands)) {
            vm.loading = true;

            serviceBroker
                .execute(CORE_API.PhysicalFlowStore.upload, [vm.flowCommands])
                .then(r => {
                    vm.loading = false;
                    vm.uploadedFlows = r.data;
                })
                .then(() => invokeFunction(vm.onUploadComplete, vm.uploadedFlows));
        }
    };

    vm.$onInit = () => {
        vm.columnDefs = mkColumnDefs();

        const api = {
            uploadFlows
        };
        invokeFunction(vm.onInitialise, api);
    };
}


controller.$inject = [
    'ServiceBroker'
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzBulkPhysicalFlowUploader'
};
