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

export function mkReportGridStore() {

    const findAll = (force = false) => remote
        .fetchAppList("GET", "api/report-grid/all", [], {force});

    const findForUser = (force = false) => remote
        .fetchAppList("GET", "api/report-grid/user", [], {force});

    const findForOwner = (force = false) => remote
        .fetchAppList("GET", "api/report-grid/owner", [], {force});


    const findAdditionalColumnOptionsForKind = (kind, force = false) => remote
        .fetchAppList("GET", `api/report-grid/additional-column-options/kind/${kind}`, [], {force});

    const updateColumnDefinitions = (id, updatecommand) => remote
        .execute(
            "POST",
            `api/report-grid/id/${id}/column-definitions/update`,
            updatecommand);

    const create = (createCommand) => {
        return remote.execute("POST", "api/report-grid/create", createCommand);
    };

    const update = (id, updateCmd) => {
        return remote.execute("POST", `api/report-grid/id/${id}/update`, updateCmd);
    };

    const remove = (id, updateCmd) => {
        return remote.execute("DELETE", `api/report-grid/id/${id}`);
    };


    return {
        findAll,
        findForUser,
        findForOwner,
        findAdditionalColumnOptionsForKind,
        updateColumnDefinitions,
        create,
        update,
        remove
    };
}

export const reportGridStore = mkReportGridStore();
