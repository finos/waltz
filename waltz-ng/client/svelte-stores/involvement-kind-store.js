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
import {checkIsCreateInvolvementKindCommand} from "../common/checks";

export function mkInvolvementKindStore() {

    const findAll = (force = false) => {
        return remote
            .fetchViewList(
                "GET",
                "api/involvement-kind",
                [],
                {force: force});
    }

    const getById = (id, force = false) => {
        return remote
            .fetchViewData(
                "GET",
                `api/involvement-kind/id/${id}`,
                null,
                {},
                {force: force});
    };

    const findKeyInvolvementKindsByEntityKind = (entityKind, force = false) => {
        return remote
            .fetchViewData(
                "GET",
                `api/involvement-kind/key-involvement-kinds/${entityKind}`,
                null,
                [],
                {force: force});
    };

    const update = (cmd) => {
        return remote.execute(
            "PUT",
            "api/involvement-kind/update",
            cmd);
    };

    const create = (cmd) => {
        checkIsCreateInvolvementKindCommand(cmd);
        return remote.execute(
            "POST",
            "api/involvement-kind/update",
            cmd);
    }

    const findUsageStats = (force) => {
        return remote.fetchViewList(
            "GET",
            "api/involvement-kind/usage-stats",
            null,
            {force});
    };

    const findUsageStatsForKind = (id, force) => {
        return remote.fetchViewData(
            "GET",
            `api/involvement-kind/usage-stats/kind/${id}`,
            null,
            [],
            {force});
    };

    return {
        findAll,
        getById,
        findKeyInvolvementKindsByEntityKind,
        update,
        create,
        findUsageStats,
        findUsageStatsForKind
    };
}

export const involvementKindStore = mkInvolvementKindStore();
