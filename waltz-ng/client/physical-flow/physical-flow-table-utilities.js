/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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




