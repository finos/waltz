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


import template from "./playpen1.html";
import {initialiseData} from "../../common";
import InvolvementPicker from "./InvolvementPicker.svelte";
import EntityPicker from "./EntityPicker.svelte";
import EntitySelector from "./EntitySelector.svelte";
import TestPage from "./TestPage.svelte";

const initData = {
    // parentEntityRef: {id: 20768, kind: "APPLICATION"}
    parentEntityRef: {id: 39855, kind: "SURVEY_INSTANCE"},
    // parentEntityRef: {id: 76823, kind: "SURVEY_INSTANCE"},
    measurableEntityRef: {id: 54566, kind: "MEASURABLE"},
    InvolvementPicker,
    EntityPicker,
    EntitySelector,
    TestPage
};


function controller($q,
                    serviceBroker,
                    userService) {

    const vm = initialiseData(this, initData);

    vm.$onInit = () => {


    }

}

controller.$inject = ["$q", "ServiceBroker", "UserService"];

const view = {
    template,
    controller,
    controllerAs: "$ctrl",
    bindToController: true,
    scope: {}
};

export default view;