import template from "./roadmap-references.html";
import {initialiseData} from "../../../common";
import roles from "../../../user/roles";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";


const bindings = {
    parentEntityRef: "<"
};


const modes = {
    LOADING: "LOADING",
    VIEW: "VIEW",
    ADD: "ADD"
};


const initialState = {
    permissions: {
        admin: false,
        edit: false
    },
    mode: modes.LOADING,
    visibility: {
        subSection: false,
        controls: false
    }
};


function determineLoadMethod(kind) {
    switch(kind) {
        case "APPLICATION":
            return CORE_API.RoadmapStore.findRoadmapsAndScenariosByRatedEntity;
        default:
            return CORE_API.RoadmapStore.findRoadmapsAndScenariosByFormalRelationship;
    }
}


function controller(notification, serviceBroker, userService) {

    const vm = initialiseData(this, initialState);

    function reloadData() {
        const loadMethod = determineLoadMethod(vm.parentEntityRef.kind);

        return serviceBroker
            .loadViewData(
                loadMethod,
                [ vm.parentEntityRef ],
                { force: true })
            .then(r => {
                vm.references = r.data;
                vm.mode = modes.VIEW;
                vm.visibility.subSection = vm.visibility.subSection || !_.isEmpty(vm.references);
            });
    }

    function loadPermissions() {
        userService
            .whoami()
            .then(u => {
                vm.permissions = {
                    admin: userService.hasRole(u, roles.SCENARIO_ADMIN),
                    edit: userService.hasRole(u, roles.SCENARIO_EDITOR)
                };
                vm.visibility.subSection = vm.permissions.admin || !_.isEmpty(vm.references);
                vm.visibility.controls = vm.permissions.admin && vm.parentEntityRef.kind !== "APPLICATION";
            });
    }

    vm.$onInit = () => {
        reloadData();
        loadPermissions();
    };


    // -- interact --

    vm.onShowAddRoadmap = () => {
        if (! vm.permissions.admin) return;
        vm.mode = modes.ADD;
    };

    vm.onAddRoadmap = (command) => {
        const commandWithLinkedEntity = Object.assign(
            {},
            command,
            { linkedEntity: vm.parentEntityRef });

        return serviceBroker
            .execute(
                CORE_API.RoadmapStore.addRoadmap,
                [ commandWithLinkedEntity ])
            .then(() => {
                notification.success(`Roadmap "${command.name}" created`);
                vm.mode = modes.LOADING;
                reloadData();
            })
    };

    vm.onCancel = () => {
        vm.mode = modes.VIEW;
    };
}


controller.$inject = [
    "Notification",
    "ServiceBroker",
    "UserService"
];


const component = {
    controller,
    template,
    bindings
};


export default {
    id: "waltzRoadmapReferences",
    component
};