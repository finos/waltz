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

import {initialiseData, invokeFunction} from "../../../common";
import _ from "lodash";
import template from "./entity-selector.html";


const bindings = {
    clearable: "<",
    currentSelection: "<",
    entityKinds: "<",
    entityLifecycleStatuses: "<?",
    itemId: "<?", // ctx
    limit: "<?",
    onSelect: "<",
    required: "<?",
    selectionFilter: "<?",
    placeholder: "@?"
};


const initialState = {
    entities: [],
    limit: 20,
    entityKinds: [],
    required: false,
    selectionFilter: (x) => true,
    entityLifecycleStatuses: ["ACTIVE", "PENDING", "REMOVED"],
    placeholder: "Search..."
};


function controller(entitySearchStore) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        vm.options = {
            entityKinds: vm.entityKinds,
            limit: vm.limit,
            entityLifecycleStatuses: vm.entityLifecycleStatuses
        };

        if (changes.entityKinds) {
            vm.entities = [];
        }

        if (vm.currentSelection) {
            vm.refresh(vm.currentSelection.name, vm.options);
        }
    };

    vm.refresh = function(query) {
        if (!query) return;
        return entitySearchStore.search(_.assign({}, vm.options, {"searchQuery": query}))
            .then((entities) => {
                vm.entities = vm.selectionFilter
                    ? _.filter(entities, vm.selectionFilter)
                    : entities;
            });
    };

    vm.select = (item) => invokeFunction(vm.onSelect, item, vm.itemId);

    vm.mkTracker = (item) => item.kind + "_" + item.id;
}


controller.$inject = ["EntitySearchStore"];


const component = {
    bindings,
    template,
    controller,
};


export default component;


