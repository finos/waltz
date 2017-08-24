import {CORE_API} from '../../../common/services/core-api-utils';
import {initialiseData} from '../../../common';

import template from './inline-logical-flow-panel.html';


const bindings = {
    parentEntityRef: '<'
};


const initialState = {
    logicalFlows: [],
    logicalFlowDecorators: [],
    physicalFlows: [],
    physicalSpecifications: []
};


function controller(serviceBroker) {

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const selector = {
            entityReference: vm.parentEntityRef,
            scope: 'EXACT'
        };

        serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.findByEntityReference,
                [vm.parentEntityRef])
            .then(r => vm.logicalFlows = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowDecoratorStore.findBySelectorAndKind,
                [selector, 'DATA_TYPE'])
            .then(r => vm.logicalFlowDecorators = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.PhysicalFlowStore.findByEntityReference,
                [vm.parentEntityRef])
            .then(r => vm.physicalFlows = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.PhysicalSpecificationStore.findByEntityReference,
                [vm.parentEntityRef])
            .then(r => vm.physicalSpecifications = r.data);
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
    id: 'waltzInlineLogicalFlowPanel'
};
