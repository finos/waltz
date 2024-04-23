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

import _ from "lodash";
import template from "./logical-flow-type-editor.html";
import {toEntityRefWithKind} from "../../../common/entity-utils";
import toasts from "../../../svelte-stores/toast-store";
import {CORE_API} from "../../../common/services/core-api-utils";
import {loadUsageData} from "../../../data-types/data-type-utils";
import FlowDataTypeEditor from "./svelte/FlowDataTypeEditor.svelte";


const bindings = {
    flow: "<",
    onDelete: "<",
    onReload: "<",
    onCancel: "<",
    onSelect: "<?"
};


const initialState = {
    flow: null,
    isDirty: false,
    onDelete: (x) => console.log("lfte: default onDelete()", x),
    onReload: (x) => console.log("lfte: default onReload()", x),
    onCancel: (x) => console.log("lfte: default onCancel()", x),
    onSelect: (x) => console.log("lfte: default onSelect()", x),
    FlowDataTypeEditor
};


function controller() {

    const vm = _.defaultsDeep(this, initialState);

    const refresh = () => {
        vm.logicalFlowEntityRef = toEntityRefWithKind(vm.flow, "LOGICAL_DATA_FLOW");
    };

    vm.$onInit = () => {
        refresh();
    };

    vm.$onChanges = () => {
        refresh();
    };

    vm.delete = () => vm.onDelete(vm.flow);
    vm.cancel = () => vm.onCancel();
}


controller.$inject = [];

const component = {
    bindings,
    controller,
    template
};


const id = "waltzLogicalFlowTypeEditor";


export default {
    component,
    id
};
