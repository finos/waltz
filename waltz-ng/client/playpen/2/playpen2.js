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

import template from "./playpen2.html";
import {mkSelectionOptions} from "../../common/selector-utils";
import {CORE_API} from "../../common/services/core-api-utils";
import {mkRef} from "../../common/entity-utils";
import Markdown from "../../common/svelte/Markdown.svelte"


const initialState = {
    checkedItemIds: [],
    parentEntityRef: {id: 20506, kind: "APPLICATION"},
    Markdown
}

function controller(serviceBroker) {

    const vm = Object.assign(this, initialState);

    vm.text = `
# Hello

hello | world
--- | ---
a | b
    `

    serviceBroker
        .loadViewData(CORE_API.MeasurableStore.findMeasurablesBySelector,
                      [mkSelectionOptions(
                          mkRef("MEASURABLE_CATEGORY", 16),
                          "EXACT")])
        .then(r => vm.recordsManagementItems = console.log(r.data) || r.data);

    vm.isDisabled = (d) => !d.concrete;

    vm.onItemCheck = (d) => {


        vm.checkedItemIds = _.union(vm.checkedItemIds, [d]);
    }

    vm.onItemUncheck = (d) => {
        vm.checkedItemIds = _.without(vm.checkedItemIds, d);

    }

}




controller.$inject = ["ServiceBroker"];


const view = {
    template,
    controller,
    controllerAs: "$ctrl",
    bindToController: true,
    scope: {}
};


export default view;
