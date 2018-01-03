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
        {label: 'aT', required: true },
        {label: 'bT', required: true },
        {label: 'cT', required: false },
        {label: 'dT', required: true },
        {label: 'eT', required: false },
    ];

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
