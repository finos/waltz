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