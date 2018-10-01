import template from "./roadmaps-list.html";
import {initialiseData} from "../../../common";
import _ from "lodash";


const bindings = {
    roadmaps: "<",
    scenarios: "<",
    onAddRoadmap: "<",
    onAddScenario: "<",
    onCloneScenario: "<",
    onSelectScenario: "<",
    onSaveRoadmapName: "<",
    onSaveRoadmapDescription: "<",
    onConfigureScenario: "<"
};


const initialState = {
    roadmaps: [],
    scenarios: []
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        vm.scenariosByRoadmapId = _.groupBy(vm.scenarios, "roadmapId");
    };

}


controller.$inject = [ ];


const component = {
    bindings,
    template,
    controller
};


export default {
    id: "waltzRoadmapsList",
    component
};