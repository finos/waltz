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


const bindings = {
    instance: "<",
    run: "<",
    attestedEntityRef: "<?",
    onConfirm: "<?",
    onCancel: "<?"
};


const initialState = {
    onConfirm: (attestation) => console.log("default onConfirm handler for attestation-confirmation: "+ instance),
    onCancel: () => console.log("default onCancel handler for attestation-confirmation")
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.confirm = (attestation) => {
        invokeFunction(vm.onConfirm, attestation);
    };

    vm.cancel = () => {
        invokeFunction(vm.onCancel);
    };
}


controller.$inject = [];


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
