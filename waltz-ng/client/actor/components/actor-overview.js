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

import {initialiseData} from "../../common/index";
import template from "./actor-overview.html";
import Markdown from "../../common/svelte/Markdown.svelte";


const bindings = {
    actor: "<"
};


const initialState = {
    Markdown
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        if (vm.actor) {
            vm.parentEntityRef = {
                kind: "ACTOR",
                id: vm.actor.id
            };
        }
    };
}


controller.$inject = [];


export default {
    component: {
        template,
        bindings,
        controller
    },
    id: "waltzActorOverview"
};