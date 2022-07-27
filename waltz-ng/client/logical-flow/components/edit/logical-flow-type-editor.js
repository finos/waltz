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
import {toEntityRef} from "../../../common/entity-utils";
import toasts from "../../../svelte-stores/toast-store";


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
    onSelect: (x) => console.log("lfte: default onSelect()", x)
};


function controller() {

    const vm = _.defaultsDeep(this, initialState);

    const refresh = () => {
        vm.logicalFlowEntityRef = toEntityRef(vm.flow, "LOGICAL_DATA_FLOW");
    };

    vm.$onInit = () => {
        refresh();
    };

    vm.$onChanges = () => {
        refresh();
    };

    vm.onDirty = (dirtyFlag) => {
        vm.isDirty = dirtyFlag;
    };

    vm.registerSaveFn = (saveFn) => {
        vm.save = saveFn;
    };

    vm.delete = () => vm.onDelete(vm.flow);
    vm.cancel = () => vm.onCancel();

    vm.onSave = () => {
        if (vm.save) {
            vm.save()
                .then(() => {
                    toasts.success("Data types updated successfully");
                    vm.cancel(); //clear edit session
                    vm.onReload();
                });
        } else {
            console.log("onSave - no impl");
        }
    };
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
