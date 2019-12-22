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
import template from "./svg-diagrams.html";

function getKey(group) {
    return `lastViewedSvg.${group}`;
}

function setLastViewed($state, index, diagram, localStorageService) {
    const lastViewed = {
        group: diagram.group,
        index: index
    };
    localStorageService.set(getKey($state.current.name), lastViewed);
}

function controller($state, $timeout, localStorageService) {
    const vm = this;

    vm.$onInit = () => {
        const activeTab = localStorageService.get(getKey($state.current.name));
        vm.active = activeTab ? activeTab.index : 0;
    };

    vm.show = (index, diagram) => {
        // timeout needed to prevent IE from crashing
        $timeout(() => diagram.visible = true, 100);
        setLastViewed($state, index, diagram, localStorageService)
    };

    vm.hide = (diagram) => {
        diagram.visible = false;
    };
}


controller.$inject = [
    "$state",
    "$timeout",
    "localStorageService"
];


export default {
    template,
    bindings: {
        diagrams: "<",
        blockProcessor: "<"
    },
    controller
};
