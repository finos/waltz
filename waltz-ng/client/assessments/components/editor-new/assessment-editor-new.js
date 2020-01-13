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
import template from "./assessment-editor-new.html";


const bindings = {
    onSave: "<",
    assessment: "<",
    onClose: "<",
    onRemove: "<"
};


const initialState = {
    canEdit: false
};


function controller(notification, userService) {
    const vm = initialiseData(this, initialState);

    vm.onSaveRating = (ratingId) => {
        const comments = vm.assessment.rating ? vm.assessment.rating.description : "";
        return vm.onSave(ratingId, comments, vm.assessment);
    };

    vm.onSaveDescription = (commentChange) => {
        const rating = vm.assessment.rating;
        if (rating){
            return vm.onSave(rating.ratingId, commentChange.newVal, vm.assessment);
        } else {
            notification.warning("Please create a rating before you add any description");
        }
    };

    vm.onInitialise = () => {
        const definition = vm.assessment.definition;
        const readOnly = definition.isReadOnly;

        userService
            .whoami()
            .then(u => {
                const permittedRole = definition.permittedRole;
                vm.canEdit = _.isEmpty(permittedRole)
                    ? !readOnly
                    : !readOnly && userService.hasRole(u, permittedRole);
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
    id: "waltzAssessmentEditorNew"
};
