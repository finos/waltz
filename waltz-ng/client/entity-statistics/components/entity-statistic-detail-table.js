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
import _ from "lodash";
import moment from "moment";
import {notEmpty} from "../../common";
import {mkEntityLinkGridCell, mkLinkGridCell} from "../../common/grid-utils";
import template from "./entity-statistic-detail-table.html";


/**
 * Intended to show a table similar to:
 *
 *  | App Name (link) | Outcome | Reason | Update Date |
 *
 * This should only be used for values associated
 * with a single entity-statistic-definition.
 **/

const bindings = {
    applications: "<",
    filterOutcome: "<",
    lastUpdatedAt: "<",
    orgUnits: "<",
    statisticDefinition: "<",
    statisticValues: "<"
};


const exportTimestampFormat = "YYYY-MM-DD_HHmmss";


const appNameCell = Object.assign(
    mkEntityLinkGridCell("Application", "entity", "none"),
    { sort: { direction: "asc" } }
);


const outcomeCell = (uiGridConstants) => {
    return {
        field: "outcome",
        filter: {
            term: null,
            condition: uiGridConstants.filter.EXACT
        }
    };
};


const assetCodeCell = {
    field: "application.assetCode",
    displayName: "Asset Code"
};


const valueCell = (uiGridConstants, statisticDefinition) => {
    return {
        field: "value",
        type: (statisticDefinition && statisticDefinition.type === "NUMERIC")
            ? "number"
            : "string",
        filter: {
            term: null,
            condition: uiGridConstants.filter.EXACT
        }
    }
};


const reasonCell = {
    field: "reason"
};


const orgUnitCell = mkLinkGridCell(
    "Owning Org Unit",
    "orgUnit.name",
    "orgUnit.id",
    "main.org-unit.view");


const dateCell = {
    field: "createdAt",
    displayName: "Last Updated",
    enableFiltering: false,
    visible: false,
    cellTemplate: "<div class=\"ui-grid-cell-contents\">\n     <waltz-from-now timestamp=\"COL_FIELD\"></waltz-from-now>\n</div>"
};


function mkGridData(statisticValues = [],
                    applications = [],
                    orgUnits = []) {
    const appsById = _.keyBy(applications, "id");
    const orgUnitsById = _.keyBy(orgUnits, "id");

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
        const exportDataSeparator = ",";
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
    "$animate",
    "uiGridConstants",
    "uiGridExporterConstants",
    "uiGridExporterService"
];


const component = {
    controller,
    bindings,
    template
};


export default component;