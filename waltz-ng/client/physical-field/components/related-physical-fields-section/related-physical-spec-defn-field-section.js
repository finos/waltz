import _ from "lodash";
import {CORE_API} from "../../../common/services/core-api-utils";
import {initialiseData} from "../../../common";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {mkLinkGridCell} from "../../../common/grid-utils";

import template from "./related-physical-spec-defn-field-section.html";


const bindings = {
    parentEntityRef: '<',
};


const initialState = {
    selector: null,
    columnDefs: [],
    data: [],
    physicalFields: [],
    physicalSpecs: [],
    physicalSpecDefns: []
};


function mkColumnDefs() {
    return [
        {
            field: 'field.name',
            name: 'Name',
        },
        {
            field: 'field.type',
            name: 'Type',
        },
        Object.assign({},
            mkLinkGridCell('Specification', 'specification.name', 'specification.id', 'main.physical-specification.view'),
            { width: '20%'}
        ),
        {
            field: 'specification.format',
            name: 'Format',
        },
        {
            field: 'specDefn.version',
            name: 'Version',
        },
        {
            field: 'specDefn.status',
            name: 'Status',
        },
        {
            field: 'field.description',
            name: 'Description',
            width: '25%'
        }
    ];
}


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        vm.columnDefs = mkColumnDefs();
    };

    vm.$onChanges = (changes) => {
        if(changes.parentEntityRef && vm.parentEntityRef) {
            vm.selector = mkSelectionOptions(vm.parentEntityRef);
            const fieldsPromise = serviceBroker
                .loadViewData(CORE_API.PhysicalSpecDefinitionFieldStore.findBySelector, [vm.selector])
                .then(r => r.data);

            const spedDefnsPromise = serviceBroker
                .loadViewData(CORE_API.PhysicalSpecDefinitionStore.findBySelector, [vm.selector])
                .then(r => r.data);

            const specsPromise = serviceBroker
                .loadViewData(CORE_API.PhysicalSpecificationStore.findBySelector, [vm.selector])
                .then(r => r.data);

            $q.all([fieldsPromise, spedDefnsPromise, specsPromise])
                .then(([fields, defns, specs]) => {
                vm.physicalFields = fields;
                vm.physicalSpecDefns = defns;
                vm.physicalSpecs = specs;

                const defnsById = _.keyBy(defns, 'id');
                const specsById = _.keyBy(specs, 'id');

                const fieldsWithSpecs = _.map(fields, f => {
                    const specDefn = defnsById[f.specDefinitionId];
                    const specification = specsById[specDefn.specificationId];
                    return Object.assign({}, {field: f}, { specDefn }, { specification } );
                });
                vm.data = fieldsWithSpecs;
            });
        }
    };


    vm.onGridInitialise = (api) => {
        vm.gridApi = api;
    };


    vm.exportGrid = () => {
        vm.gridApi.exportFn('physical-fields.csv');
    };
}


controller.$inject = [
    '$q',
    'ServiceBroker'
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzRelatedPhysicalSpecDefnFieldSection'
};
