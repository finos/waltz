import template from "./roadmaps-panel.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import _ from "lodash";


const bindings = {
    parentEntityRef: "<"
};


const initialState = {
    roadmaps: [],
    scenariosByRoadmapId: {}
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const roadmapSelectorOptions = mkSelectionOptions(vm.parentEntityRef);
        serviceBroker
            .loadViewData(
                CORE_API.RoadmapStore.findRoadmapsBySelector,
                [ roadmapSelectorOptions ])
            .then(r => vm.roadmaps = r.data);


        serviceBroker
            .loadViewData(
                CORE_API.RoadmapStore.findScenariosByRoadmapSelector,
                [ roadmapSelectorOptions ])
            .then(r => vm.scenariosByRoadmapId = _.keyBy(r.data, "roadmapId"));
    };
}


controller.$inject = [ "ServiceBroker" ];


const component = {
    bindings,
    template,
    controller
};


export default {
    id: "waltzRoadmapsPanel",
    component
};