import template from "./scenario-list-section.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";


const bindings = {
    roadmapId: "<"
};


const modes = {
    LOADING: "LOADING",
    LIST: "LIST",
    ADD_SCENARIO: "ADD_SCENARIO",
    CONFIGURE_SCENARIO: "CONFIGURE_SCENARIO",
};


const initialState = {
    modes,
    scenarios: [],
    selectedScenario: null,
    visibility: {
        mode: modes.LOADING
    }
};


function controller($q,
                    serviceBroker,
                    notification)
{
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        vm.visibility.mode = modes.LOADING;
        reloadAllData()
            .then(() => vm.visibility.mode = modes.LIST);
    };


    // -- INTERACT --

    vm.onAddScenario = (roadmap) => {
        const usedNames = _
            .chain(vm.scenarios)
            .filter(s => s.roadmapId = vm.roadmap.id)
            .map(s => s.name.toLowerCase())
            .value();

        const defaultName = "New Scenario for " + vm.roadmap.name;
        const newName = prompt("Enter a name for the new scenario", defaultName);
        if (! newName) {
            return Promise.reject("Create cancelled by user");
        } else {
            const isNameAlreadyTaken = _.includes(usedNames, newName.toLowerCase());
            if (isNameAlreadyTaken) {
                const msg = "Cannot create a scenario with the same name as an existing scenario";
                notification.error(msg);
                return Promise.reject(msg);
            } else {
                return serviceBroker
                    .execute(
                        CORE_API.RoadmapStore.addScenario,
                        [ vm.roadmap.id, newName ])
                    .then(r => {
                        notification.success("New scenario created");
                        reloadAllData();
                    });
            }
        }
    };

    vm.onCloneScenario = (scenario) => {
        const newName = prompt(
            "Please enter a new name for the scenario",
            `Clone of ${scenario.name}`);

        if (newName) {
            serviceBroker
                .execute(
                    CORE_API.ScenarioStore.cloneById,
                    [ scenario.id, newName ])
                .then(() => reloadAllData())
                .then(() => notification.success("Scenario cloned"));
        } else {
            notification.warning("Aborting clone")
        }
    };

    vm.onCancel = () => {
        vm.visibility.mode = modes.LIST;
        reloadAllData();
    };

    // -- helpers --

    function reloadAllData() {
        const roadmapPromise = serviceBroker
            .loadViewData(CORE_API.RoadmapStore.getRoadmapById, [ vm.roadmapId ])
            .then(r => vm.roadmap = r.data);

        const scenarioPromise = serviceBroker
            .loadViewData(
                CORE_API.ScenarioStore.findForRoadmap,
                [ vm.roadmapId ],
                { force: true })
            .then(r => vm.scenarios = r.data)
            .then(() => console.log('sc', vm.scenarios))

        return $q
            .all([scenarioPromise]);
    }

}


controller.$inject = [
    "$q",
    "ServiceBroker",
    "Notification"
];


const component = {
    bindings,
    template,
    controller
};


export default {
    id: "waltzScenarioListSection",
    component
};