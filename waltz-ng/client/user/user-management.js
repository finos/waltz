
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
import {initialiseData} from "../common";
import {CORE_API} from "../common/services/core-api-utils";
// ---
import template from "./user-management.html";
import roles from "./roles";


const initialState = {
    numAllowedWithoutFilter: 100,
    roles
};




function controller(serviceBroker) {

    const vm =  initialiseData(this, initialState);

    serviceBroker.loadViewData(CORE_API.UserStore.findAll, [])
        .then(result => vm.users = result.data);

    vm.dismiss = () => {
        vm.newUser = null;
        vm.selectedUser = null;
        vm.newPassword1 = null;
        vm.newPassword2 = null;
    };

    vm.userSelected = (user) => {
        vm.dismiss();
        vm.selectedUser = user;
        vm.roleSelections = _.reduce(
            user.roles,
            (acc, role) => { acc[role] = true; return acc; },
            {});
    };

    vm.addUserSelected = () => {
        vm.dismiss();
        vm.newUser = { userName: "", password: ""};
    };

    vm.isValidNewUser = (user) => {
        return user.userName && user.password;
    };

    vm.registerUser = (user) => {
        serviceBroker
            .execute(CORE_API.UserStore.register, [user])
            .then(
                () => {
                    vm.userSelected(user);
                    vm.users = [...vm.users, user];
                },
                err => {
                    console.error("Error registering user: ", err);
                    vm.lastError = err.data;
                }
            );
    };

    vm.updateUser = (user, roleSelections, password1, password2) => {

        if (password1 !== password2) {
            vm.lastError = { id: "MISMATCH", message: "Passwords do not match"};
            return;
        }


        const roles = _.chain(roleSelections)
            .map((v, k) => v ? k : null)  // get selected key name or null if not selected
            .compact() // remove non selected names
            .value();

        serviceBroker
            .execute(CORE_API.UserStore.updateRoles, [user.userName, roles])
            .then(
                () => {
                    user.roles = roles;
                    vm.dismiss();
                },
                e => {
                    vm.lastError = e.data;
                }
            );

        if (password1) {
            serviceBroker
                .execute(CORE_API.UserStore.resetPassword, [user.userName, password1]);
        }
    };

    vm.hasRole = (user, role) => {
        const existingRoles = user.roles || [];
        return existingRoles.indexOf(role) > -1;
    };

    vm.deleteUser = (user) => {
        serviceBroker
            .execute(CORE_API.UserStore.deleteUser, [user.userName])
            .then(
                () => {
                    vm.users = _.reject(vm.users, u => u === user);
                    vm.dismiss();
                },
                e => vm.lastError = e.data
            );

        return false; // prevent form submission
    };

    function setAllSelectionsTo(b) {
        vm.roleSelections = _.reduce(
            _.map(roles, "key"),
            (acc, k) => {
                acc[k] = b;
                return acc;
            },
            {});
    }

    vm.selectAll = () => {
        setAllSelectionsTo(true);
    };

    vm.deselectAll = () => {
        setAllSelectionsTo(false);
    };
}


controller.$inject = [ "ServiceBroker" ];


export default {
    template,
    controller,
    controllerAs: "ctrl",
    bindToController: true,
    scope: {}
};

