import {initialiseData, mkEntityLinkGridCell, mkLinkGridCell, termSearch, invokeFunction} from "../../../common";

const bindings = {
    lineage: '<',
    onInitialise: '<'
};


const template = require('./physical-flow-table.html');


const initialState = {
    filteredLineage: []
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        vm.filteredLineage = vm.lineage;
    };


    const fields = [
        'targetEntity.name',
        'flow.description',
        'flow.basisOffset',
        'flow.transport',
        'flow.frequency',
        'specification.name',
        'specification.externalId',
        'specification.format',
        'specification.description',
        'specification.owningEntity.name',
    ];


    vm.filterLineage = query => {
        vm.filteredLineage = termSearch(vm.lineage, query, fields);
    };


    vm.columnDefs = [
        mkLinkGridCell('Name', 'specification.name', 'flow.id', 'main.physical-flow.view'),
        mkEntityLinkGridCell('Source', 'sourceEntity', 'left'),
        mkEntityLinkGridCell('Target', 'targetEntity', 'left'),
        {
            field: 'specification.format',
            name: 'Format',
            cellFilter: 'toDisplayName:"dataFormatKind"'
        }
    ];


    vm.onGridInitialise = (api) => {
        vm.gridApi = api;
    };


    vm.exportGrid = () => {
        vm.gridApi.exportFn('lineage-reports.csv');
    };

    invokeFunction(vm.onInitialise, {export: vm.exportGrid });

}


const component = {
    bindings,
    template,
    controller
};


export default component;