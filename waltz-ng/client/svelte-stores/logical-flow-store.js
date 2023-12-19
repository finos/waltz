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
import {checkIsEntityRef} from "../common/checks";

export function mkLogicalFlowStore() {

    const findByEntityReference = (ref) => {
        return remote.fetchViewList("GET", `api/logical-flow/entity/${ref.kind}/${ref.id}`);
    };


    const getById = (id) => {
        return remote.fetchViewList("GET", `api/logical-flow/${id}`);
    };

    const findBySelector = (selector, force = false) => {
        return remote.fetchViewList(
            "POST",
            "api/logical-flow/selector",
            selector,
            {force});
    };


    const findEditableFlowIdsForParentReference = (ref, force = false) => {
        return remote.fetchViewList(
            "GET",
            `api/logical-flow/entity/${ref.kind}/${ref.id}/editable-flows`,
            null,
            {force});
    }


    const getFlowGraphSummary = (ref, dtId, force = false) => {
        checkIsEntityRef(ref);
        return remote
            .fetchViewData(
                "GET",
                `api/logical-flow/entity/${ref.kind}/${ref.id}/data-type/${dtId}/graph-summary`,
                null,
                {},
                {force: force});
    };


    const getViewForSelector = (selector, force = false) => {
        return remote
            .fetchViewData(
                "POST",
                "api/logical-flow/view",
                selector,
                null,
                {force});
    };

    const findPermissionsForFlow = (flowId, force = false) => {
        return remote
            .fetchViewList(
                "GET",
                `api/logical-flow/id/${flowId}/permissions`,
                null,
                {},
                {force: force});
    };


    const addFlow = (command) => {
        return remote
            .execute(
                "POST",
                "api/logical-flow",
                command);
    };


    const removeFlow = (flowId) => {
        return remote
            .execute(
                "DELETE",
                `api/logical-flow/${flowId}`,
                null);
    };


    return {
        findByEntityReference,
        findBySelector,
        getFlowGraphSummary,
        getById,
        addFlow,
        findEditableFlowIdsForParentReference,
        findPermissionsForFlow,
        getViewForSelector,
        removeFlow
    };
}

export const logicalFlowStore = mkLogicalFlowStore();
