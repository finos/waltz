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

import DataTypeDecoratorSection from "./DataTypeDecoratorSection.svelte";

const bindings = {
    parentEntityRef: "<",
};


const initialState = {
    DataTypeDecoratorSection
};


function controller() {
    const vm = initialiseData(this, initialState);
}


controller.$inject= [];


const component = {
    controller,
    bindings,
    template: `<waltz-svelte-component component="$ctrl.DataTypeDecoratorSection" primary-entity-ref="$ctrl.parentEntityRef"></waltz-svelte-component>`,
    controllerAs: "$ctrl",
    bindToController: true,
};


export default {
    component,
    id: "waltzDataTypeDecoratorSection"
};


