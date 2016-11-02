import {initialiseData} from '../../../common';

const bindings = {
    candidates: '<',
    current: '<',
    selected: '<',
    onDismiss: '<',
    onChange: '<'
};


const template = require('./physical-flow-edit-specification.html');


const initialState = {
    visibility: {
        showAddButton: true
    },
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.select = (spec) => {
        vm.cancelAddNew();
        vm.selected = spec;
    };

    vm.change = () => {
        if (vm.selected == null) vm.cancel();
        if (vm.current && vm.selected.id === vm.current.id) vm.cancel();
        vm.onChange(vm.selected);
    };

    vm.cancel = () => vm.onDismiss();

    vm.showAddNewForm = () => {
        vm.selected = null;
        vm.visibility.showAddButton = false;
    };

    vm.cancelAddNew = () => {
        vm.visibility.showAddButton = true;
    };

}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default component;