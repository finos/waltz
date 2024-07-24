
import {initialiseData} from "../../common";
import RoleView from "../svelte/RoleView.svelte";
import {entity} from "../../common/services/enums/entity";

const bindings = {
};


const initialState = {
    RoleView
};


function controller($stateParams) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        vm.roleId = $stateParams.id;
        vm.primaryEntityRef = {id: $stateParams.id, kind: entity.ROLE.key}
    };

}


controller.$inject = [
    "$stateParams"
];


const component = {
    bindings,
    controller,
    template: `<waltz-svelte-component component='$ctrl.RoleView' role-id='$ctrl.roleId'></waltz-svelte-component>
               <br>
               <waltz-dynamic-sections-view parent-entity-ref="$ctrl.primaryEntityRef"></waltz-dynamic-sections-view>`
};


export default {
    id: "waltzRoleView",
    component
};



