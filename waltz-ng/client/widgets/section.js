/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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