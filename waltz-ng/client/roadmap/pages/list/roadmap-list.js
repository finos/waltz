import template from "./roadmap-list.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";


const bindings = {
};


const initialState = {

};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        serviceBroker
            .loadViewData(CORE_API.RoadmapStore.findAllRoadmapsAndScenarios)
            .then(r => vm.list = r.data);
    };

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
    id: "waltzRoadmapList",
    component
};

