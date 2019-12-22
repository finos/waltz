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

import template from "./dynamic-section.html";
import {initialiseData} from "../../../common/index";
import {kindToViewState} from "../../../common/link-utils";


const bindings = {
    parentEntityRef: "<",
    section: "<",
    onRemove: "<",
};

const initialState = {
    embedded: false,
    backLink: {
        state: "",
        params: {}
    }
};


function controller($state) {
    const vm = initialiseData(this, initialState);
    vm.embedded = _.startsWith($state.current.name, "embed");

    vm.$onChanges = () => {
        if (vm.parentEntityRef !== null) {
            vm.backLink = {
                state: kindToViewState(vm.parentEntityRef.kind),
                params: { id: vm.parentEntityRef.id },
            };
        }
    };

}


controller.$inject = [
    "$state"
];


const component = {
    controller,
    template,
    bindings,
    transclude: true
};

const id = "waltzDynamicSection";


export default {
    id,
    component
};