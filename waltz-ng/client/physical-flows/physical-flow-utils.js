

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
import _ from 'lodash';


/**
 * Given a physical flow, returns a new extended object with the following:
 * `frequencyName`, `transportName` and `criticalityName`.
 *
 * @param physFlow
 * @param displayNameService
 * @returns {*}
 */
export function enrichPhysicalFlow(physFlow, displayNameService) {
    const frequencyName = displayNameService.lookup('frequencyKind', physFlow.frequency);
    const transportName = displayNameService.lookup('TransportKind', physFlow.transport);
    const criticalityName = displayNameService.lookup('physicalFlowCriticality', physFlow.criticality);
    const displayNames = { frequencyName, transportName, criticalityName };
    return Object.assign({}, physFlow, displayNames);
}


/**
 * Given a specification returns a new extended object with the `format` enum value
 * resolved as `formatName`.
 *
 * @param spec
 * @param displayNameService
 * @returns {*}
 */
export function enrichSpecification(spec, displayNameService) {
    const formatName = displayNameService.lookup('dataFormatKind', spec.format);
    return Object.assign({}, spec, { formatName });
}


/**
 * Given either an enriched physical flow or specification returns a new object with the
 * enriched fields removed.
 * @param o
 * @returns {*}
 */
export function removeEnrichments(o) {
    return _.omit(o, ['frequencyName', 'transportName', 'criticalityName', 'formatName']);
}

/**
 * Given arrays of physical flows, logical flows and physical specs, this
 * function will return a new array where each object represents a triple of
 * `
 * { physical, logical, specification }
 * `
 * Order is the same as the given list of physical flows.
 * The physical flow and specification objects are enriched with display names.
 *
 * @param physicals
 * @param logicals
 * @param specs
 * @param displayNameService
 * @return Array<{ physical, logical, specification }>
 */
export function combinePhysicalWithLogical(physicals = [],
                                    logicals = [] ,
                                    specs = [],
                                    displayNameService) {
    const logicalsById = _.keyBy(logicals, 'id');
    const specsById = _.keyBy(specs, 'id');

    return _.map(physicals, p => {
        const logical = logicalsById[p.logicalFlowId];
        const physical = enrichPhysicalFlow(p, displayNameService);
        const specification = enrichSpecification(specsById[p.specificationId], displayNameService);
        return {
            physical,
            logical,
            specification
        };
    });
}
