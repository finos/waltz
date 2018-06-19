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

        const hoverFormat = vm.daysOnly
            ? formats.daysOnly
            : formats.daysAndMinutes;

        const current = vm.daysOnly ? moment().startOf('day') : moment();

        vm.hoverValue = m.local().format(hoverFormat);
        vm.fromNow = !vm.daysOnly
            ? m.fromNow()
            : Math.round(current.diff(m, 'days', true)) !== 0 ? m.from(current) : 'today';
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
