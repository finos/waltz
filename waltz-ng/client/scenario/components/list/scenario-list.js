import template from "./scenario-list.html";
import {initialiseData} from "../../../common";
import _ from "lodash";


const bindings = {
    scenarios: "<",
    onAddScenario: "<",
    onCloneScenario: "<",
    onSelectScenario: "<",
    onConfigureScenario: "<"
};


const initialState = {
    permissions: {
        admin: false,
        edit: false
    },
    scenarios: []
};


function controller(userService) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        userService
            .whoami()
            .then(u => vm.permissions = {
                admin: userService.hasRole(u, "SCENARIO_ADMIN"),
                edit: userService.hasRole(u, "SCENARIO_EDIT")
            });
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