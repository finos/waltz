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
import {CORE_API} from "../../../common/services/core-api-utils";


const bindings = {
    attestationKind: "<",
    parentEntityRef: "<",
    attestedEntityRef: "<?",
    onConfirm: "<?",
    onCancel: "<?"
};

const initialState = {
    disabled: false,
    attesting: false,
    onConfirm: (attestation) => console.log("default onConfirm handler for attestation-confirmation: "+ attestation),
    onCancel: () => console.log("default onCancel handler for attestation-confirmation")
};


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    function disableSubmission(messages) {
        vm.message = _
            .chain(messages)
            .map(m => " - " + m)
            .join("\n")
            .value();
        vm.disabled = true;
    }

    function enableSubmission() {
        vm.message = null;
        vm.disabled = false;
    }

    function validateLogicalFlows() {
        serviceBroker
            .loadViewData(CORE_API.AttestationPreCheckStore.logicalFlowCheck, [vm.parentEntityRef])
            .then(r => r.data)
            .then(failures =>  _.isEmpty(failures)
                ? enableSubmission()
                : disableSubmission(failures))
    }

    function validateViewpoints() {
        const categoryId = _.get(vm, ["attestedEntityRef", "id"]);
        serviceBroker
            .loadViewData(CORE_API.AttestationPreCheckStore.viewpointCheck, [vm.parentEntityRef, categoryId])
            .then(r => r.data)
            .then(failures =>  _.isEmpty(failures)
                ? enableSubmission()
                : disableSubmission(failures))
    }

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(
                CORE_API.ApplicationStore.getById,
                [vm.parentEntityRef.id])
            .then(r => {
                if(r.data) {
                    vm.overallRating = r.data.overallRating
                }
            });
        switch (vm.attestationKind) {
            case "LOGICAL_DATA_FLOW":
                validateLogicalFlows();
                break;
            case "MEASURABLE_CATEGORY":
                validateViewpoints();
                break;
            default:
                enableSubmission();
        }
    };

    vm.confirm = (attestation) => {
        if (vm.disabled) {
            return;
        }
        vm.attesting = true;
        vm.onConfirm(attestation)
            .finally(() => vm.attesting = false);
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
