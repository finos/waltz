import template from "./roadmap-view.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";


const bindings = {
};


const initialState = {
    foo: "baa"
};


function controller($stateParams, serviceBroker) {
    const vm = initialiseData(this, initialState);


    vm.$onInit = () => {
        vm.roadmapId = $stateParams.id;
        vm.parentEntityRef = {
            kind: "SCENARIO",
            id: vm.roadmapId
        };

    };
}


controller.$inject = [
    "$stateParams",
    "ServiceBroker"
];


const component = {
    controller,
    template,
    bindings,
    controllerAs: "$ctrl"
};


export default {
    id: "waltzScenarioView",
    component
};



