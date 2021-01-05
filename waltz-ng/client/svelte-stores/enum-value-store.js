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
import {writable} from "svelte/store";
import {$http} from "../common/WaltzHttp";

export function mkEnumValueStore() {
    const store = writable([]);
    const loadOld = (force = false) => remote
        .fetchAppList("GET", "api/enum-value", null, {force});
    const load = (force = false) => {
        $http
            .get("api/enum-value")
            .then(r => store.set(r.data));
        return store;
    }

    return {
        load: loadOld
    };
}

export const enumValueStore = mkEnumValueStore();