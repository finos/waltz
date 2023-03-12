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

import {initialiseData, invokeFunction, termSearch} from "../../common";
import template from './actor-selector.html';


const bindings = {
    allActors: "<",
    onSelect: "<"
};


const initialState = {
    allActors: [],
    actors: []
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.refresh = function (query) {
        if (!query) {
            return;
        }
        vm.actors = termSearch(
            vm.allActors,
            query,
            ["name", "externalId"]);
    };

    vm.select = (item) => {
        invokeFunction(
            vm.onSelect,
            Object.assign({}, item, {kind: "ACTOR"}));
    };
}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;


