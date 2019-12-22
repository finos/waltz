
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

import _ from "lodash";
import {initialiseData} from "../common";
import {CORE_API} from "../common/services/core-api-utils";
import template from "./user-management.html";
import roles from "./system-roles";


const initialState = {
    numAllowedWithoutFilter: 100,
    roles
};



function filterUsers(users = [], qry = null) {
    if (_.isEmpty(users) || qry === '') return [];

    const qryStr = _.toLower(qry);
    return users.filter(user => user.searchStr.indexOf(qryStr) > -1);
}

function enrichUsersWithSearchStr(user) {
    const searchStr = _.toLower(user.userName);
    return Object.assign({}, user, { searchStr });
}

function controller(serviceBroker) {
    const vm =  initialiseData(this, initialState);

    serviceBroker.loadViewData(CORE_API.UserStore.findAll, [])
        .then(result => {
            vm.users = _.map(result.data, d => enrichUsersWithSearchStr(d));

            if(vm.users.length <= vm.numAllowedWithoutFilter) {
                vm.filteredUsers = vm.users;
            }
        });

    serviceBroker.loadViewData(CORE_API.RoleStore.findAllRoles, [])
        .then(result => vm.roles = result.data);

    const refresh = () => {
        vm.filteredUsers=[] = filterUsers(vm.users, vm.qry);
    };

    vm.onQueryStrChange = (qry) => {
        vm.qry = qry;
        refresh();
    };


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
            _.map(vm.roles, "key"),
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

