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
