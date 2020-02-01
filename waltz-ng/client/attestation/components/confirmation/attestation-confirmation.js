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

import {initialiseData, invokeFunction} from "../../../common/index";

import template from "./attestation-confirmation.html";
import {loadAndCalcUnattestableLogicalFlows} from "../../attestation-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";


const bindings = {
    attestationKind: "<",
    parentEntityRef: "<",
    onConfirm: "<?",
    onCancel: "<?"
};

const unknownOrDeprecatedFlowErrorMessage = "Flows cannot be attested as there unknown and/or deprecated datatype usages. Please amend the flows.";

const initialState = {
    disabled: false,
    onConfirm: (attestation) => console.log("default onConfirm handler for attestation-confirmation: "+ instance),
    onCancel: () => console.log("default onCancel handler for attestation-confirmation")
};


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    function disableSubmission(message) {
        vm.message = message;
        vm.disabled = true;
    }

    function enableSubmission() {
        vm.message = null;
        vm.disabled = false;
    }

    function validateLogicalFlows() {
        const selector = mkSelectionOptions(vm.parentEntityRef, "EXACT");
        loadAndCalcUnattestableLogicalFlows($q, serviceBroker, selector)
            .then(unattestableFlows =>
                _.isEmpty(unattestableFlows)
                    ? enableSubmission()
                    : disableSubmission(unknownOrDeprecatedFlowErrorMessage));
    }

    vm.$onInit = () => {
        switch (vm.attestationKind) {
            case "LOGICAL_DATA_FLOW":
                validateLogicalFlows();
                break;
            default:
                enableSubmission();
        }
    };

    vm.confirm = (attestation) => {
        if (vm.disabled) {
            return;
        }
        invokeFunction(vm.onConfirm, attestation);
    };

    vm.cancel = () => {
        invokeFunction(vm.onCancel);
    };
}


controller.$inject = [
    "$q",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller,
    transclude: true
};


export default {
    component,
    id: "waltzAttestationConfirmation"
};
