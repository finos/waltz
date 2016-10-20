import _ from 'lodash';
import {combineFlowData} from '../../../physical-specifications/utilities';


const bindings = {
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


function controller() {
    const vm = this;

    vm.$onChanges = () => {
        const specOwners = _.map(vm.specifications, "owningEntity");
        const flowTargets = _.map(vm.physicalFlows, "target");

        const endpointReferences = _.chain(_.concat(specOwners, flowTargets))
            .uniqBy("id")
            .value();

        const flowData = combineFlowData(vm.specifications.produces, vm.physicalFlows, endpointReferences);
        vm.candidates = removeCandidatesWithNoPhysicalFlows(flowData);
    };
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};

export default component;