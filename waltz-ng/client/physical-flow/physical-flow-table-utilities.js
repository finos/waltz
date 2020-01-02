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

import _ from "lodash";
import {mkEnumGridCell, mkLinkGridCell} from "../common/grid-utils";
import {CORE_API} from "../common/services/core-api-utils";


export const columnDef = {
    name : mkLinkGridCell("Name", "specification.name", "physicalFlow.id", "main.physical-flow.view"),
    extId : { field: "specification.externalId", displayName: "Ext. Id"},
    observation: mkEnumGridCell("Observation", "physicalFlow.freshnessIndicator", "FreshnessIndicator", true, true),
    format: { field: "specification.format", displayName: "Format", cellFilter: "toDisplayName:\"dataFormatKind\"" },
    transport: { field: "physicalFlow.transport", displayName: "Transport", cellFilter: "toDisplayName:\"TransportKind\"" },
    frequency: { field: "physicalFlow.frequency", displayName: "Frequency", cellFilter: "toDisplayName:\"frequencyKind\"" },
    criticality: { field: "physicalFlow.criticality", displayName: "Criticality", cellFilter: "toDisplayName:\"physicalFlowCriticality\"" },
    description: { field: "specification.description", displayName: "Description"},
    source: mkLinkGridCell("Source App", "logicalFlow.source.name", "logicalFlow.source.id", "main.app.view"),
    target: mkLinkGridCell("Target App", "logicalFlow.target.name", "logicalFlow.target.id", "main.app.view"),
    basisOffset: { field: "physicalFlow.basisOffset", displayName: "Basis", cellFilter: "toBasisOffset" }
};

export function withWidth(name, width) {
    return Object.assign(name, { width: width})
}


export function fetchData(entityRef, $q, serviceBroker) {
    const selector = {
        entityReference: entityRef,
        scope: "EXACT"
    };
    const physicalFlowPromise = serviceBroker
        .loadViewData(CORE_API.PhysicalFlowStore.findBySelector, [selector])
        .then(r => r.data);
    const specPromise = serviceBroker
        .loadViewData(CORE_API.PhysicalSpecificationStore.findBySelector, [selector])
        .then(r => r.data);
    const logicalFlowPromise = serviceBroker
        .loadViewData(CORE_API.LogicalFlowStore.findBySelector, [selector])
        .then(r => r.data);

    return $q.all([physicalFlowPromise, specPromise, logicalFlowPromise])
        .then(([physicalFlows, specs, logicalFlows]) => mkData(entityRef, specs, physicalFlows, logicalFlows));

}

function mkData(primaryRef,
                specifications = [],
                physicalFlows = [],
                logicalFLows = [])
{
    if (!primaryRef) return [];

    const specsById = _.keyBy(specifications, "id");
    const logicalFlowsById = _.keyBy(logicalFLows, "id");

    const enrichFlow = (pf) => {
        return {
            physicalFlow: pf,
            specification: specsById[pf.specificationId],
            logicalFlow: logicalFlowsById[pf.logicalFlowId]
        };
    };

    return _.map(physicalFlows, enrichFlow);
}




