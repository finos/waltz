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

export function mkFlowDiagramEntityStore() {

    const findByDiagramId = (id, force = false) => remote
        .fetchViewData(
            "GET", 
            `api/flow-diagram-entity/id/${id}`, 
            null, 
            [], 
            {force})

    const findByEntityReference = (ref) => remote
        .fetchViewData(
            "GET",
            `api/flow-diagram-entity/entity/${ref.kind}/${ref.id}`);

    const findForSelector = (options, force = false) => remote
        .fetchViewData(
            "POST",
            "api/flow-diagram-entity/selector",
            options,
            [],
            force);

    const addRelationship = (diagramId, ref) => {
        checkIsEntityRef(ref);
        return remote
            .execute(
                "POST",
                `api/flow-diagram-entity/id/${diagramId}/${ref.kind}/${ref.id}`, {})
            .then(() => findByDiagramId(diagramId, true));
    };

    const removeRelationship = (diagramId, ref) => {
        checkIsEntityRef(ref);
        return remote
            .execute(
                "DELETE",
                `api/flow-diagram-entity/id/${diagramId}/${ref.kind}/${ref.id}`)
            .then(() => findByDiagramId(diagramId, true));
    };

    return {
        findByDiagramId,
        findByEntityReference,
        findForSelector,
        addRelationship,
        removeRelationship
    };
}

export const flowDiagramEntityStore = mkFlowDiagramEntityStore();
