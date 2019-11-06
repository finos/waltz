import template from "./physical-flow-and-specification-detail.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {toEntityRef} from "../../../common/entity-utils";


const bindings = {
    physicalFlow: "<",
    specification: "<"
};


const initialState = {
    tags: []
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        if (vm.physicalFlow) {
            serviceBroker
                .loadViewData(
                    CORE_API.TagStore.findTagsByEntityRef,
                    [toEntityRef(vm.physicalFlow)],
                    { force: true })
                .then(r => vm.tags = r.data);
        }
    }
}

controller.$inject = [
    "ServiceBroker"
];

const component = {
    bindings,
    controller,
    template
};


export default {
    id: "waltzPhysicalFlowAndSpecificationDetail",
    component
};