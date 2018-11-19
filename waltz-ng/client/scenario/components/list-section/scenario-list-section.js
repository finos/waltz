import _ from "lodash";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {releaseLifecycleStatus} from "../../../common/services/enums/release-lifecycle-status";
import {confirmWithUser} from "../../../common/dialog-utils";

import template from "./scenario-list-section.html";

const bindings = {
    roadmapId: "<"
};


const modes = {
    LOADING: "LOADING",
    LIST: "LIST",
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

    vm.onAddScenario = () => {
        return ensureActiveRoadmap(notification, vm.roadmap)
            .then(() => {
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
                            .then(() => {
                                notification.success("New scenario created");
                                reloadAllData();
                            });
                    }
                }
            });
    };

    vm.onCloneScenario = (scenario) => {
        return ensureActiveRoadmap(notification, vm.roadmap)
            .then(() => {
                const newName = prompt(
                    "Please enter a new name for the scenario",
                    `Clone of ${scenario.name}`);

                if (newName) {
                    return serviceBroker
                        .execute(
                            CORE_API.ScenarioStore.cloneById,
                            [ scenario.id, newName ])
                        .then(() => reloadAllData())
                        .then(() => notification.success("Scenario cloned"));
                }
            });
    };

    vm.onPublishScenario = (scenario) => {
        return ensureActiveRoadmap(notification, vm.roadmap)
            .then(() => confirmWithUser(
                    `PUBLISH: Please confirm that you want scenario: "${scenario.name}" to be published`,
                    () => updateReleaseStatus(
                        scenario.id,
                        releaseLifecycleStatus.ACTIVE,
                        "Scenario published",
                        "Failed to publish scenario")));
    };

    vm.onRevertToDraftScenario = (scenario) => {
        return ensureActiveRoadmap(notification, vm.roadmap)
            .then(() => confirmWithUser(
                    `REVERT: Please confirm that you want to revert scenario: "${scenario.name}" back to draft`,
                    () => updateReleaseStatus(
                        scenario.id,
                        releaseLifecycleStatus.DRAFT,
                        "Scenario reverted to draft",
                        "Failed to revert scenario to draft")));

    };

    vm.onRetireScenario = (scenario) => {
        return ensureActiveRoadmap(notification, vm.roadmap)
            .then(() => confirmWithUser(
                    `RETIRE: Please confirm that you want scenario: "${scenario.name}" to be retired`,
                    () => updateReleaseStatus(
                        scenario.id,
                        releaseLifecycleStatus.DEPRECATED,
                        "Scenario retired",
                        "Failed to retire scenario")));

    };

    vm.onRepublishScenario = (scenario) => {
        return ensureActiveRoadmap(notification, vm.roadmap)
            .then(() => confirmWithUser(
                    `REPUBLISH: Please confirm that you want scenario: "${scenario.name}" to be republished`,
                    () => updateReleaseStatus(
                        scenario.id,
                        releaseLifecycleStatus.ACTIVE,
                        "Scenario republished",
                        "Failed to republish scenario")));

    };

    vm.onDeleteScenario = (scenario) => {
        return ensureActiveRoadmap(notification, vm.roadmap)
            .then(() => confirmWithUser(
                    `DELETE: Please confirm that you want scenario: "${scenario.name}" to be deleted, it cannot be recovered`,
                    () => serviceBroker
                        .execute(
                            CORE_API.ScenarioStore.removeScenario,
                            [ scenario.id ])
                        .then(() => reloadAllData())
                        .then(() => notification.success("Scenario deleted"))
                        .catch((e) => {
                            console.log("WSLS: Failed to delete scenario", { error: e });
                            return notification.warning(`Failed to delete scenario: ${e.message}`);
                        })));

    };

    vm.onCancel = () => {
        vm.visibility.mode = modes.LIST;
        reloadAllData();
    };


    // -- helpers --

    function ensureActiveRoadmap(notification, roadmap) {
        if(roadmap.entityLifecycleStatus === "REMOVED") {
            const msg = "Cannot make changes to a roadmap that is deleted";
            notification.error(msg);
            return Promise.reject(msg);
        } else {
            return Promise.resolve();
        }
    }


    function updateReleaseStatus(scenarioId, status, successMessage = "Success", failureMessage = "Failed") {
        serviceBroker
            .execute(
                CORE_API.ScenarioStore.updateReleaseStatus,
                [ scenarioId, status.key ])
            .then(() => reloadAllData())
            .then(() => notification.success(successMessage))
            .catch((e) => {
                console.log(`WSLS: ${failureMessage}`, { error: e });
                return notification.warning(`${failureMessage}: ${e.message}`);
            });
    }

    function reloadAllData() {
        const roadmapPromise = serviceBroker
            .loadViewData(CORE_API.RoadmapStore.getRoadmapById, [ vm.roadmapId ])
            .then(r => vm.roadmap = r.data);

        const scenarioPromise = serviceBroker
            .loadViewData(
                CORE_API.ScenarioStore.findForRoadmap,
                [ vm.roadmapId ],
                { force: true })
            .then(r => vm.scenarios = r.data);

        return $q
            .all([scenarioPromise, roadmapPromise]);
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