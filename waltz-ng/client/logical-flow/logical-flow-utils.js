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

import {checkIsEntityRef, checkIsLogicalFlow} from "../common/checks";
import {sameRef} from "../common/entity-utils";
import {CORE_API} from "../common/services/core-api-utils";


export const INBOUND = 'INBOUND';
export const OUTBOUND = 'OUTBOUND';
export const NEITHER = 'NEITHER';


/**
 * For a given flow and an anchor point (ref) determines
 * if the flow is inbound to that anchor.
 *
 * @param flow
 * @param ref
 * @returns {*}
 */
export function isLogicalFlowInbound(flow, ref) {
    checkIsLogicalFlow(flow);
    checkIsEntityRef(ref);
    return sameRef(flow.target, ref, { skipChecks: true });
}


/**
 * For a given flow and an anchor point (ref) determines
 * if the flow is outbound from that anchor.
 *
 * @param flow
 * @param ref
 * @returns {*}
 */
export function isLogicalFlowOutbound(flow, ref) {
    checkIsLogicalFlow(flow);
    checkIsEntityRef(ref);
    return sameRef(flow.source, ref, { skipChecks: true });
}


/**
 * For a given flow and an anchor point (ref) determines
 * if the flow in inbound, outbound or neither (doesn't
 * involve) to the anchor.
 *
 * @param flow
 * @param ref
 * @returns {*}
 */
export function categorizeDirection(flow, ref) {
    if (isLogicalFlowInbound(flow, ref)) return INBOUND;
    else if (isLogicalFlowOutbound(flow, ref)) return OUTBOUND;
    else return NEITHER;
}


/**
 * We calculate flow summary stats differently based on the entity kind.
 * This helper method takes a kind and returns a method reference which
 * can be used by the ServiceBroker
 * @param kind
 * @returns {*}
 */
export function determineStatMethod(kind) {
    return kind === 'DATA_TYPE'
        ? CORE_API.DataTypeUsageStore.calculateStats
        : CORE_API.LogicalFlowStore.calculateStats;
}


/**
 * Given a service broker and logical flow returns a json
 * object with `source` and `target` keys containing the
 * relevant entities.  An additional property of `entityReference`
 * is added to each entity for convenience.
 *
 * @param serviceBroker
 * @param logicalFlow
 */
export function resolveSourceAndTarget(serviceBroker, logicalFlow) {
    const load = (node) => {
        const serviceCall = node.kind === 'APPLICATION'
            ? CORE_API.ApplicationStore.getById
            : CORE_API.ActorStore.getById;

        return serviceBroker
            .loadViewData(serviceCall, [ node.id ])
            .then(r => Object.assign({}, r.data, { entityReference: node }));
    };

    const results = {};
    const sourcePromise = load(logicalFlow.source).then(r => results.source = r);
    const targetPromise = load(logicalFlow.source).then(r => results.target = r);
    return sourcePromise.then(() => targetPromise).then(() => results);
}