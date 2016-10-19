import _ from 'lodash';
import {combineFlowData} from '../../utilities';
import {termSearch} from "../../../common"


const bindings = {
    endpointReferences: '<',  // [ entityReference... ]
    physicalFlows: '<',
    specifications: '<'
};


const template = require('./physical-data-section.html');


function enrichConsumes(specifications = [],
                physicalFlows = [],
                endpointReferences = [])
{
    const visitedRefs = [];

    return _.chain(specifications)
        .map(specification => {
            const physicalFlow = _.find(physicalFlows, { specificationId: specification.id });
            const sourceRef = _.find(endpointReferences, { id: specification.owningEntity.id, kind: specification.owningEntity.kind });
            const targetRef = _.find(endpointReferences, { id: physicalFlow.target.id, kind: physicalFlow.target.kind });

            if (!physicalFlow || !sourceRef || !targetRef) {
                return null;
            } else {
                const firstSource = !_.includes(
                    visitedRefs,
                    sourceRef);

                if(firstSource === true) {
                    visitedRefs.push(sourceRef);
                }

                return {
                    specification,
                    physicalFlow,
                    firstSource,
                    sourceRef,
                    targetRef
                };
            }
        })
        .filter(r => r != null)
        .value();
}


function mkData(specifications = { produces: [], consumes: [] },
                physicalFlows = [],
                endpointReferences = [])
{
    const produces = combineFlowData(
        specifications.produces,
        physicalFlows,
        endpointReferences);
    const consumes = enrichConsumes(
        specifications.consumes,
        physicalFlows,
        endpointReferences);
    return { produces, consumes };
}


function controller() {

    const vm = this;

    const produceFields = [
        'specification.name',
        'specification.externalId',
        'specification.format',
        'specification.description',
        'physicalFlow.transport',
        'physicalFlow.frequency',
        'targetRef.name'
    ];


    const consumeFields = [
        'specification.name',
        'specification.externalId',
        'specification.format',
        'specification.description',
        'physicalFlow.transport',
        'physicalFlow.frequency',
        'sourceRef.name'
    ];

    vm.$onChanges = (changes) => {
        Object.assign(vm, mkData(vm.specifications, vm.physicalFlows, vm.endpointReferences));
        vm.filterProduces("");
        vm.filterConsumes("");
    };

    vm.filterProduces = query => {
        vm.filteredProduces = termSearch(vm.produces, query, produceFields)
    };

    vm.filterConsumes = query => {
        vm.filteredConsumes = termSearch(vm.consumes, query, consumeFields)
    };
}

controller.$inject = ['$scope'];


const component = {
    template,
    bindings,
    controller
};


export default component;