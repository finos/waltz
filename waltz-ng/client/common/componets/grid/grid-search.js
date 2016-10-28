import _ from "lodash";
import {initialiseData, invokeFunction, termSearch} from "../../../common";

const bindings = {
    columnDefs: '<',
    entries: '<',
    searchPlaceholderText: '@',
    exportFilename: '@',
    onInitialise: '<'
};

const template = require('./grid-search.html');


const initialState = {
    columnDefs: [],
    entries: [],
    filteredEntries: [],
    searchFields: [],
    searchPlaceholderText: 'Search...',
    exportFilename: 'export.csv',
    onInitialise: (gridApi) => console.log('Default onOnitialise handler for grid-search: ', gridApi)
};


function mkSearchFields(columnDefs = []) {
    return _.map(columnDefs, "field");
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        vm.filteredEntries = vm.entries;
        vm.searchFields = mkSearchFields(vm.columnDefs);
    };


    vm.filterEntries = query => {
        vm.filteredEntries = termSearch(vm.entries, query, vm.searchFields);
    };


    vm.onGridInitialise = (api) => {
        vm.gridApi = api;
    };


    vm.exportGrid = () => {
        vm.gridApi.exportFn(vm.exportFilename);
    };


    invokeFunction(vm.onInitialise, {export: vm.exportGrid });
}


const component = {
    bindings,
    template,
    controller
};


export default component;
