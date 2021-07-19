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
import {initialiseData} from "../../common";
import {stringToBoolean} from "../string-utils";
import template from "./boolean-renderer.html";


const bindings = {
    value: "<",
    muteFalse: "<?",
    muteTrue: "<?",
    muteNull: "<?",
    trueLabel: "<?",
    falseLabel: "<?",
    nullLabel: "<?",
};


const initialState = {
    booleanValue: null,
    muteFalse: false,
    muteTrue: false,
    muteNull: false,
    trueLabel: "Yes",
    falseLabel: "No",
    nullLabel: "Not provided",
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        if (vm.value === true || stringToBoolean(vm.value) === true) {
            vm.booleanValue = true;
        } else if (vm.value === false || stringToBoolean(vm.value) === false) {
            vm.booleanValue = false;
        } else {
            vm.booleanValue = null;
        }
    };
}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;