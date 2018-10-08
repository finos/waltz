import template from "./roadmaps-panel.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";


const bindings = {
    parentEntityRef: "<"
};


const modes = {
    LOADING: "LOADING",
    LIST: "LIST",
    ADD_ROADMAP: "ADD_ROADMAP",
    ADD_SCENARIO: "ADD_SCENARIO",
    CONFIGURE_SCENARIO: "CONFIGURE_SCENARIO",
    VIEW_SCENARIO: "VIEW_SCENARIO"
};


const initialState = {
    modes,
    roadmaps: [],
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
        vm.visibility.mode = modes.ADD_SCENARIO;
        const usedNames = _
            .chain(vm.scenarios)
            .filter(s => s.roadmapId = roadmap.id)
            .map(s => s.name.toLowerCase())
            .value();

        const defaultName = "New Scenario for " + roadmap.name;
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
                        [ roadmap.id, newName ])
                    .then(r => {
                        notification.success("New scenario created");
                        vm.onConfigureScenario(r.data);
                    });
            }
        }
    };

    vm.onAddRoadmap = () => {
        vm.visibility.mode = modes.ADD_ROADMAP;
    };

    vm.onSelectScenario = (scenario) => {
        vm.visibility.mode = modes.VIEW_SCENARIO;
        vm.selectedScenario = scenario;
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

    vm.onSaveRoadmapName = (ctx, data) => {
        return updateField(
                ctx.id,
                CORE_API.RoadmapStore.updateName,
                data,
                true,
                "Roadmap name updated")
            .then(() => reloadAllData());
    };

    vm.onSaveRoadmapDescription = (ctx, data) => {
        return updateField(
                ctx.id,
                CORE_API.RoadmapStore.updateDescription,
                data,
                false,
                "Roadmap description updated")
            .then(() => reloadAllData());
    };

    vm.onSaveScenarioName = (ctx, data) => {
        return updateField(
                ctx.id,
                CORE_API.ScenarioStore.updateName,
                data,
                true,
                "Scenario name updated");
    };

    vm.onSaveScenarioEffectiveDate = (ctx, data) => {
        return updateField(
                ctx.id,
                CORE_API.ScenarioStore.updateEffectiveDate,
                data,
                true,
                "Scenario target date updated");
    };

    vm.onSaveScenarioStatus = (data, ctx) => {
        return updateField(
                ctx.id,
                CORE_API.ScenarioStore.updateScenarioStatus,
                { newVal: data },   // as this is coming from enum field, we need to fake out change object
                true,
                "Scenario status updated");
    };

    vm.onSaveScenarioDescription = (ctx, data) => {
        return updateField(
                ctx.id,
                CORE_API.ScenarioStore.updateDescription,
                data,
                false,
                "Scenario description updated");
    };

    vm.onConfigureScenario = (scenario) => {
        vm.visibility.mode = modes.CONFIGURE_SCENARIO;
        vm.selectedScenario = scenario;
    };

    vm.onCancel = () => {
        vm.visibility.mode = modes.LIST;
        vm.selectedScenario = null;
        reloadAllData();
    };

    vm.onAddAxisItem = (axisItem) => {
        const args = [
            vm.selectedScenario.id,
            axisItem.orientation,
            axisItem.domainItem,
            axisItem.position
        ];
        return serviceBroker
            .execute(
                CORE_API.ScenarioStore.addAxisItem,
                args);

    };

    vm.onRemoveAxisItem = (axisItem) => {
        const args = [
            vm.selectedScenario.id,
            axisItem.orientation,
            axisItem.domainItem
        ];
        return serviceBroker
            .execute(
                CORE_API.ScenarioStore.removeAxisItem,
                args);
    };

    vm.onRepositionAxisItems = (scenarioId, orientation, ids) => {
        const args = [
            scenarioId,
            orientation,
            ids
        ];

        return serviceBroker
            .execute(
                CORE_API.ScenarioStore.reorderAxis,
                args);
    };


    // -- helpers --

    function updateField(roadmapId,
                         method,
                         data,
                         preventNull = true,
                         message = "Updated") {
        if (preventNull && (_.isEmpty(data.newVal) && !_.isDate(data.newVal))) {
            return Promise.reject("Waltz:updateField - Cannot set an empty value");
        }
        if (data.newVal !== data.oldVal) {
            return serviceBroker
                .execute(
                    method,
                    [ roadmapId, data.newVal ])
                .then(() => notification.success(message));
        } else {
            return Promise.reject("Nothing updated")
        }
    }


    function reloadAllData() {
        const roadmapSelectorOptions = mkSelectionOptions(vm.parentEntityRef);

        const roadmapPromise = serviceBroker
            .loadViewData(
                CORE_API.RoadmapStore.findRoadmapsBySelector,
                [ roadmapSelectorOptions ],
                { force: true })
            .then(r => vm.roadmaps = r.data);

        const scenarioPromise = serviceBroker
            .loadViewData(
                CORE_API.ScenarioStore.findByRoadmapSelector,
                [ roadmapSelectorOptions ],
                { force: true })
            .then(r => vm.scenarios = r.data);

        return $q
            .all([roadmapPromise, scenarioPromise]);
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
    id: "waltzRoadmapsPanel",
    component
};