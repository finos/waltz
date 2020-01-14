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


import {CORE_API} from "../common/services/core-api-utils";
import _ from "lodash";


/**
 * Determines if the current user is owner for the
 * given group reference.
 *
 * @param serviceBroker
 * @param entityRef
 */
export function isGroupOwner(serviceBroker, entityRef) {
    return determineGroupRoles(serviceBroker, entityRef)
        .then(roles => _.includes(roles, "OWNER"));
}


export function determineGroupRoles(serviceBroker, entityRef) {
    let user = null;
    let members = [];

    const userPromise = serviceBroker
        .loadAppData(CORE_API.UserStore.whoami)
        .then(r => user = r.data);

    const groupPromise = serviceBroker
        .loadViewData(
            CORE_API.AppGroupStore.getById,
            [ entityRef.id ])
        .then(r => members = _.get(r, "data.members", []));

    return userPromise
        .then(() => groupPromise)
        .then(() => _
                .chain(members)
                .filter(m => m.userId === user.userName)
                .map("role")
                .value());
}