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

import {initialiseData} from "../../../common/index";
import {sectionToTemplate} from "../../dynamic-section-utils";

import template from "./dynamic-section-wrapper.html";


const bindings = {
    parentEntityRef: "<",
    filters: "<",
    renderMode: "@?",  // chromeless | standard (default)
    section: "<",
    onRemove: "<",
};


const initialState = {
    renderMode: "standard",
    sectionScope: null
};


function controller($element, $compile, $scope) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        vm.sectionScope = $scope.$new();
        vm.sectionScope.parentEntityRef = vm.parentEntityRef;
        vm.sectionScope.filters = vm.filters;
        vm.sectionScope.section = vm.section;
        vm.sectionScope.onRemove = vm.onRemove;
        vm.sectionScope.canRemove = vm.canRemove;

        const linkFn = $compile(sectionToTemplate(vm.section, vm.renderMode));
        const content = linkFn(vm.sectionScope);
        $element.append(content);
    };
}


controller.$inject=[
    "$element",
    "$compile",
    "$scope"
];


const component = {
    controller,
    template,
    bindings
};


export default {
    component,
    id: "waltzDynamicSectionWrapper"
}