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

import {initialiseData, invokeFunction} from "../../../common";
import template from "./grid.html";
import {markdownToHtml} from "../../markdown-utils";


const bindings = {
    columnDefs: "<",
    rowData: "<",
    rowTemplate: "<",
    onInitialise: "<?",
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
    onInitialise: () => {}
};

function controller(uiGridExporterConstants,
                    uiGridExporterService,
                    $scope) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        vm.gridOptions = {
            appScopeProvider: vm.scopeProvider,
            columnDefs: vm.columnDefs,
            data: vm.rowData,
            enableColumnMenus: true,
            enableColumnResizing: true,
            enableGridMenu: false,
            minRowsToShow: vm.minRowsToShow,
            enableRowHeaderSelection: false,
            enableRowSelection: vm.onRowSelect
                ? true
                : false,
            onRegisterApi: function(gridApi){
                vm.gridApi = gridApi;

                invokeFunction(
                    vm.onInitialise,
                    {
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
        if (!vm.gridOptions) return;

        if (changes.columnDefs) {
            vm.gridOptions.columnDefs = vm.columnDefs;
        }

        vm.gridOptions.minRowsToShow = Math.min(vm.minRowsToShow, vm.rowData.length);
        vm.gridOptions.data = vm.rowData;
    };

    $scope.markdownToHtml = (str) => {
        return markdownToHtml(str);
    }


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
    "uiGridExporterService",
    "$scope"
];


const component = {
    bindings,
    template,
    controller
};


export default component;