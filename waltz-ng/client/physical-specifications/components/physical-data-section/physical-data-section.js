import _ from 'lodash';
import {combineFlowData} from '../../utilities';
import {termSearch} from "../../../common"


const bindings = {
    logicalFlows: '<',
    physicalFlows: '<',
    specifications: '<'
};


const template = require('./physical-data-section.html');


function enrichConsumes(specifications = [],
                physicalFlows = [],
                logicalFlows = [])
{
    const visitedApps = [];

    return _.chain(specifications)
        .map(specification => {
            const physicalFlow = _.find(physicalFlows, { specificationId: specification.id });
            const logicalFlow = _.find(logicalFlows, f => f.id === physicalFlow.flowId);
            const firstSource = !_.includes(visitedApps, specification.owningApplicationId);
            if(firstSource === true) {
                visitedApps.push(specification.owningApplicationId);
            }

            return {
                specification,
                logicalFlow,
                physicalFlow,
                firstSource
            };
        })
        .groupBy("logicalFlow.source.id")
        .value();
}


function mkData(specifications = { produces: [], consumes: [] },
                physicalFlows = [],
                logicalFlows = [])
{
    const produces = combineFlowData(
        specifications.produces,
        physicalFlows,
        logicalFlows);
    const consumes = enrichConsumes(
        specifications.consumes,
        physicalFlows,
        logicalFlows);
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
        'logicalFlow.target.name'
    ];


    const consumeFields = [
        'specification.name',
        'specification.externalId',
        'specification.format',
        'specification.description',
        'physicalFlow.transport',
        'physicalFlow.frequency',
        'logicalFlow.source.name'
    ];

    vm.$onChanges = (changes) => {
        Object.assign(vm, mkData(vm.specifications, vm.physicalFlows, vm.logicalFlows));
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