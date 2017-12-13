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

import _ from "lodash";
import {termSearch, perhaps} from "../../../common";
import template from './technology-section.html';
import {CORE_API} from "../../../common/services/core-api-utils";


const bindings = {
    parentEntityRef: '<'
};


const EOL_CELL_TEMPLATE = '<div class="ui-grid-cell-contents"> <waltz-icon ng-if="COL_FIELD" name="power-off"></waltz-icon></div>';


function mkBooleanColumnFilter(uiGridConstants) {
    return {
        type: uiGridConstants.filter.SELECT,
        selectOptions: [
            {value: 'true', label: 'Yes'},
            {value: 'false', label: 'No'}
        ]
    };
}


function isEndOfLife(endOfLifeStatus) {
    return endOfLifeStatus === 'END_OF_LIFE';
}


function createDefaultTableOptions($animate, uiGridConstants, exportFileName = "export.csv") {
    return {
        columnDefs: [],
        data: [],
        enableGridMenu: true,
        enableFiltering: true,
        enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
        enableSorting: true,
        exporterCsvFilename: exportFileName,
        exporterMenuPdf: false,
        onRegisterApi: (gridApi) => {
            $animate.enabled(gridApi.grid.element, false);
        }
    };
}


function prepareServerGridOptions($animate, uiGridConstants) {

    const columnDefs = [
        { field: 'hostname', displayName: 'Host' },
        { field: 'environment' },
        {
            field: 'virtual',
            displayName: 'Virtual',
            width: "5%",
            filter: mkBooleanColumnFilter(uiGridConstants),
            cellTemplate: `
                <div class="ui-grid-cell-contents"> 
                    <waltz-icon ng-if="COL_FIELD" name="check"></waltz-icon>
                </div>`
        },
        { field: 'operatingSystem', displayName: 'OS' },
        { field: 'operatingSystemVersion', displayName: 'Version' },
        { field: 'location' },
        { field: 'country' },
        {
            field: 'isHwEndOfLife',
            displayName: 'h/w EOL',
            width: "6%",
            filter: mkBooleanColumnFilter(uiGridConstants),
            cellTemplate: EOL_CELL_TEMPLATE
        },
        { field: 'hardwareEndOfLifeDate', displayName: 'h/w EOL On' },
        {
            field: 'isOperatingSystemEndOfLife',
            displayName: 'OS EOL',
            width: "6%",
            filter: mkBooleanColumnFilter(uiGridConstants),
            cellTemplate: EOL_CELL_TEMPLATE
        },
        { field: 'operatingSystemEndOfLifeDate', displayName: 'OS EOL On' },
        {
            field: 'lifecycleStatus',
            displayName: 'Lifecycle',
            cellFilter: "toDisplayName:'lifecycleStatus'"
        }
    ];

    const baseTable = createDefaultTableOptions($animate, uiGridConstants, "server.csv");
    return _.extend(baseTable, {
        columnDefs,
        rowTemplate: '<div ng-class="{\'bg-danger\': row.entity.isHwEndOfLife || row.entity.isOperatingSystemEndOfLife}"><div ng-repeat="col in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div></div>'
    });
}


function prepareDatabaseGridOptions($animate, uiGridConstants) {

    const columnDefs = [
        { field: 'instanceName', displayName: 'Instance' },
        { field: 'databaseName', displayName: 'Database' },
        { field: 'environment' },
        { field: 'dbmsVendor', displayName: 'Vendor' },
        { field: 'dbmsName', displayName: 'Product Name' },
        { field: 'dbmsVersion', displayName: 'Version' },
        {
            field: 'isEndOfLife',
            displayName: 'EOL',
            width: "5%",
            filter: mkBooleanColumnFilter(uiGridConstants),
            cellTemplate: EOL_CELL_TEMPLATE
        },
        { field: 'endOfLifeDate', displayName: 'EOL On' },
        {
            field: 'lifecycleStatus',
            displayName: 'Lifecycle',
            cellFilter: "toDisplayName:'lifecycleStatus'"
        }
    ];

    const baseTable = createDefaultTableOptions($animate, uiGridConstants, "database.csv");
    return _.extend(baseTable, {
        columnDefs,
        rowTemplate: '<div ng-class="{\'bg-danger\': row.entity.isEndOfLife}"><div ng-repeat="col in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div></div>'
    });
}



function controller($animate, uiGridConstants, serviceBroker) {

    const vm = this;


    function refresh(qry) {
        if (qry) {
            vm.filteredServers = termSearch(vm.servers, qry);
            vm.filteredDatabases = termSearch(vm.databases, qry);
            console.log('sr', qry, vm.servers.length, vm.filteredServers.length)
        } else {
            vm.filteredServers = vm.servers;
            vm.filteredDatabases = vm.databases;
        }
    }

    vm.$onInit = () => {
        serviceBroker
            .loadViewData(
                CORE_API.ServerInfoStore.findByAppId,
                [ vm.parentEntityRef.id ])
            .then(r => {
                vm.servers = r.data;
                _.forEach(vm.servers,
                    (s) => Object.assign(s, {
                        "isHwEndOfLife": isEndOfLife(s.hardwareEndOfLifeStatus),
                        "isOperatingSystemEndOfLife": isEndOfLife(s.operatingSystemEndOfLifeStatus)
                    })
                );
                vm.serverGridOptions.data = vm.servers;
            })
            .then(() => refresh(vm.qry));

        serviceBroker
            .loadViewData(
                CORE_API.DatabaseStore.findByAppId,
                [ vm.parentEntityRef.id ])
            .then(r => {
                vm.databases = r.data;
                _.forEach(vm.databases,
                    (db) => Object.assign(db, {
                        "isEndOfLife": isEndOfLife(db.endOfLifeStatus)
                    })
                );
                vm.databaseGridOptions.data = vm.databases;
            })
            .then(() => refresh(vm.qry));

    };

    vm.serverGridOptions = prepareServerGridOptions($animate, uiGridConstants);
    vm.databaseGridOptions = prepareDatabaseGridOptions($animate, uiGridConstants);

    vm.doSearch = () => refresh(vm.qry);


    vm.hasAnyData = () => {
        const hasServers = perhaps(() => vm.servers.length > 0, false);
        const hasDatabases = perhaps(() => vm.databases.length > 0, false);
        return hasServers || hasDatabases;
    };
}


controller.$inject = [
    '$animate',
    'uiGridConstants',
    'ServiceBroker'
];


const component = {
    template,
    bindings,
    controller
};

export default {
    id: 'waltzTechnologySection',
    component
};

