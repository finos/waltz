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
import {checkIsCustomEnvironmentUsage} from "../common/checks";

export function mkCustomEnvironmentUsageStore() {

    const remove = (id) => remote
        .execute(
            "DELETE",
            `api/custom-environment-usage/remove/id/${id}`);

    const addAsset = (usage) => {
        checkIsCustomEnvironmentUsage(usage);
        return remote
            .execute(
                "POST",
                "api/custom-environment-usage/add",
                usage);
    }

    const findByOwningEntityRef = (owningEntityRef, force = false) => remote
        .fetchViewList(
            "GET",
            `api/custom-environment-usage/owning-entity/kind/${owningEntityRef.kind}/id/${owningEntityRef.id}`,
            null,
            {force});


    const findUsageInfoByOwningEntityRef = (owningEntityRef, force = false) => remote
        .fetchViewList(
            "GET",
            `api/custom-environment-usage/owning-entity/kind/${owningEntityRef.kind}/id/${owningEntityRef.id}`,
            null,
            {force});


    return {
        findByOwningEntityRef,
        addAsset,
        remove,
        findUsageInfoByOwningEntityRef
    };
}

export const customEnvironmentUsageStore = mkCustomEnvironmentUsageStore();