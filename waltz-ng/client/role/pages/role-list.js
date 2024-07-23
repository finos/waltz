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

import {CORE_API} from "../../common/services/core-api-utils";
import template from "./role-list.html";
import {displayError} from "../../common/error-utils";
import toasts from "../../svelte-stores/toast-store";
import RoleListPanel from "../svelte/RoleListPanel.svelte";
import {initialiseData} from "../../common";

const initialState = {
    RoleListPanel
};

const bindings = {};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const reload = () =>
        serviceBroker
            .loadViewData(CORE_API.RoleStore.findAllRoles, [], {force: true})
            .then(result => vm.roles = result.data);

    reload();

    vm.transformKey = () => {
        vm.roleKey = vm.roleName
            ? vm.roleName
                .toUpperCase()
                .replace(/\s*&\s*/g," AND ") //replacing & with AND
                .replace(/[\s\-]+/g,"_") //replacing whitespaces and hyphens with _
                .replace(/\W/g, "") //Remove any non alphanumeric character
            : "";
    };

    vm.createRole = (roleName, description) => {
        let payload = {
            name: roleName,
            description: description,
            key: vm.roleKey
        };

        serviceBroker
            .execute(CORE_API.RoleStore.createCustomRole, [payload])
            .then(
                () => {
                    toasts.info("Role created successfully");
                    reload();
                    vm.roleKey = ""
                    vm.roleName = ""
                    vm.roleDescription = ""
                })
            .catch(e => displayError("Failed to create role! ", e));
    };
}

controller.$inject = ["ServiceBroker"];


const component = {
    bindings,
    controller,
    template,
};


export default {
    id: "waltzRoleList",
    component
};
