import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";

import template from "./physical-spec-data-type-list.html";


const bindings = {
    parentEntityRef: '<'
};


const initialState = {
    specDataTypes: []
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        const selectorOptions = {
            entityReference: vm.parentEntityRef,
            scope: 'EXACT'
        };

        serviceBroker
            .loadViewData(CORE_API.PhysicalSpecDataTypeStore.findBySpecificationSelector, [selectorOptions])
            .then(result => vm.specDataTypes = result.data);
    };
}


controller.$inject = [
    'ServiceBroker'
];


export default {
    component: {
        template,
        bindings,
        controller
    },
    id: 'waltzPhysicalSpecDataTypeList'
};

