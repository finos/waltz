import {initialiseData} from '../common';

const template = require('./editable-field.html');


const bindings = {
    initialVal: '<',
    onSave: '<',
    fieldType: '@',
    itemId: '<'
};


const initialState = {
    editing: false,
    saving: false,
    fieldType: 'text',
    onSave: () => console.log("WETF: No on-save method provided")
};


function controller($timeout) {
    const vm = initialiseData(this, initialState);


    const saveComplete = () => {
        vm.saving = false;
        vm.editing = false;
    };


    vm.save = () => {
        const data = {
            newVal: vm.newVal,
            oldVal: vm.initialVal
        };

        vm.saving = true;

        const promise = vm.onSave(vm.itemId, data);

        if (promise) {
            promise.then(saveComplete)
        } else {
            saveComplete();
        }
    };


    vm.edit = () => {
        vm.editing = true;
        vm.newVal = vm.initialVal;
    };


    vm.cancel = () => {
        vm.editing = false;
        vm.saving = false;
    };

}


controller.$inject = ['$timeout'];


const component = {
    template,
    bindings,
    controller
};


export default component;