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

import {initialiseData} from "../common";
import {stringToBoolean} from "../common/string-utils";
import template from './section.html';


const bindings = {
    name: '@',
    icon: '@',
    small: '@',
    id: '@',
    collapsible: '<'
};


const initialState = {
    collapsed: false,
    collapsible: true
};


function buildPreferenceKey(state, widgetId, keyName) {
    return `${state.name}.section.${widgetId}.${keyName}`;
}


function controller($state,
                    userPreferenceService) {
    const vm = initialiseData(this, initialState);

    if(vm.id) {
        userPreferenceService.loadPreferences()
            .then(preferences => {
                const preferenceKey = buildPreferenceKey($state.current, vm.id, 'collapsed');
                if(preferences[preferenceKey]) {
                    const keyValue = stringToBoolean(preferences[preferenceKey].value)
                    vm.collapsed = keyValue;
                }

            });
    }


    vm.expand = (collapsed) => {
        vm.collapsed = collapsed;
        if(vm.id) {
            userPreferenceService.savePreference(buildPreferenceKey($state.current, vm.id, 'collapsed'), collapsed);
        }
    }
}


controller.$inject = [
    '$state',
    'UserPreferenceService'
];


const component = {
    template,
    bindings,
    controller,
    transclude: true
};


export default component;