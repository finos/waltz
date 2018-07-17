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
import moment from "moment";
import {notEmpty} from "../../common";
import {mkEntityLinkGridCell, mkLinkGridCell} from "../../common/grid-utils";
import template from './entity-statistic-detail-table.html';


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
    orgUnits: '<',
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


const orgUnitCell = mkLinkGridCell(
    'Org Unit',
    'orgUnit.name',
    'orgUnit.id',
    'main.org-unit.view');


const dateCell = {
    field: 'createdAt',
    displayName: 'Last Updated',
    enableFiltering: false,
    visible: false,
    cellTemplate: '<div class="ui-grid-cell-contents">\n     <waltz-from-now timestamp="COL_FIELD"></waltz-from-now>\n</div>'
};


function mkGridData(statisticValues = [],
                    applications = [],
                    orgUnits = []) {
    const appsById = _.keyBy(applications, 'id');
    const orgUnitsById = _.keyBy(orgUnits, 'id');

    return _.map(statisticValues, sv => {
        const app = appsById[sv.entity.id];
        return app
            ? Object.assign(
                {},
                sv,
                {
                    application: app,
                    orgUnit: orgUnitsById[app.organisationalUnitId]
                })
            : sv;
    });
}


function controller($animate,
                    uiGridConstants,
                    uiGridExporterConstants,
                    uiGridExporterService) {
    const vm = this;

    vm.$onChanges = (change) => {
        vm.gridOptions = setupGrid();

        if (change.filterOutcome) {
            const tableOutcomeCell = vm.gridOptions.columnDefs[2];
            tableOutcomeCell.filter.term = vm.filterOutcome;
        }

        if (vm.statisticValues
                && notEmpty(vm.applications)
                && notEmpty(vm.orgUnits)) {

            vm.gridOptions.data = mkGridData(
                vm.statisticValues,
                vm.applications,
                vm.orgUnits);
        }
    };

    const setupGrid = () => {
        return {
            enableGridMenu: false,
            enableSorting: true,
            enableFiltering: true,
            enableColumnMenus: false,
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
                orgUnitCell,
                dateCell
            ]
        };
    };

    vm.exportData = () => {
        const grid = vm.gridApi.grid;
        const rowVisibility = uiGridExporterConstants.ALL;
        const colVisibility = uiGridExporterConstants.ALL;
        const exportDataSeparator = ',';
        const statName = vm.statisticDefinition.name;
        const timestamp = moment().format(exportTimestampFormat);
        const fileName = `${statName}_${timestamp}.csv`;

        uiGridExporterService.loadAllDataIfNeeded(grid, rowVisibility, colVisibility)
            .then(() => {
                // prepare data
                const exportColumnHeaders = uiGridExporterService.getColumnHeaders(
                    grid,
                    colVisibility);
                const exportData = uiGridExporterService.getData(
                    grid,
                    rowVisibility,
                    colVisibility);
                const csvContent = uiGridExporterService.formatAsCsv(
                    exportColumnHeaders,
                    exportData,
                    exportDataSeparator);

                // trigger file download
                uiGridExporterService.downloadFile(
                    fileName,
                    csvContent,
                    false);
            });
    };
}


controller.$inject = [
    '$animate',
    'uiGridConstants',
    'uiGridExporterConstants',
    'uiGridExporterService'
];


const component = {
    controller,
    bindings,
    template
};


export default component;