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
import template from "./physical-flow-editor.html";
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
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
                CORE_API.TagStore.findTagsByEntityRef,
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
                CORE_API.TagStore.update,
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