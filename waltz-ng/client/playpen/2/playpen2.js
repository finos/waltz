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


function controller(notification,
                    svgDiagramStore,
                    $element) {

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
        from_app_nar: {name: 'source', required: true},
        'Target code': {name: 'target', required: true},
        'Data Types': {name: 'dataType', required: true},
        'Source Pr': {name: 'provenance', required: false},
    };

    vm.spreadsheetLoaded = (event) => {
        console.log('spreadsheet loaded: ', event);
        vm.sourceData = event.rowData;
    };

    vm.parseComplete = (event) => {
        console.log('parse complete: ', event.data, event.isComplete());
    };

    vm.uploadComplete = (event) => {
        console.log('upload complete: ', event);
    };
}



controller.$inject = [
    'Notification',
    'SvgDiagramStore',
    '$element'
];


const view = {
    template: require('./playpen2.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;
