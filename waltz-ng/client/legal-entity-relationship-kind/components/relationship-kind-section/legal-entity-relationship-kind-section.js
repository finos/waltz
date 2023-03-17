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
import template from "./legal-entity-relationship-kind-section.html";
import {initialiseData} from "../../../common";
import {activeMode, inputString, Modes} from "../bulk-upload/bulk-upload-relationships-store";
import {mkSelectionOptions} from "../../../common/selector-utils";

const bindings = {
    parentEntityRef: "<"
};


const initialState = {
    relationshipKind: null,
    relationships: [],
    visibility: {
        overlay: false,
        bulkUpload: false
    },
}


function controller($q, $scope) {

    const vm = initialiseData(this, initialState);


    vm.$onChanges = () => {
        vm.selectionOptions = mkSelectionOptions(vm.parentEntityRef);
    }

    vm.bulkUpload = () => {
        vm.visibility.bulkUpload = true;
    }

    vm.cancelBulkUpload = () => {
        inputString.set(null);
        activeMode.set(Modes.INPUT);
        vm.visibility.bulkUpload = false;
    }

    vm.doneUpload = () => {
        $scope.$applyAsync(() => {
            vm.cancelBulkUpload();
        })
    }

}

controller.$inject = [
    "$q",
    "$scope",
];


const component = {
    template,
    bindings,
    controller
};


export default {
    id: "waltzLegalEntityRelationshipKindSection",
    component
};