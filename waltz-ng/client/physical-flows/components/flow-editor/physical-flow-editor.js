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
import template from "./physical-flow-editor.html";
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import {toEntityRef} from "../../../common/entity-utils";
import {displayError} from "../../../common/error-utils";


const bindings = {
    parentEntityRef: "<"
};


const initialState = {
    visibility: {
        tagEditor: false
    }
};


function controller(notification, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const load = () => {
        serviceBroker
            .loadViewData(
                CORE_API.PhysicalFlowStore.getById,
                [vm.parentEntityRef.id],
                {force: true})
            .then(r => vm.physicalFlow = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.EntityTagStore.findTagsByEntityRef,
                [vm.parentEntityRef],
                {force: true})
            .then(r => vm.tags = r.data);
    };

    vm.$onInit = () => load();

    const mkCommand = (name, value) => {
        return {
            entityReference: vm.parentEntityRef,
            name,
            value
        };
    };

    // -- TAGS ---
    const saveTags = (tags = [], successMessage) => {
        return serviceBroker
            .execute(
                CORE_API.EntityTagStore.update,
                [vm.parentEntityRef, tags])
            .then(() => {
                notification.success(successMessage);
                return load();
            })
            .catch(e => displayError(notification, "Could not update tags", e));
    };

    const doSave = (name, value) => {
        const cmd = mkCommand(name, value);
        return serviceBroker
            .execute(
                CORE_API.PhysicalFlowStore.updateAttribute,
                [ vm.physicalFlow.id, cmd ])
            .then(r => {
                notification.success(`Updated ${name}`);
                return load();
            })
            .catch(e => displayError(notification, `Could not update ${name} value`, e))
    };


    vm.onSaveCriticality = (value, ctx) => doSave("criticality", value);
    vm.onSaveFrequency = (value, ctx) => doSave("frequency", value);
    vm.onSaveTransport = (value, ctx) => doSave("transport", value);
    vm.onSaveBasisOffset = (value, ctx) => doSave("basisOffset", value.newVal);
    vm.onSaveDescription = (value, ctx) => doSave("description", value.newVal);
    vm.onSaveTags = (tags, successMessage) => saveTags(tags, successMessage);
    vm.onShowTagEditor = () => vm.visibility.tagEditor = true;
    vm.onDismissTagEditor = () => vm.visibility.tagEditor = false;

}



controller.$inject = [
    "Notification",
    "ServiceBroker"
];


const component = {
    template,
    controller,
    bindings
};


export default {
    component,
    id: "waltzPhysicalFlowEditor"
};