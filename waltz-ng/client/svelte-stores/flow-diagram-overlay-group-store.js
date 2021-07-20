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

export function mkFlowDiagramOverlayGroupStore() {

    const findByDiagramId = (id, force = false) => {
        return remote
            .fetchViewData(
                "GET",
                `api/flow-diagram-overlay-group/diagram-id/${id}`,
                null,
                [],
                {force});
    };

    const findOverlaysByDiagramId = (id) => {
        return remote
            .fetchViewData(
                "GET",
                `api/flow-diagram-overlay-group/overlays/diagram-id/${id}`,
                null,
                [],
                {force: true});
    };

    const createGroup = (group) => {
        return remote
            .execute(
                "POST",
                "api/flow-diagram-overlay-group/create",
                group)
            .then(() => findByDiagramId(group.diagramId, true));
    };

    const deleteGroup = (diagramId, id) => {
        return remote
            .execute(
                "DELETE",
                `api/flow-diagram-overlay-group/id/${id}`)
            .then(() => findByDiagramId(diagramId, true));
    }

    const cloneOverlayGroup = (diagramId, groupId) => {
        return remote
            .execute(
                "POST",
                `api/flow-diagram-overlay-group/clone/diagram-id/${diagramId}/id/${groupId}`)
            .then(() => findByDiagramId(diagramId, true));
    }

    return {
        findByDiagramId,
        findOverlaysByDiagramId,
        createGroup,
        deleteGroup,
        cloneOverlayGroup
    };
}

export const flowDiagramOverlayGroupStore = mkFlowDiagramOverlayGroupStore();
