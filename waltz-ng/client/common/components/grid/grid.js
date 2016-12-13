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

import {initialiseData} from "../../../common";


const bindings = {
    columnDefs: '<',
    rowData: '<',
    rowTemplate: '<',
    onInitialise: '<'
};


const template = require('./grid.html');


const exportDataSeparator = ',';


const initialState = {
    columnDefs: [],
    rowData: [],
    minRowsToShow: 10,
    rowTemplate: null,
    onInitialise: (e) => {}
};


function controller(uiGridExporterConstants,
                    uiGridExporterService) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        vm.gridOptions.minRowsToShow = Math.min(vm.minRowsToShow, vm.rowData.length);
        vm.gridOptions.data = vm.rowData;
    };

    vm.exportData = (fileName = 'download.csv') => {
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

    vm.gridOptions = {
        columnDefs: vm.columnDefs,
        data: vm.rowData,
        enableGridMenu: false,
        enableColumnMenus: false,
        minRowsToShow: vm.minRowsToShow,
        onRegisterApi: function(gridApi){
            vm.gridApi = gridApi;
        },
        rowTemplate: vm.rowTemplate
    };

    // callback
    vm.onInitialise({
        exportFn: vm.exportData
    });
}


controller.$inject = [
    'uiGridExporterConstants',
    'uiGridExporterService'
];


const component = {
    bindings,
    template,
    controller
};


export default component;