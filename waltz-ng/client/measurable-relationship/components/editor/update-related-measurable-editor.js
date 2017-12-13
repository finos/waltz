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
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";

import template from "./update-related-measurable-editor.html";


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
                })
                .catch(e => {
                    notification.error("Could not save because: "+e.message);
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