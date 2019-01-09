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

import {getEditRoleForEntityKind} from "../../common/role-utils";
import {initialiseData} from "../../common";


const bindings = {
    entityKind: "@waltzHasRoleForEntityKind"
};


const initialState = {
    show: false
};


const splitRegExp = new RegExp(",\\s*");

/**
 * NOTE: this is not a true 1.5+ component as we need it to
 * work as an attribute level directive and components can
 * only be declared as elements.
 *
 * @param UserService
 */
function controller(UserService) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (nv) => {
        if (! vm.entityKind) return;
        const bits = _.split(vm.entityKind, splitRegExp);
        const primaryEntity = bits[0];
        const secondaryEntity = bits[1];
        const requiredRole = getEditRoleForEntityKind(primaryEntity, secondaryEntity);
        UserService
            .whoami()
            .then(user => vm.show = UserService.hasRole(user, requiredRole));
    };
}


controller.$inject = [
    "UserService"
];


export default () => ({
    replace: true,
    restrict: "A",
    transclude: true,
    scope: {},
    bindToController: bindings,
    controllerAs: "$ctrl",
    template: "<span ng-show=\"$ctrl.show\"><ng-transclude></ng-transclude></span>",
    controller
});
