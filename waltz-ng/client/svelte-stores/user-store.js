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

import {remote} from "./remote";

export function mkUserStore() {

    const findAll = (force = false) => remote
        .fetchAppList("GET", "api/user", [], {force});

    const load = (force = false) => remote
        .fetchAppDatum("GET", "api/user/whoami", null, {force});

    const getByUserId = (userId, force = false) => remote
        .fetchViewDatum("GET", `api/user/user-id/${userId}`, null, {force});

    const updateRoles = (userName, roles, comment) => remote.execute(
        "POST",
        `api/user/${userName}/roles`, {roles, comment});

    const register = (newUserCmd) => remote.execute(
        "POST",
        "api/user/new-user",
        newUserCmd);

    const resetPassword = (userName, newPassword, currentPassword) => {
        const cmd = {
            userName,
            newPassword,
            currentPassword
        };
        return remote.execute(
            "POST",
            "api/user/reset-password",
            cmd);
    };

    const bulkUploadPreview = (mode, rows = []) => remote.execute(
        "POST",
        `api/user/bulk/${mode}/preview`,
        rows);

    const bulkUpload = (mode, rows = []) => remote.execute(
        "POST",
        `api/user/bulk/${mode}/upload`,
        rows);

    const deleteUser = (username) => remote.execute("DELETE", `api/user/${username}`, null);

    return {
        findAll,
        load,
        getByUserId,
        updateRoles,
        register,
        resetPassword,
        deleteUser,
        bulkUpload,
        bulkUploadPreview
    };
}


export const userStore = mkUserStore();
