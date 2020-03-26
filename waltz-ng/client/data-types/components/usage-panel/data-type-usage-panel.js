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
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";

import template from "./data-type-usage-panel.html";
import roles from "../../../user/system-roles";


const bindings = {
    parentEntityRef: "<",
    helpText: "@"
};


const initialState = {
    helpText: null,
    isDirty: false,
    visibility: {
        editor: false,
        controls: false
    }
};


function controller(notification, serviceBroker, userService) {
    const vm = initialiseData(this, initialState);

    const reload = (force = false) => {
        const promise = serviceBroker
            .loadViewData(
                CORE_API.DataTypeDecoratorStore.findByEntityReference,
                [ vm.parentEntityRef ],
                { force })
            .then(r => r.data)
            .then(decorators => _.map(decorators, d => ({
                lastUpdatedAt: d.lastUpdatedAt,
                lastUpdatedBy: d.lastUpdatedBy,
                provenance: d.provenance,
                dataTypeId: d.decoratorEntity.id,
                dataFlowId: d.dataFlowId
            })));

        return promise
            .then(r => {
                vm.used = r;
            });
    };

    vm.$onInit = () => {
        userService
            .whoami()
            .then(u => vm.visibility.controls = userService.hasRole(u, roles.LOGICAL_DATA_FLOW_EDITOR));
    };

    vm.$onChanges = () => {
        if (! vm.parentEntityRef) return;
        reload();
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
        if (vm.save) {
            vm.save()
                .then(() => {
                    notification.success("Data types updated successfully");
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
    "Notification",
    "ServiceBroker",
    "UserService"
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