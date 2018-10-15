import template from "./roadmap-references.html";
import {initialiseData} from "../../../common";
import roles from "../../../user/roles";
import {CORE_API} from "../../../common/services/core-api-utils";


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
        subSection: false
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


function controller(serviceBroker, userService) {

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {

        const loadMethod = determineLoadMethod(vm.parentEntityRef.kind);

        serviceBroker
            .loadViewData(
                loadMethod,
                [ vm.parentEntityRef ])
            .then(r => {
                vm.references = r.data;
                vm.mode = modes.VIEW;
                vm.visibility.subSection = vm.visibility.subSection || vm.references.length > 0;
            });

        userService
            .whoami()
            .then(u => {
                vm.permissions = {
                    admin: userService.hasRole(u, roles.SCENARIO_ADMIN),
                    edit: userService.hasRole(u, roles.SCENARIO_EDITOR)
                };
                vm.visibility.subSection = vm.permissions.admin || vm.references.length > 0;
            });
    };

    vm.onShowAddRoadmap = () => {
        vm.mode = modes.ADD;
    };

    vm.onAddRoadmap = (r) => {
        console.log("add roadmap", {r})
    };

    vm.onCancel = () => {
        vm.mode = modes.VIEW;
    };


}


controller.$inject = [
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