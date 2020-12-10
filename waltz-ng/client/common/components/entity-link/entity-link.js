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
import {kindToViewState} from "../../../common/link-utils";
import template from "./entity-link.html";


const bindings = {
    entityRef: "<",
    iconPlacement: "@?",
    tooltipPlacement: "@?",
    additionalDisplayData: "<?",
    target: "@", /** if '_blank' the external icon is shown **/
};


const initialState = {
    additionalDisplayData: [],
    iconPlacement: "left", // can be left, right, none
    tooltipPlacement: "top" // left, top-left, top-right; refer to: (https://github.com/angular-ui/bootstrap/tree/master/src/tooltip)
};


function controller($state) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        if (vm.entityRef) {
            const viewState = kindToViewState(vm.entityRef.kind);
            if (viewState) {
                // url needs to be re-computed when entityRef changes
                // eg: when used in a ui-grid cell template
                vm.viewUrl = $state.href(viewState, { id: vm.entityRef.id });
            }
        }
    };
}

controller.$inject = [
    "$state"
];


const component = {
    bindings,
    template,
    controller
};


export default component;