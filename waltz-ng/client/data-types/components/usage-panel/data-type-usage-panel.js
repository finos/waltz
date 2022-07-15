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

import template from "./data-type-usage-panel.html";
import roles from "../../../user/system-roles";
import {loadUsageData} from "../../data-type-utils";
import toasts from "../../../svelte-stores/toast-store";
import _ from "lodash";
import {CORE_API} from "../../../common/services/core-api-utils";


const bindings = {
    parentEntityRef: "<",
    parentFlow: "<?",
    helpText: "@"
};


const initialState = {
    helpText: null,
    isDirty: false,
    parentFlow: null,
    visibility: {
        editor: false,
        controls: false
    }
};


function controller(serviceBroker, userService, $q) {
    const vm = initialiseData(this, initialState);

    const reload = (force = false) => {
        loadUsageData($q, serviceBroker, vm.parentEntityRef, force)
            .then(usageData => vm.used = usageData);
    };

    vm.$onInit = () => {
        serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.findPermissionsForFlow,
                [vm.parentFlow.id])
            .then(r => {
                vm.visibility.controls = _.some(r.data, d => _.includes(["ADD", "UPDATE", "REMOVE"], d));
            });
    };

    vm.$onChanges = () => {
        if (! vm.parentEntityRef) return;
        reload(true);
    };

    vm.onShowEdit = () => {
        vm.visibility.editor = true;
    };

    vm.onHideEdit = () => {
        vm.visibility.editor = false;
    };

    vm.onSave = () => {
        if(!vm.isDirty)
            return;
        if(vm.parentEntityRef.kind === "PHYSICAL_SPECIFICATION" && !confirm("This will affect all associated physical flows. Do you want to continue?")){
            return;
        }
        if (vm.save) {
            vm.save()
                .then(() => {
                    toasts.success("Data types updated successfully");
                    reload(true);
                    vm.onHideEdit();
                });
        } else {
            console.log("onSave - no impl");
        }
    };

    vm.onDirty = (dirtyFlag) => {
        vm.isDirty = dirtyFlag;
    };

    vm.registerSaveFn = (saveFn) => {
        vm.save = saveFn;
    };
}


controller.$inject = [
    "ServiceBroker",
    "UserService",
    "$q"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    id: "waltzDataTypeUsagePanel",
    component
}