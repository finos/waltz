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

import _ from "lodash";

const BINDINGS = {
    expr: '@waltzHasSetting'
};

function controller(settingsService) {

    const vm = this;

    settingsService
        .findAll()
        .then(settings => {
            const [name, value] = vm.expr.split(/\s*=\s*/);
            vm.show = _.find(settings, { name }).value == value;
        });

    vm.show = false;
}

controller.$inject = ['SettingsService'];


export default () => ({
    replace: false,
    restrict: 'A',
    transclude: true,
    scope: {},
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    template: '<div ng-show="ctrl.show"><ng-transclude></ng-transclude></div>',
    controller
});
