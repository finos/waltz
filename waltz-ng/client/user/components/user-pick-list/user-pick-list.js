import {initialiseData} from "../../../common";
import template from "./user-pick-list.html";


const bindings = {
    people: "<",
    canRemoveLast: "<?",
    onRemove: "<",
    onAdd: "<",
    requiredRole: "@?"
};


const initialState = {
    requiredRole: null,
    canRemoveLast: false,
    addingPerson: false,
    onRemove: p => console.log("wupl: on-remove-person", p),
    onAdd: p => console.log("wupl: on-add-person", p)
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
    };

    vm.$onChanges = (changes) => {

    };

    vm.$onDestroy = () => {
    };

    // -- INTERACT --


    vm.onStartAddPerson = () => {
        vm.addingPerson = true;
    };

    vm.onCancelAddPerson = () => {
        vm.addingPerson = false;
    };

    vm.onSelectPerson = (p) => vm.selectedPerson = p;

    vm.onAddPerson = () => {
        if (vm.selectedPerson) {
            vm.onAdd(vm.selectedPerson);
            vm.onCancelAddPerson();
        }
    }

}

controller.$inject = [];


const component = {
    template,
    controller,
    bindings
};


export default {
    component,
    id: "waltzUserPickList"
};