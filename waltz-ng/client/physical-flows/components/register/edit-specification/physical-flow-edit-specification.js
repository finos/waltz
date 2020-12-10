/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
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