/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";
import moment from "moment";
import {mkEntityLinkGridCell} from "../../common";


/**
 * Intended to show a table similar to:
 *
 *  | App Name (link) | Outcome | Reason | Update Date |
 *
 * This should only be used for values associated
 * with a single entity-statistic-definition.
 **/

const bindings = {
    applications: '<',
    filterOutcome: '<',
    lastUpdatedAt: '<',
    statisticDefinition: '<',
    statisticValues: '<'
};


const exportTimestampFormat = 'YYYY-MM-DD_HHmmss';


const appNameCell = Object.assign(
    mkEntityLinkGridCell('Application', 'entity', 'none'),
    { sort: { direction: 'asc' } }
);


const outcomeCell = (uiGridConstants) => {
    return {
        field: 'outcome',
        filter: {
            term: null,
            condition: uiGridConstants.filter.EXACT
        }
    }
};


const assetCodeCell = {
    field: 'application.assetCode',
    displayName: 'Asset Code'
};


const valueCell = (uiGridConstants, statisticDefinition) => {
    return {
        field: 'value',
        type: (statisticDefinition && statisticDefinition.type === 'NUMERIC')
            ? 'number'
            : 'string',
        filter: {
            term: null,
            condition: uiGridConstants.filter.EXACT
        }
    }
};


const reasonCell = {
    field: 'reason'
};


const dateCell = {
    field: 'createdAt',
    displayName: 'Last Updated',
    enableFiltering: false,
    visible: false,
    cellTemplate: '<div class="ui-grid-cell-contents">\n     <waltz-from-now timestamp="COL_FIELD"></waltz-from-now>\n</div>'
};


function controller($animate,
                    uiGridConstants,
                    uiGridExporterConstants,
                    uiGridExporterService) {
    const vm = this;

    vm.$onChanges = (change) => {
        vm.gridOptions = setupGrid();

        if (change.statisticValues) {
            vm.gridOptions.data = vm.statisticValues || [];
        }

        if(change.filterOutcome) {
            const tableOutcomeCell = vm.gridOptions.columnDefs[2];
            tableOutcomeCell.filter.term = vm.filterOutcome;
        }

        if(change.applications) {
            vm.appsById = _.keyBy(vm.applications, 'id');
        }

        if (vm.appsById && vm.statisticValues) {
            _.each(vm.statisticValues, sv => sv.application = vm.appsById[sv.entity.id]);
        }
    };

    const setupGrid = () => {
        return {
            enableGridMenu: false,
            enableSorting: true,
            enableFiltering: true,
            enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
            onRegisterApi: (gridApi) => {
                $animate.enabled(gridApi.grid.element, false);
                vm.gridApi = gridApi;
            },
            columnDefs: [
                appNameCell,
                assetCodeCell,
                outcomeCell(uiGridConstants),
                valueCell(uiGridConstants, vm.statisticDefinition),
                reasonCell,
                dateCell
            ]
        };
    };

    vm.exportData = () => {
        const grid = vm.gridApi.grid;
        const rowVisibility = uiGridExporterConstants.ALL;
        const colVisibility = uiGridExporterConstants.ALL;
        const exportDataSeparator = ',';
        const fileName = vm.statisticDefinition.name + '_' + moment().format(exportTimestampFormat) + '.csv';

        uiGridExporterService.loadAllDataIfNeeded(grid, rowVisibility, colVisibility)
            .then(() => {
                // prepare data
                const exportColumnHeaders = uiGridExporterService.getColumnHeaders(grid, colVisibility);
                const exportData = uiGridExporterService.getData(grid, rowVisibility, colVisibility);
                const csvContent = uiGridExporterService.formatAsCsv(exportColumnHeaders, exportData, exportDataSeparator);

                // trigger file download
                uiGridExporterService.downloadFile(fileName, csvContent, false);
            });
    };
}


controller.$inject = [
    '$animate',
    'uiGridConstants',
    'uiGridExporterConstants',
    'uiGridExporterService'
];


const template = require('./entity-statistic-detail-table.html');


const component = {
    controller,
    bindings,
    template
};


export default component;