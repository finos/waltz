import {CORE_API} from '../../../common/services/core-api-utils';
import {initialiseData} from '../../../common';

import template from './inline-physical-flow-panel.html';


const bindings = {
    parentEntityRef: '<'
};


const initialState = {
    physicalFlows: [],
    logicalFlows: [],
    specifications: []
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {

        serviceBroker
            .loadViewData(
                CORE_API.PhysicalSpecificationStore.findByEntityReference,
                [vm.parentEntityRef])
            .then(r => vm.physicalSpecifications = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.findByEntityReference,
                [vm.parentEntityRef])
            .then(r => vm.logicalFlows = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.PhysicalFlowStore.findByEntityReference,
                [vm.parentEntityRef])
            .then(r => vm.physicalFlows = r.data);

    };

    vm.onPhysicalFlowsInitialise = (e) => {
        vm.physicalFlowProducesExportFn = e.exportProducesFn;
        vm.physicalFlowConsumesExportFn = e.exportConsumesFn;
        vm.physicalFlowUnusedSpecificationsExportFn = e.exportUnusedSpecificationsFn;
    };

    vm.exportPhysicalFlowProduces = () => {
        vm.physicalFlowProducesExportFn();
    };

    vm.exportPhysicalFlowConsumes = () => {
        vm.physicalFlowConsumesExportFn();
    };

    vm.exportPhysicalFlowUnusedSpecifications = () => {
        vm.physicalFlowUnusedSpecificationsExportFn();
    };
}


controller.$inject = [
    'ServiceBroker'
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzInlinePhysicalFlowPanel'
};
