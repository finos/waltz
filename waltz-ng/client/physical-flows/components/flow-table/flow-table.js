import _ from "lodash";
import {initialiseData, termSearch} from "../../../common";

const bindings = {
    physicalFlows: '<',
    specifications: '<'
};


const template = require('./flow-table.html');


const initialState = {
    physicalFlows: [],
    specifications: [],
    filteredFlows: []
};


function mkData(physicalFlows = [], specifications = []) {
    const specsById = _.keyBy(specifications, 'id');
    return _.map(physicalFlows, f => Object.assign({}, f, { specification: specsById[f.specificationId] }));
}


function controller($animate,
                    uiGridConstants) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        vm.flows = mkData(vm.physicalFlows, vm.specifications);
        vm.filterFlows("");
        vm.gridOptions.data = vm.filteredFlows;
    };


    const fields = [
        'target.name',
        'description',
        'basisOffset',
        'transport',
        'frequency',
        'specification.name',
        'specification.externalId',
        'specification.format',
        'specification.description',
        'specification.owningEntity.name',
    ];


    vm.filterFlows = query => {
        vm.filteredFlows = termSearch(vm.flows, query, fields);
        vm.gridOptions.data = vm.filteredFlows;
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
                cellTemplate: '<div class="ui-grid-cell-contents"><a ui-sref="main.physical-flow.view ({ id: row.entity[\'id\'] })"><span ng-bind="COL_FIELD"></span></a></div>'
            },
            {
                field: 'specification.owningEntity.name',
                name: 'Source',
                cellTemplate: '<div class="ui-grid-cell-contents"><waltz-entity-link entity-ref="row.entity[\'specification\'].owningEntity"></waltz-entity-link></div>'
            },
            {
                field: 'target.name',
                name: 'Target',
                cellTemplate: '<div class="ui-grid-cell-contents"><waltz-entity-link entity-ref="row.entity[\'target\']"></waltz-entity-link></div>'
            },
            {
                field: 'specification.format',
                name: 'Format',
                cellTemplate: '<div class="ui-grid-cell-contents"><span ng-bind="COL_FIELD"></span></div>'
            }
        ],
        data: vm.filteredFlows
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