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
import template from "./logical-flow-type-editor.html";
import {toEntityRef} from "../../../common/entity-utils";


const bindings = {
    flow: "<",
    onDelete: "<",
    onCancel: "<"
};


const initialState = {
    flow: null,
    isDirty:false,
    onDelete: (x) => console.log("lfte: default onDelete()", x),
    onCancel: (x) => console.log("lfte: default onCancel()", x)
};


function controller(notification) {

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
                    notification.success("Data types updated successfully");
                    vm.cancel(); //clear edit session
                });
        } else {
            console.log("onSave - no impl");
        }
    };
}


controller.$inject = [
    "Notification"
];

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
