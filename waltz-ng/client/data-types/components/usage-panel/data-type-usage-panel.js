/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import _ from "lodash";
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";

import template from "./data-type-usage-panel.html";
import roles from "../../../user/roles";


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
        const promise = vm.parentEntityRef.kind === "PHYSICAL_SPECIFICATION"
            ? serviceBroker
                .loadViewData(
                    CORE_API.PhysicalSpecDataTypeStore.findBySpecificationId,
                    [ vm.parentEntityRef.id ],
                    { force })
                .then(r => r.data)
            : serviceBroker
                .loadViewData(
                    CORE_API.LogicalFlowDecoratorStore.findByFlowIdsAndKind,
                    [ [vm.parentEntityRef.id] ],
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