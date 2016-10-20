import {combineFlowData, enrichConsumes} from '../../utilities';
import {termSearch} from "../../../common"


const bindings = {
    physicalFlows: '<',
    specifications: '<'
};


const template = require('./physical-data-section.html');


function mkData(specifications = { produces: [], consumes: [] },
                physicalFlows = [])
{
    const produces = combineFlowData(
        specifications.produces,
        physicalFlows);
    const consumes = enrichConsumes(
        specifications.consumes,
        physicalFlows);
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
        Object.assign(vm, mkData(vm.specifications, vm.physicalFlows));
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