import {initialiseData, termSearch, invokeFunction} from "../../../common";

const bindings = {
    lineage: '<',
    onInitialise: '<'
};


const template = require('./flow-table.html');


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
        {
            field: 'specification.name',
            name: 'Name',
            cellTemplate: '<div class="ui-grid-cell-contents"><a ui-sref="main.physical-flow.view ({ id: row.entity[\'flow\'].id })"><span ng-bind="COL_FIELD"></span></a></div>'
        },
        {
            field: 'specification.owningEntity.name',
            name: 'Source',
            cellTemplate: '<div class="ui-grid-cell-contents"><waltz-entity-link entity-ref="row.entity[\'specification\'].owningEntity"></waltz-entity-link></div>'
        },
        {
            field: 'targetEntity.name',
            name: 'Target',
            cellTemplate: '<div class="ui-grid-cell-contents"><waltz-entity-link entity-ref="row.entity[\'targetEntity\']"></waltz-entity-link></div>'
        },
        {
            field: 'specification.format',
            name: 'Format',
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