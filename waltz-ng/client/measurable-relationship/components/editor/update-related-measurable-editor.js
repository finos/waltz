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
import {CORE_API} from "../../../common/services/core-api-utils";

import template from "./update-related-measurable-editor.html";
import {displayError} from "../../../common/error-utils";


const bindings = {
    relationship: '<',
    onCancel: '<',
    onRefresh: '<'
};


const initialState = {
    form: {
        description: null
    },
    onCancel: () => console.log('wurme: onCancel - default impl'),
    onRefresh: () => console.log('wurme: onRefresh - default impl')
};


function controller(notification, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        vm.form.description = vm.relationship.description;
        vm.form.relationshipKind = vm.relationship.relationship;
    };

    vm.$onChange = (c) => {
    };


    // -- INTERACT --

    vm.isFormValid = () => true;

    vm.submit = () => {
        if (vm.isFormValid()) {
            const form = vm.form;
            const key = {
                a: vm.relationship.a,
                b: vm.relationship.b,
                relationshipKind: vm.relationship.relationship
            };
            const changes = {
                relationshipKind: vm.relationship.relationship,
                description: form.description
            };
            return save(key, changes)
                .then(() => {
                    notification.success("Relationship saved");
                    vm.onRefresh();
                    vm.onCancel();
                })
                .catch(e => {
                    displayError(notification, "Could not save because: ", e);
                });
        }
    };


    // -- API ---

    const save = (key, change) =>
        serviceBroker.execute(
            CORE_API.MeasurableRelationshipStore.update,
            [key, change]);
}


controller.$inject = [
    'Notification',
    'ServiceBroker'
];


const component = {
    bindings,
    template,
    controller
};


const id = "waltzUpdateRelatedMeasurableEditor";


export default {
    id,
    component
};