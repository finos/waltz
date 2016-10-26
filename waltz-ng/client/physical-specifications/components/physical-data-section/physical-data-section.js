import _ from "lodash";
import {combineFlowData, enrichConsumes} from "../../utilities";
import {termSearch} from "../../../common";


const bindings = {
    physicalFlows: '<',
    specifications: '<'
};


const template = require('./physical-data-section.html');


function mkData(specifications = { produces: [], consumes: [] },
                physicalFlows = [])
{

    const ownedData = combineFlowData(
        specifications.produces,
        physicalFlows);

    const produces = _.filter(ownedData, p => p.physicalFlow != null);

    const consumes = enrichConsumes(
        specifications.consumes,
        physicalFlows);

    const unusedSpecifications = _.chain(ownedData)
        .filter(p => !p.physicalFlow)
        .map('specification')
        .value();

    return { produces, consumes, unusedSpecifications };
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