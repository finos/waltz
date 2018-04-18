/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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
