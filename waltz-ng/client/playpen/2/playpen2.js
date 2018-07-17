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
import {CORE_API} from "../../common/services/core-api-utils";
import template from './playpen2.html';


function controller(notification,
                    svgDiagramStore,
                    $element,
                    serviceBroker) {

    const vm = Object.assign(this, {});

    const logicalFlowFields = [
        {name: 'id', required: false},
        {name: 'source', required: true},
        {name: 'target', required: true},
        {name: 'lastUpdatedAt', required: false},
        {name: 'lastUpdatedBy', required: true},
        {name: 'lastAttestedAt', required: false},
        {name: 'lastAttestedBy', required: false},
        {name: 'isRemoved', required: false},
        {name: 'provenance', required: false},
    ];

    const physicalSpecFields = [
        {name: 'id', required: false},
        {name: 'owner', required: true},
        {name: 'externalId', required: true},
        {name: 'name', required: true},
        {name: 'format', required: true},
        {name: 'description', required: false},
        {name: 'provenance', required: false}
    ];

    const physicalFlowFields = [
        {name: 'id', required: false},
        {name: 'basisOffset', required: true},
        {name: 'frequency', required: true},
        {name: 'transport', required: true},
        {name: 'criticality', required: true},
        {name: 'description', required: false},
        {name: 'provenance', required: false}
    ];

    vm.sourceColumns = ['aS', 'bS', 'cS', 'dS'];
    vm.targetColumns = [
        {name: 'aT', required: true },
        {name: 'bT', required: true },
        {name: 'cT', required: false },
        {name: 'dT', required: true },
        {name: 'eT', required: false },
    ];

    vm.newFlows = [
        {
            "source": {
                "id": 155,
                "kind": "APPLICATION",
                "name": "Australian Kelpie Dog - 154",
                "description": "All about Australian Kelpie Dog - 154"
            },
            "target": {
                "id": 4142,
                "kind": "APPLICATION",
                "name": "Guinea Fowl - 4141",
                "description": "All about Guinea Fowl - 4141"
            },
            "dataType": {
                "id": 3000,
                "kind": "DATA_TYPE",
                "name": "Pricing data",
                "description": "Pricing"
            },
            "provenance": "kam"
        },
        {
            "source": {
                "id": 155,
                "kind": "APPLICATION",
                "name": "Australian Kelpie Dog - 154",
                "description": "All about Australian Kelpie Dog - 154"
            },
            "target": {
                "id": 4142,
                "kind": "APPLICATION",
                "name": "Guinea Fowl - 4141",
                "description": "All about Guinea Fowl - 4141"
            },
            "dataType": {
                "id": 1000,
                "kind": "DATA_TYPE",
                "name": "Book Data",
                "description": "Book data"
            },
            "provenance": "kam"
        },
        {
            "source": {
                "id": 174,
                "kind": "APPLICATION",
                "name": "Australian Kelpie Dog - 173",
                "description": "All about Australian Kelpie Dog - 173"
            },
            "target": {
                "id": 1258,
                "kind": "APPLICATION",
                "name": "Beagle - 1257",
                "description": "All about Beagle - 1257"
            },
            "dataType": {
                "id": 1000,
                "kind": "DATA_TYPE",
                "name": "Book Data",
                "description": "Book data"
            },
            "provenance": "kam"
        },
        {
            "source": {
                "id": 161,
                "kind": "APPLICATION",
                "name": "Balinese - 160",
                "description": "All about Balinese - 160"
            },
            "target": {
                "id": 1245,
                "kind": "APPLICATION",
                "name": "English Cocker Spaniel - 1244",
                "description": "All about English Cocker Spaniel - 1244"
            },
            "dataType": {
                "id": 1000,
                "kind": "DATA_TYPE",
                "name": "Book Data",
                "description": "Book data"
            },
            "provenance": "kam"
        },
        {
            "source": {
                "id": 180,
                "kind": "APPLICATION",
                "name": "Bombay - 179",
                "description": "All about Bombay - 179"
            },
            "target": {
                "id": 1264,
                "kind": "APPLICATION",
                "name": "Caiman - 1263",
                "description": "All about Caiman - 1263"
            },
            "dataType": {
                "id": 6000,
                "kind": "DATA_TYPE",
                "name": "Currency",
                "description": "Currency"
            },
            "provenance": "kam"
        },
        {
            "source": {
                "id": 175,
                "kind": "APPLICATION",
                "name": "Bull Shark - 174",
                "description": "All about Bull Shark - 174"
            },
            "target": {
                "id": 1259,
                "kind": "APPLICATION",
                "name": "River Dolphin - 1258",
                "description": "All about River Dolphin - 1258"
            },
            "dataType": {
                "id": 6000,
                "kind": "DATA_TYPE",
                "name": "Currency",
                "description": "Currency"
            },
            "provenance": "kam"
        },
        {
            "source": {
                "id": 162,
                "kind": "APPLICATION",
                "name": "Bulldog - 161",
                "description": "All about Bulldog - 161"
            },
            "target": {
                "id": 1246,
                "kind": "APPLICATION",
                "name": "Cheetah - 1245",
                "description": "All about Cheetah - 1245"
            },
            "dataType": {
                "id": 6000,
                "kind": "DATA_TYPE",
                "name": "Currency",
                "description": "Currency"
            },
            "provenance": "kam"
        },
        {
            "source": {
                "id": 169,
                "kind": "APPLICATION",
                "name": "Cavalier King Charles Spaniel - 168",
                "description": "All about Cavalier King Charles Spaniel - 168"
            },
            "target": {
                "id": 1253,
                "kind": "APPLICATION",
                "name": "Golden Lion Tamarin - 1252",
                "description": "All about Golden Lion Tamarin - 1252"
            },
            "dataType": {
                "id": 1000,
                "kind": "DATA_TYPE",
                "name": "Book Data",
                "description": "Book data"
            },
            "provenance": "kam"
        }];

    vm.onMappingsChanged = (event) => {
        console.log('mappings changed: ', event.mappings, event.isComplete());
    };

    vm.columnMappings = {
        'source nar': {name: 'source', required: true},
        'target nar': {name: 'target', required: true},
        'name': {name: 'name', required: true},
        'external id': {name: 'externalId', required: true},
        'description': {name: 'description', required: true},
        'frequency': {name: 'frequency', required: true},
        'basis': {name: 'basisOffset', required: true},
        'format': {name: 'format', required: true},
        'transport': {name: 'transport', required: true},
        'criticality': {name: 'criticality', required: true},
    };

    vm.spreadsheetLoaded = (event) => {
        console.log('spreadsheet loaded: ', event);
        vm.sourceData = event.rowData;
    };

    vm.parserInitialised = (api) => {
        console.log('parser init')
        vm.parseFlows = api.parseFlows;
    };

    vm.parseComplete = (event) => {
        console.log('parse complete: ', event.data, event.isComplete());
        vm.validatedRows = event.data;
        vm.uploadCmds = _.map(vm.validatedRows, 'originalCommand');

        console.log('uploading: ', vm.uploadCmds);
        vm.uploadFlows();
    };

    vm.uploaderInitialised = (api) => {
        console.log('uploader initialised');
        vm.uploadFlows = api.uploadFlows;
    };

    vm.uploadComplete = (event) => {
        console.log('upload complete: ', event);
    };

    vm.validate = () => {
        const logicalFlow = {
            source: '109235-1',
            target: '21455-1'
        };

        const physicalSpecification = {
            owner: '109235-1',
            specExternalId: '',
            name: 'abcd_<YYYYMMDD>.csv',
            format: 'FLAT_FILE',
            specDescription: ''
        };

        const flowAttributes = {
            basisOffset: '-5',
            criticality: 'MEDIUM',
            description: 'Daily file with Waltz usage data',
            externalId: 'IF10293321',
            frequency: 'DAILY',
            transport: 'FILE_TRANSPORT',
        };

        // vm.uploadCmds = [Object.assign({}, logicalFlow, physicalSpecification, flowAttributes)];

        // console.log('validating: ', vm.uploadCmds);
        vm.parseFlows();
    };

    vm.generate = () => {
        const obj = {
            sourceColumns: vm.sourceColumns,
            targetColumns: vm.targetColumns
        };

        serviceBroker
            .loadViewData(CORE_API.SharedPreferenceStore.generateKeyRoute, [obj], { force: true })
            .then(r => {
                console.log('key: ', r.data);
                serviceBroker
                    .execute(CORE_API.SharedPreferenceStore.save, [r.data, 'physical-flow-upload-mapping', vm.columnMappings])
                    .then(r2 => console.log('r2: ', r2))
            });

    };
}



controller.$inject = [
    'Notification',
    'SvgDiagramStore',
    '$element',
    'ServiceBroker'
];


const view = {
    template,
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;
