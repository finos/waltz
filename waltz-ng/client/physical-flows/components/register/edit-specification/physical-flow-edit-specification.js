/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";
import {initialiseData} from "../../../../common";
import {toEntityRef} from '../../../../common/entity-utils';
import {dataFormatKind} from "../../../../common/services/enums/data-format-kind";
import {toOptions} from "../../../../common/services/enums";
import template from './physical-flow-edit-specification.html';


const bindings = {
    candidates: '<',
    current: '<',
    onDismiss: '<',
    onChange: '<',
    owningEntity: '<',
    selected: '<',
};


const SEARCH_CUTOFF = 6;


const initialState = {
    visibility: {
        showAddButton: true,
        search: false
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
    formatOptions: toOptions(dataFormatKind, true)
};


function controller() {

    const vm = initialiseData(this, initialState);

    vm.select = (spec) => {
        vm.cancelAddNew();
        vm.selected = spec;
        if (vm.current && vm.selected.id === vm.current.id) vm.cancel();
        vm.onChange(vm.selected);
    };

    vm.doAddNew = () => {
        vm.onChange(Object.assign(
            {},
            vm.form,
            { owningEntity: toEntityRef(vm.owningEntity), lastUpdatedBy: "ignored, server will set" }));
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
    };

    vm.$onInit = () => {
        vm.visibility.search = vm.candidates.length > SEARCH_CUTOFF;
    };
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzPhysicalFlowEditSpecification'
};