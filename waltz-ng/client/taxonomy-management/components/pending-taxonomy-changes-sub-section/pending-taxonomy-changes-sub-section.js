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

import template from "./pending-taxonomy-changes-sub-section.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {determineColorOfSubmitButton} from "../../../common/severity-utils";
import {displayError, mkErrorMessage} from "../../../common/error-utils";


const bindings = {
    pendingChanges: "<",
    onApplyChange: "<",
    onDiscardChange: "<",
    onDismiss: "<"
};


const modes = {
    LIST: "LIST",
    VIEW: "VIEW"
};


const initialState = {
    selectedPendingChange: null,
    preview: null,
    mode: modes.LIST
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    function reload() {
        console.log("reload ?!")
    }

    vm.$onChanges = (c) => {
    };

    vm.onSelectPendingChange = (pendingChange) => {
        vm.selectedPendingChange = pendingChange;
        vm.mode = modes.VIEW;
        serviceBroker
            .execute(
                CORE_API.TaxonomyManagementStore.previewById,
                [ pendingChange.id ],
                { force: true })
            .then(r => {
                vm.preview = r.data;
                vm.submitButtonClass = determineColorOfSubmitButton(_.map(vm.preview.impacts, "severity"));
            })
            .catch(e => {
                displayError("Could not preview command", e);
                vm.preview = {
                    command:{},
                    impacts: [],
                    errorMessage: mkErrorMessage("Could not preview command", e)
                };
            });
    };

    vm.dismiss = () => {
        vm.mode = modes.LIST;
        vm.preview = null;
        vm.selectedPendingChange = null;

        if (vm.onDismiss) {
            vm.onDismiss();
        }
    };

    vm.onDiscardPendingChange = (c) => {
        vm.onDiscardChange(c)
            .then(vm.dismiss);
    };

    vm.onApplyPendingChange = (c) => {
        vm.onApplyChange(c)
            .then(vm.dismiss)
            .then(reload);
    };

}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    bindings,
    template,
    controller
};


export default {
    component,
    id: "waltzPendingTaxonomyChangesSubSection"
}

