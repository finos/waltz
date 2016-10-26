import {initialiseData} from "../../../common";


const bindings = {
    columnDefs: '<',
    rowData: '<',
    onInitialise: '<'
};


const template = require('./grid.html');


const exportDataSeparator = ',';


const initialState = {
    columnDefs: [],
    rowData: [],
    onInitialise: (e) => {}
};


function controller(uiGridExporterConstants,
                    uiGridExporterService) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
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
        onRegisterApi: function(gridApi){
            vm.gridApi = gridApi;
        }
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