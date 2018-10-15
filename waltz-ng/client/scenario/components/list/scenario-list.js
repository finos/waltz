import template from "./scenario-list.html";
import {initialiseData} from "../../../common";
import roles from "../../../user/roles";
import {releaseLifecycleStatus} from "../../../common/services/enums/release-lifecycle-status";


const bindings = {
    scenarios: "<",
    onAddScenario: "<",
    onCloneScenario: "<",
    onSelectScenario: "<",
    onConfigureScenario: "<",
    onPublishScenario: "<",
    onRetireScenario: "<",
    onRevertToDraftScenario: "<",
    onRepublishScenario: "<",
    onDeleteScenario: "<"
};


const initialState = {
    permissions: {
        admin: false,
        edit: false
    },
    actions: [],
    scenarios: []
};


function controller(userService) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        userService
            .whoami()
            .then(u => vm.permissions = {
                admin: userService.hasRole(u, roles.SCENARIO_ADMIN),
                edit: userService.hasRole(u, roles.SCENARIO_EDITOR)
            })
            .then(() => {
                vm.actions = [
                    publishAction,
                    revertToDraftAction,
                    republishAction,
                    cloneAction,
                    retireAction,
                    deleteAction
                ];
            });
    };

    const cloneAction = {
        type: "action",
        name: "Clone",
        predicate: () => vm.permissions.admin,
        icon: "clone",
        description: "Makes a copy of this scenario",
        execute: (scenario) => vm.onCloneScenario(scenario)
    };

    const publishAction = {
        type: "action",
        predicate: (scenario) => vm.permissions.admin &&  scenario.releaseStatus === releaseLifecycleStatus.DRAFT.key,
        name: "Publish",
        icon: "arrow-up",
        description: "Makes this scenario viewable by all users",
        execute: (scenario) => vm.onPublishScenario(scenario)
    };

    const retireAction = {
        type: "action",
        predicate: (scenario) => vm.permissions.admin &&  scenario.releaseStatus === releaseLifecycleStatus.ACTIVE.key,
        name: "Retire",
        icon: "arrow-down",
        description: "Marks this scenario as retired",
        execute: (scenario) => vm.onRetireScenario(scenario)
    };

    const revertToDraftAction = {
        type: "action",
        predicate: (scenario) => vm.permissions.admin && scenario.releaseStatus === releaseLifecycleStatus.ACTIVE.key,
        name: "Draft",
        icon: "arrow-left",
        description: "Marks this scenario as in draft",
        execute: (scenario) => vm.onRevertToDraftScenario(scenario)
    };

    const republishAction = {
        type: "action",
        predicate: (scenario) => vm.permissions.admin &&  scenario.releaseStatus === releaseLifecycleStatus.DEPRECATED.key,
        name: "Republish",
        icon: "arrow-left",
        description: "Marks this scenario as published",
        execute: (scenario) => vm.onRepublishScenario(scenario)
    };

    const deleteAction = {
        type: "action",
        predicate: (scenario) => vm.permissions.admin &&  scenario.releaseStatus === releaseLifecycleStatus.DRAFT.key,
        name: "Delete",
        icon: "trash",
        description: "Delete this scenario (cannot be recovered)",
        execute: (scenario) => vm.onDeleteScenario(scenario)
    };

}


controller.$inject = [
    "UserService"
];


const component = {
    bindings,
    template,
    controller
};


export default {
    id: "waltzScenarioList",
    component
};