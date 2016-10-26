import {initialiseData, termSearch} from "../../../common";

const bindings = {
    lineage: '<'
};


const template = require('./flow-table.html');


const initialState = {
    filteredLineage: []
};


function controller($animate,
                    uiGridConstants) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        vm.filterLineage("");
        vm.gridOptions.data = vm.filteredLineage;
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
        vm.gridOptions.data = vm.filteredLineage;
    };


    vm.gridOptions = {
        enableGridMenu: true,
        exporterMenuPdf: false,
        enableSorting: true,
        enableFiltering: false,
        enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
        onRegisterApi: (gridApi) => {
            $animate.enabled(gridApi.grid.element, false);
        },
        columnDefs: [
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
                cellTemplate: '<div class="ui-grid-cell-contents"><span ng-bind="COL_FIELD"></span></div>'
            }
        ],
        data: vm.filteredLineage
    };

}


controller.$inject = [
    '$animate',
    'uiGridConstants'
];


const component = {
    bindings,
    template,
    controller
};


export default component;