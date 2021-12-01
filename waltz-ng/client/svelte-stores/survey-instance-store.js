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

export function mkSurveyInstanceStore() {

    const getPermissions = (id) => remote
        .fetchViewDatum("GET", `api/survey-instance/${id}/permissions`);

    const findPossibleActions = (id, force = false) => remote
        .fetchViewDatum("GET", `api/survey-instance/${id}/actions`, [], {force});

    const findRecipients = (id) => remote
        .fetchViewList("GET",`api/survey-instance/${id}/recipients`);

    const findOwners = (id) => remote
        .fetchViewList("GET",`api/survey-instance/${id}/owners`);

    const findResponses = (id) => remote
        .fetchViewList("GET",`api/survey-instance/${id}/responses`);

    const updateStatus = (id, command) => remote
        .execute("PUT",`api/survey-instance/${id}/status`, command);

    return {
        getPermissions,
        findPossibleActions,
        findRecipients,
        findOwners,
        findResponses,
        updateStatus
    };
}

export const surveyInstanceStore = mkSurveyInstanceStore();