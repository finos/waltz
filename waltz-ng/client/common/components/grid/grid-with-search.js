import _ from "lodash";
import {initialiseData, invokeFunction, termSearch} from "../../../common";

const bindings = {
    columnDefs: '<',
    entries: '<',
    searchPlaceholderText: '@',
    exportFilename: '@',
    onInitialise: '<',
    onChange: '<'
};

const template = require('./grid-with-search.html');


const initialState = {
    columnDefs: [],
    entries: [],
    filteredEntries: [],
    searchFields: [],
    searchPlaceholderText: 'Search...',
    onInitialise: (gridApi) => console.log('Default onInitialise handler for grid-search: ', gridApi),
    onChange: (gridApi) => {}
};


function mkSearchFields(columnDefs = []) {
    return _.map(columnDefs, "field");
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        vm.filteredEntries = vm.entries;
        vm.searchFields = mkSearchFields(vm.columnDefs);
        invokeFunction(vm.onChange, { entriesCount: _.size(vm.filteredEntries) });
    };


    vm.filterEntries = query => {
        vm.filteredEntries = termSearch(vm.entries, query, vm.searchFields);
        invokeFunction(vm.onChange, { entriesCount: _.size(vm.filteredEntries) });
    };

}


const component = {
    bindings,
    template,
    controller
};


export default component;
