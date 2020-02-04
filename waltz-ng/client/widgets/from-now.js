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

import moment from "moment";


const BINDINGS = {
    timestamp: '<',
    daysOnly: '<' // boolean
};


const formats = {
    daysAndMinutes: 'ddd Do MMM YYYY - HH:mm:ss',
    daysOnly: 'ddd Do MMM YYYY',
    parse: 'YYYY-MM-DDThh:mm:ss.SSS'
};


const template = '<span title="{{ ctrl.hoverValue }}" ng-bind="ctrl.fromNow"></span>';


function controller($scope) {
    const vm = this;

    $scope.$watch('ctrl.timestamp', (nv) => {
        if (! nv) {
            vm.hoverValue = null;
            vm.fromNow = null;
            return;
        }

        const m = moment.utc(nv, formats.parse );


        if(vm.daysOnly) {
            const current = moment().utc().startOf('day');
            vm.hoverValue = m.format(formats.daysOnly);
            vm.fromNow = Math.round(current.diff(m, 'days', true)) !== 0 ? m.from(current) : 'today';
        } else {
            vm.hoverValue = m.local().format(formats.daysAndMinutes);
            vm.fromNow = m.fromNow();
        }
    });
}

controller.$inject=['$scope'];


const directive = {
    restrict: 'E',
    replace: true,
    scope: {},
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    template,
    controller
};


export default () => directive;
