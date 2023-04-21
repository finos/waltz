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

export function mkReportGridMemberStore() {

    const findByGridId = (id, force = false) => remote
        .fetchViewList("GET", `api/report-grid-member/grid-id/${id}`, [], {force});

    const findPeopleByGridId = (id, force = false) => remote
        .fetchViewList("GET", `api/report-grid-member/grid-id/${id}/people`, [], {force});

    const updateRole = (gridId, updateRoleCommand) => remote
        .execute("POST", `api/report-grid-member/grid-id/${gridId}/update-role`, updateRoleCommand);

    const deleteRole = (cmd) => remote
        .execute("POST", "api/report-grid-member/delete", cmd);

    const create = (cmd) => remote
        .execute("POST", "api/report-grid-member/create", cmd);

    return {
        findByGridId,
        findPeopleByGridId,
        updateRole,
        deleteRole,
        create
    };
}

export const reportGridMemberStore = mkReportGridMemberStore();
