import template from "./roadmap-view.html";
import {initialiseData} from "../../../common";
import {dynamicSections} from "../../../dynamic-section/dynamic-section-definitions";


const bindings = {
};


const initialState = {
    changeLogSection: dynamicSections.changeLogSection,
};


function controller($stateParams, serviceBroker) {
    const vm = initialiseData(this, initialState);


    vm.$onInit = () => {
        vm.roadmapId = $stateParams.id;
        vm.parentEntityRef = {
            kind: "ROADMAP",
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



