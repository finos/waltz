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

export function mkApplicationStore() {

    const getById = (id) => remote
        .fetchViewData("GET", `api/app/id/${id}`, null, {});

    const update = (id, data) => remote
        .execute("POST", `api/app/${id}`, data);

    const registerApp = (data) => remote
        .execute("POST", `api/app`, data);

    const findBySelector = (selector) => remote
        .fetchViewList("POST", "api/app/selector", selector);

    const getViewBySelector = (selector) => remote
        .fetchViewDatum("POST", "api/app/view/selector", selector);


    const findByExternalId = (extId) => remote
        .fetchViewList("GET", `api/app/external-id/${extId}`, null );



    return {
        getById,
        update,
        registerApp,
        findByExternalId,
        findBySelector,
        getViewBySelector
    };
}

export const applicationStore = mkApplicationStore();
