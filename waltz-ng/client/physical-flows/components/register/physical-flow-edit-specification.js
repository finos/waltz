/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";
import {initialiseData, toEntityRef} from "../../../common";
import {toOptions, dataFormatKindNames} from "../../../common/services/display_names";


const bindings = {
    candidates: '<',
    current: '<',
    onDismiss: '<',
    onChange: '<',
    owningEntity: '<',
    selected: '<',
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
    },
    formatOptions: toOptions(dataFormatKindNames, true)
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
        vm.onChange(Object.assign(
            {},
            vm.form,
            { owningEntity: toEntityRef(vm.owningEntity) }));
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