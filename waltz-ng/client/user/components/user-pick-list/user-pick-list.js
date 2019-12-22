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

import {initialiseData} from "../../../common";
import template from "./user-pick-list.html";
import _ from "lodash";


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

    vm.personFilter = (p) => {
        return ! _.some(vm.people, existing => existing.id === p.id);
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