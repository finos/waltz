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
import {activeSections} from "../../section-store";
import _ from "lodash";

import template from "./dynamic-sections-view.html";
import {dynamicSectionsByKind} from "../../dynamic-section-definitions";


const bindings = {
    filters: "<",
    parentEntityRef: "<",
};


const initialState = {
    filters: {},
};


function controller($scope) {
    const vm = initialiseData(this, initialState);

    const unsub = activeSections.subscribe(d => $scope.$applyAsync(() => {
        vm.availableSections = _.get(dynamicSectionsByKind, d.pageKind, []);
        vm.sections = d.sections;
    }));

    vm.$onDestroy = () => unsub();

    vm.onRemove = (section) => activeSections.remove(section);
}


controller.$inject = [
    "$scope"
];

const component = {
    controller,
    bindings,
    template
};


const id = "waltzDynamicSectionsView";


export default {
    id,
    component
};
