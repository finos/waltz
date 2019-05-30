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

import {CORE_API} from "../common/services/core-api-utils";
import template from "./custom-role.html";


function controller(serviceBroker) {
    const vm = this;

    serviceBroker.loadViewData(CORE_API.RoleStore.findAllRoles, [])
        .then(result => vm.roles = result.data);

    vm.createRole = (roleName, description) => {
        vm.errorMessage = null;
        vm.successMessage = null;
        console.log('create new role ' + roleName)
        let payload = {
            name: roleName,
            description: description
        };
        serviceBroker.execute(CORE_API.RoleStore.createCustomRole, [payload])
            .then(
                () => {
                    vm.successMessage = 'role has been created successfully'
                },
                err => {
                    vm.errorMessage = 'Failed to create role';
                }
            );

        console.log(vm.errorMessage);
        console.log(vm.successMessage);
    };
}

controller.$inject = ["ServiceBroker"];


export default {
    template,
    controller,
    controllerAs: "ctrl",
    bindToController: true,
    scope: {}
};

