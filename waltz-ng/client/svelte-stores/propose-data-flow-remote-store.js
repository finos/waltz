/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016 - 2025 Waltz open source project
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
    const BASE_URL = "api/mc";

    const proposeDataFlow = (command) => remote
        .execute("POST",
        `${BASE_URL}/propose`,
        command);

    const getProposedFlowsForUser = (command) => remote
        .execute("POST",
        `${BASE_URL}/propose-flow`,
        command);

    return {
        proposeDataFlow,
        getProposedFlowsForUser
    }
}

export const proposeDataFlowRemoteStore = mkStore();