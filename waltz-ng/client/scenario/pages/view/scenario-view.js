import template from "./scenario-view.html";
import {initialiseData} from "../../../common";


const bindings = {
};


const initialState = {
};


function controller($stateParams, dynamicSectionManager) {
    const vm = initialiseData(this, initialState);


    vm.$onInit = () => {
        console.log("onInit");
        vm.scenarioId = $stateParams.id;
        vm.parentEntityRef = {
            kind: "SCENARIO",
            id: vm.scenarioId
        };

        vm.availableSections = dynamicSectionManager.findAvailableSectionsForKind("SCENARIO");
        vm.sections = dynamicSectionManager.findUserSectionsForKind("SCENARIO");

    };


    // -- DYNAMIC SECTIONS

    vm.addSection = s => vm.sections = dynamicSectionManager.openSection(s, "SCENARIO");
    vm.removeSection = (section) => vm.sections = dynamicSectionManager.removeSection(section, "SCENARIO");
}


controller.$inject = [
    "$stateParams",
    "DynamicSectionManager"
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



