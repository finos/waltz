

import template from "./roadmap-scenario-config.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";


const bindings = {
    scenarioId: "<",
    onCancel: "<",
    onSaveScenarioName: "<",
    onSaveScenarioDescription: "<",
    onSaveScenarioEffectiveDate: "<",
    onSaveScenarioType: "<",
    onAddAxisItem: "<",
    onRemoveAxisItem: "<",
    onRepositionAxisItems: "<"
};


const initialState = {
    scenario: null
};


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    function reload() {
        return serviceBroker
            .loadViewData(
                CORE_API.ScenarioStore.getById,
                [ vm.scenarioId ],
                { force: true })
            .then(r => Object.assign(vm, r.data));
    }


    // -- boot ---

    vm.$onInit = () => {
        reload();
    };


    // -- interact ---

    vm.onSaveName = (data, ctx) => {
        vm.onSaveScenarioName(data, ctx)
            .then(() => reload());
    };

    vm.onSaveDescription = (data, ctx) => {
        vm.onSaveScenarioDescription(data, ctx)
            .then(() => reload());
    };

    vm.onSaveEffectiveDate = (data, ctx) => {
        vm.onSaveScenarioEffectiveDate(data, ctx)
            .then(() => reload());
    };

}


controller.$inject = [
    "$q",
    "ServiceBroker"
];


const component = {
    controller,
    template,
    bindings
};


export default {
    component,
    id: "waltzRoadmapScenarioConfig"
}