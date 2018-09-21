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

import {initialiseData} from "../../../common";
import template from "./grid.html";


const bindings = {
    columnDefs: "<",
    rowData: "<",
    rowTemplate: "<",
    onInitialise: "<",
    scopeProvider: "<?",
    onRowSelect: "<"
};


const exportDataSeparator = ",";


const initialState = {
    columnDefs: [],
    rowData: [],
    minRowsToShow: 10,
    rowTemplate: null,
    scopeProvider: null,
    onInitialise: (e) => {}
  };


function controller(uiGridExporterConstants,
                    uiGridExporterService) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        vm.gridOptions = {
            appScopeProvider: vm.scopeProvider,
            columnDefs: vm.columnDefs,
            data: vm.rowData,
            enableColumnMenus: false,
            enableGridMenu: false,
            minRowsToShow: vm.minRowsToShow,
            enableRowHeaderSelection: false,
            enableRowSelection: vm.onRowSelect ? true: false,
            onRegisterApi: function(gridApi){
                vm.gridApi = gridApi;
                vm.onInitialise({
                    exportFn: vm.exportData,
                    gridApi: vm.gridApi
                });

                if (vm.onRowSelect) {
                    gridApi.selection.setMultiSelect(false);
                    gridApi.selection.toggleRowSelection(true);
                    gridApi.selection.on.rowSelectionChanged(null, function(row){
                        vm.onRowSelect(_.first(gridApi.selection.getSelectedRows()));
                    });

                }

            },
            exporterFieldCallback: function (grid, row, col, input) {
                const formatter = col.colDef.exportFormatter;
                return formatter
                    ? formatter(input)
                    : input;
            },
            rowTemplate: vm.rowTemplate
        };
    };


    vm.$onChanges = (changes) => {
        if (! vm.gridOptions) return;

        if (changes.columnDefs) {
            vm.gridOptions.columnDefs = vm.columnDefs;
        }

        vm.gridOptions.minRowsToShow = Math.min(vm.minRowsToShow, vm.rowData.length);
        vm.gridOptions.data = vm.rowData;
    };

    vm.exportData = (fileName = "download.csv") => {
        const grid = vm.gridApi.grid;
        const rowVisibility = uiGridExporterConstants.ALL;
        const colVisibility = uiGridExporterConstants.ALL;

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
    "uiGridExporterConstants",
    "uiGridExporterService"
];


const component = {
    bindings,
    template,
    controller
};


export default component;