

import template from "./roadmap-scenario-config.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";


const bindings = {
    scenarioId: "<",
    onCancel: "<",
    onSaveScenarioName: "<",
    onSaveScenarioDescription: "<"
};


const initialState = {
    scenario: null
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    function reload() {
        return serviceBroker
            .loadViewData(
                CORE_API.ScenarioStore.getById,
                [ vm.scenarioId ],
                { force: true })
            .then(r => Object.assign(vm, r.data));
    }


    vm.$onInit = () => {
        reload();
    };


    vm.onSaveName = (ctx, data) => {
        vm.onSaveScenarioName(ctx, data)
            .then(() => reload());
    };


    vm.onSaveDescription = (ctx, data) => {
        vm.onSaveScenarioDescription(ctx, data)
            .then(() => reload());
    };
}


controller.$inject = [ "ServiceBroker" ];


const component = {
    controller,
    template,
    bindings
};


export default {
    component,
    id: "waltzRoadmapScenarioConfig"
}