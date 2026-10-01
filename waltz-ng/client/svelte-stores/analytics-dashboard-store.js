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

export function mkStore() {
    const ACCESS_LOG_BASE = "api/access-log";
    const CHANGE_LOG_BASE = "api/change-log-summaries";

    const range = (startDate, endDate) => `startDate=${startDate}&endDate=${endDate}`;

    // Single source of truth for every analytics endpoint url, shared by the reactive
    // `find*` stores (used by the charts) and the promise-based export fetch below.
    const urls = {
        accessLogTrends: (period, s, e) => `${ACCESS_LOG_BASE}/summary/period/${period}?${range(s, e)}`,
        activityByHour: (s, e) => `${ACCESS_LOG_BASE}/analytics/activity-by-hour?${range(s, e)}`,
        activityByDay: (s, e) => `${ACCESS_LOG_BASE}/analytics/activity-by-day?${range(s, e)}`,
        sessionDurations: (s, e) => `${ACCESS_LOG_BASE}/analytics/session-durations?${range(s, e)}`,
        topUsers: (s, e, limit) => `${ACCESS_LOG_BASE}/analytics/top-users?${range(s, e)}&limit=${limit}`,
        topPages: (s, e, limit) => `${ACCESS_LOG_BASE}/analytics/top-pages?${range(s, e)}&limit=${limit}`,
        changeLogTrends: (period, s, e) => `${CHANGE_LOG_BASE}/changes?${range(s, e)}&period=${period}`,
        changesByDay: (s, e) => `${CHANGE_LOG_BASE}/analytics/by-day?${range(s, e)}`,
        changesByEntityKind: (s, e, limit) => `${CHANGE_LOG_BASE}/analytics/by-entity-kind?${range(s, e)}&limit=${limit}`,
        changesByChildKind: (s, e, limit) => `${CHANGE_LOG_BASE}/analytics/by-child-kind?${range(s, e)}&limit=${limit}`,
        changesByOperation: (s, e) => `${CHANGE_LOG_BASE}/analytics/by-operation?${range(s, e)}`,
        operationTrends: (period, s, e) => `${CHANGE_LOG_BASE}/analytics/operation-trends?${range(s, e)}&period=${period}`,
        changesBySeverity: (s, e) => `${CHANGE_LOG_BASE}/analytics/by-severity?${range(s, e)}`,
        topContributors: (s, e, limit) => `${CHANGE_LOG_BASE}/analytics/top-contributors?${range(s, e)}&limit=${limit}`,
        topContributorsTrends: (period, s, e, limit) => `${CHANGE_LOG_BASE}/analytics/top-contributors-trends?${range(s, e)}&period=${period}&limit=${limit}`
    };

    const findAccessLogTrends = (period, startDate, endDate, force) => remote
        .fetchViewList("GET", urls.accessLogTrends(period, startDate, endDate), null, {force});

    const findActivityByHour = (startDate, endDate, force) => remote
        .fetchViewList("GET", urls.activityByHour(startDate, endDate), null, {force});

    const findActivityByDay = (startDate, endDate, force) => remote
        .fetchViewList("GET", urls.activityByDay(startDate, endDate), null, {force});

    const findSessionDurations = (startDate, endDate, force) => remote
        .fetchViewList("GET", urls.sessionDurations(startDate, endDate), null, {force});

    const findTopUsers = (startDate, endDate, limit, force) => remote
        .fetchViewList("GET", urls.topUsers(startDate, endDate, limit), null, {force});

    const findTopPages = (startDate, endDate, limit, force) => remote
        .fetchViewList("GET", urls.topPages(startDate, endDate, limit), null, {force});

    const findChangeLogTrends = (period, startDate, endDate, force) => remote
        .fetchViewList("GET", urls.changeLogTrends(period, startDate, endDate), null, {force});

    const findChangesByDay = (startDate, endDate, force) => remote
        .fetchViewList("GET", urls.changesByDay(startDate, endDate), null, {force});

    const findChangesByEntityKind = (startDate, endDate, limit, force) => remote
        .fetchViewList("GET", urls.changesByEntityKind(startDate, endDate, limit), null, {force});

    const findChangesByChildKind = (startDate, endDate, limit, force) => remote
        .fetchViewList("GET", urls.changesByChildKind(startDate, endDate, limit), null, {force});

    const findChangesByOperation = (startDate, endDate, force) => remote
        .fetchViewList("GET", urls.changesByOperation(startDate, endDate), null, {force});

    const findOperationTrends = (period, startDate, endDate, force) => remote
        .fetchViewList("GET", urls.operationTrends(period, startDate, endDate), null, {force});

    const findChangesBySeverity = (startDate, endDate, force) => remote
        .fetchViewList("GET", urls.changesBySeverity(startDate, endDate), null, {force});

    const findTopContributors = (startDate, endDate, limit, force) => remote
        .fetchViewList("GET", urls.topContributors(startDate, endDate, limit), null, {force});

    const findTopContributorsTrends = (period, startDate, endDate, limit, force) => remote
        .fetchViewList("GET", urls.topContributorsTrends(period, startDate, endDate, limit), null, {force});

    // Fetch every dataset once for a spreadsheet export. Resolves to a map keyed by
    // dataset so the caller can build one worksheet per chart.
    const fetchAllForExport = (period, startDate, endDate, limit = 15) => {
        const get = (url) => remote.execute("GET", url).then(r => r.data);
        return Promise
            .all([
                get(urls.accessLogTrends(period, startDate, endDate)),
                get(urls.activityByHour(startDate, endDate)),
                get(urls.activityByDay(startDate, endDate)),
                get(urls.sessionDurations(startDate, endDate)),
                get(urls.topUsers(startDate, endDate, limit)),
                get(urls.topPages(startDate, endDate, limit)),
                get(urls.changeLogTrends(period, startDate, endDate)),
                get(urls.changesByDay(startDate, endDate)),
                get(urls.changesByEntityKind(startDate, endDate, limit)),
                get(urls.changesByChildKind(startDate, endDate, limit)),
                get(urls.changesByOperation(startDate, endDate)),
                get(urls.operationTrends(period, startDate, endDate)),
                get(urls.changesBySeverity(startDate, endDate)),
                get(urls.topContributors(startDate, endDate, limit))
            ])
            .then(([
                accessLogTrends, activityByHour, activityByDay, sessionDurations,
                topUsers, topPages, changeLogTrends, changesByDay, changesByEntityKind,
                changesByChildKind, changesByOperation, operationTrends, changesBySeverity,
                topContributors
            ]) => ({
                accessLogTrends, activityByHour, activityByDay, sessionDurations,
                topUsers, topPages, changeLogTrends, changesByDay, changesByEntityKind,
                changesByChildKind, changesByOperation, operationTrends, changesBySeverity,
                topContributors
            }));
    };

    return {
        findAccessLogTrends,
        findActivityByHour,
        findActivityByDay,
        findSessionDurations,
        findTopUsers,
        findTopPages,
        findChangeLogTrends,
        findChangesByDay,
        findChangesByEntityKind,
        findChangesByChildKind,
        findChangesByOperation,
        findOperationTrends,
        findChangesBySeverity,
        findTopContributors,
        findTopContributorsTrends,
        fetchAllForExport
    };
}

export const analyticsDashboardStore = mkStore();
