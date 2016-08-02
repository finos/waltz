/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */
import {stringToBoolean} from "../common";


const bindings = {
    name: '@',
    icon: '@',
    small: '@',
    id: '@',
    collapsible: '<'
};


const template = require('./section.html');


function buildPreferenceKey(state, widgetId, keyName) {
    return `${state.name}.section.${widgetId}.${keyName}`;
}


function controller($scope,
                    $state,
                    userPreferenceService) {
    const vm = this;

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


    $scope.$watch('ctrl.collapsed', (collapsed) => {
        if(vm.id) {
            userPreferenceService.savePreference(buildPreferenceKey($state.current, vm.id, 'collapsed'), collapsed);
        }
    });
}


controller.$inject = [
    '$scope',
    '$state',
    'UserPreferenceService'
];


const component = {
    template,
    bindings,
    controller,
    controllerAs:'ctrl',
    transclude: true
};


export default component;