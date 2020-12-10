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


const bindings = {
    value: "<",
    muteFalse: "<?",
    muteTrue: "<?",
    muteNull: "<?"
};


const template = `<waltz-icon ng-if="$ctrl.booleanValue === true"  ng-style="{opacity: $ctrl.muteTrue ? 0.4 : 1}" name="check" class="text-success"></waltz-icon>
                  <waltz-icon ng-if="$ctrl.booleanValue === false" ng-style="{opacity: $ctrl.muteFalse ? 0.4 : 1}" name="times" class="text-danger"></waltz-icon>
                  <span ng-if="$ctrl.booleanValue == null" ng-style="{opacity: $ctrl.muteNull ? 0.4 : 1}" class="text-muted">-</span>`;


const initialState = {
    booleanValue: null,
    muteFalse: false,
    muteTrue: false,
    muteNull: false
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