import _ from 'lodash';
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
    form: {
        name: "",
        description: "",
        externalId: "",
        format: ""
    },
    validation: {
        canSubmit: false,
        message: null
    }

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

    vm.doAddNew = () => {
        vm.onChange(vm.form);
    };

    vm.cancel = () => vm.onDismiss();

    vm.showAddNewForm = () => {
        vm.selected = null;
        vm.visibility.showAddButton = false;
        vm.validateForm();
    };

    vm.cancelAddNew = () => {
        vm.visibility.showAddButton = true;
    };

    vm.validateForm = () => {
        const proposedName = _.trim(vm.form.name);
        const existingNames = _.map(vm.candidates, 'name');

        const nameDefined = _.size(proposedName) > 0;
        const nameUnique = ! _.includes(existingNames, proposedName);
        const formatDefined = _.size(vm.form.format) > 0;

        const message = (nameDefined ? "" : "Name cannot be empty. ")
            +
            (nameUnique ? "" : "Name must be unique. ")
            +
            (formatDefined ? "" : "Format must be supplied");

        vm.validation = {
            canSubmit: nameDefined && nameUnique && formatDefined,
            message
        };
    }

}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default component;