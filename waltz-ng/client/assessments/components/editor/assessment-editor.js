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
import template from "./assessment-editor.html";


const bindings = {
    onSave: "<",
    assessment: "<",
    onClose: "<",
    onRemove: "<"
};


const modes = {
    EDIT: "EDIT",
    VIEW: "VIEW"
};


const initialState = {
    mode: modes.VIEW,
    isEditable: false,
    readOnlyReason: null,
    working: null
};


function controller(notification, userService) {
    const vm = initialiseData(this, initialState);


    vm.doSave = () => {
        if (vm.working.ratingId == null) {
            alert("Cannot save without a valid rating selection");
            return;
        }
        return vm
            .onSave(
                vm.assessment.definition.id,
                vm.working.ratingId,
                vm.working.comment)
            .then(() => vm.onClose());
    };


    vm.onEdit = () => {
        vm.mode = modes.EDIT;
    };


    vm.onCancelEdit = () => {
        vm.mode = modes.VIEW;
    };


    vm.$onInit = () => {
        const definition = vm.assessment.definition;
        const readOnly = definition.isReadOnly;

        vm.working = _.pick(
            vm.assessment.rating,
            ["comment", "ratingId"]);

        userService
            .whoami()
            .then(u => {
                const permittedRole = definition.permittedRole;
                if (readOnly) {
                    // assessment is readonly therefore cannot be edited
                    vm.isEditable = false;
                    vm.readOnlyReason = "This assessment is marked as read only";
                } else if (! _.isEmpty(permittedRole)) {
                    // permitted role is not empty therefore need to check user roles
                    if (userService.hasRole(u, permittedRole)) {
                        vm.isEditable = true;
                        vm.readOnlyReason = null;
                    } else {
                        vm.isEditable = false;
                        vm.readOnlyReason = "You do not have permission to modify this assessment";
                    }
                } else {
                    // permitted roles is empty therefore anyone can edit
                    vm.isEditable = true;
                    vm.readOnlyReason = null;
                }
            });
    }

}


controller.$inject = [
    "Notification",
    "UserService"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzAssessmentEditor"
};
