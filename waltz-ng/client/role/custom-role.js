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
import {displayError} from "../../client/common/error-utils";

function controller(serviceBroker, notification) {
    const vm = this;

    const reload = () =>
        serviceBroker.loadViewData(CORE_API.RoleStore.findAllRoles, [])
        .then(result => vm.roles = result.data);

    reload();

    vm.transformKey = () => {
        vm.roleKey = vm.roleName
            ? vm.roleName
                .toUpperCase()
                .replace(/\s+/g,'_') //replacing whitespaces with _
                .replace(/\W/g, '') //Remove any non alphanumeric character
            : '';
    };

    vm.createRole = (roleName, description) => {
        vm.errorMessage = null;
        vm.successMessage = null;

        let payload = {
            name: roleName,
            description: description,
            key: vm.roleKey
        };
        serviceBroker.execute(CORE_API.RoleStore.createCustomRole, [payload])
            .then(
                () => {
                    notification.info("Role created successfully");
                    reload();
                })
            .catch(e => displayError(notification, "Failed to create role! ", e))
    };
}

controller.$inject = ['ServiceBroker', 'Notification'];


export default {
    template,
    controller,
    controllerAs: "ctrl",
    bindToController: true,
    scope: {}
};

