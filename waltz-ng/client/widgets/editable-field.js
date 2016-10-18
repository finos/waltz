import {initialiseData} from '../common';

const template = require('./editable-field.html');


const bindings = {
    initialVal: '<',
    onSave: '<',
    fieldType: '@',
    itemId: '<'
};


const initialState = {
    errorMessage: "",
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
        vm.errorMessage = "";
    };

    const saveFailed = (e) => {
        vm.saving = false;
        vm.editing = true;
        vm.errorMessage = e;
    };


    vm.save = () => {
        const data = {
            newVal: vm.newVal,
            oldVal: vm.initialVal
        };

        vm.saving = true;

        const promise = vm.onSave(vm.itemId, data);

        if (promise) {
            promise.then(saveComplete, saveFailed)
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
        vm.errorMessage = "";
    };

}


controller.$inject = ['$timeout'];


const component = {
    template,
    bindings,
    controller
};


export default component;