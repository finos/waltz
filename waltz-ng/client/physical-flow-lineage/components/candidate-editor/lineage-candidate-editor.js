import _ from 'lodash';
import {enrichConsumes} from '../../../physical-specifications/utilities';


const bindings = {
    currentLineage: '<',
    logicalFlows: '<',
    physicalFlows: '<',
    specifications: '<',
    onRefocus: '<',
    onAdd: '<',
    onRemove: '<',
    contributingFlowIds: '<'
};


const template = require('./lineage-candidate-editor.html');


function removeCandidatesWithNoPhysicalFlows(candidates = []) {
    return _.filter(candidates, c => c.physicalFlow != null);
}


function removeUsedCandidates(candidates = [], currentLineage = []) {
    const usedFlowIds = _.map(currentLineage, 'flow.id');

    return _.reject(candidates, c => _.includes(usedFlowIds, c.physicalFlow.id));
}


function mkCandidates(flowData = [], currentLineage = []) {
    const candidates = removeCandidatesWithNoPhysicalFlows(flowData);
    return removeUsedCandidates(candidates, currentLineage);
}


function controller() {
    const vm = this;

    vm.$onChanges = () => {
        const specOwners = _.map(vm.specifications, "owningEntity");
        const flowTargets = _.map(vm.physicalFlows, "target");

        const endpointReferences = _.chain(_.concat(specOwners, flowTargets))
            .uniqBy("id")
            .value();

        const flowData = enrichConsumes(
            vm.specifications.consumes,
            vm.physicalFlows,
            endpointReferences);

        vm.candidates = mkCandidates(flowData, vm.currentLineage);
    };
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};

export default component;