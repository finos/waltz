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

export function mkStore()  {
    const BASE_URL = "api/access-log";

    const findAccessLogsForUser = (userId, force) => remote
        .fetchViewList("GET",
            `${BASE_URL}/user/${userId}`,
            null,
            {force});

    const findActiveUsersSince = (minutes, force) => remote
        .fetchViewList("GET",
            `${BASE_URL}/active/${minutes}`,
            null,
            {force});

    const findAccessLogsSince = (days, force) => remote
            .fetchViewList(
                "GET",
            `${BASE_URL}/counts_by_state/${days}`,
            null,
            {force});

    const findAccessLogCountsSince = (days, force) => remote
        .fetchViewList(
            "GET",
            `${BASE_URL}/counts_since/${days}`,
            null,
            {force});

    const findDailyActiveUserCountsSince = (days, force) => remote
        .fetchViewList(
            "GET",
            `${BASE_URL}/users_since/${days}`,
            null,
            {force});

    const findYearOnYearUsers = (mode, force) => remote
        .fetchViewList(
            "GET",
            `${BASE_URL}/summary/year_on_year/${mode}`,
            null,
            {force});

    return {
        findAccessLogsForUser,
        findActiveUsersSince,
        findAccessLogsSince,
        findAccessLogCountsSince,
        findDailyActiveUserCountsSince,
        findYearOnYearUsers
    };
}

export const accessLogStore = mkStore();