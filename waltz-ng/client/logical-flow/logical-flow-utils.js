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
import {entity} from "../common/services/enums/entity";
import _ from "lodash";


export const INBOUND = "INBOUND";
export const OUTBOUND = "OUTBOUND";
export const NEITHER = "NEITHER";

export const untaggedFlowsTag = {
    name: "<i>Untagged Flows</i>",
    id: -1
};


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
 * Add untaggedFlowsTag to allTags if not already added
 * @param allTags
 * @returns {*[]}
 */
export function maybeAddUntaggedFlowsTag(allTags = []) {
    if (!_.isEmpty(allTags) && !_.includes(_.map(allTags, "id"), untaggedFlowsTag.id)) {
        allTags.push(untaggedFlowsTag);
    }

    return allTags;
}


/**
 * Saves excluded tags ids under user preferences
 * @param allTags
 * @param selectedTags
 * @param preferenceKey either app-level or group-level view key
 * @param serviceBroker
 */
export function saveTagFilterPreferences(allTags = [],
                                         selectedTags = [],
                                         preferenceKey = "",
                                         serviceBroker) {
    const allTagIds = _.map(allTags, "id");
    const selectedTagIds = _.map(selectedTags, "id");
    const excludedTagIds = _.difference(allTagIds, selectedTagIds);

    serviceBroker
        .execute(
            CORE_API.UserPreferenceStore.saveForUser,
            [{key: preferenceKey, value: _.join(excludedTagIds, ";")}]);
}


/**
 * Filters excluded tags from allTags based on stored user preferences
 * @param allTags
 * @param preferenceKey
 * @param serviceBroker
 */
export function getSelectedTagsFromPreferences(allTags = [],
                                               preferenceKey,
                                               serviceBroker) {
    return serviceBroker
        .loadAppData(CORE_API.UserPreferenceStore.findAllForUser)
        .then(r => {
            const preference = _.find(r.data, p => p.key === preferenceKey);
            const excludedTagIdsStr = _.get(preference, ["value"], "");

            if (!_.isEmpty(excludedTagIdsStr)) {
                const excludedTagIds = _.map(
                    _.split(excludedTagIdsStr, ";"),
                    id => Number(id));

                return _.filter(
                    allTags,
                    t => !_.includes(excludedTagIds, t.id));
            }
            return allTags;
        });
}


/**
 * Filter utils
 */
export const filterUtils = {
    defaultOptions: {
        typeIds: ["ALL"], // [dataTypeId...]
        selectedTags: [] // [{name, id, tagUsages}...]
    },
    buildDecoratorFilter: (typeIds = ["ALL"]) => {
        const datatypeIds = _.map(typeIds, id => (id === "ALL") ? "ALL" : Number(id));
        return d => {
            const isDataType = d.decoratorEntity.kind === entity.DATA_TYPE.key;
            const matchesDataType = _.includes(datatypeIds, "ALL")
                                    || _.includes(datatypeIds, d.decoratorEntity.id);
            return isDataType && matchesDataType;
        };
    },
    mkTypeFilterFn: (decorators = []) => {
        const flowIds = _.chain(decorators)
            .map("dataFlowId")
            .uniq()
            .value();
        return f => _.includes(flowIds, f.id);
    },
    mkTagFilterFn: (selectedTags = [],
                    allTags = [],
                    allFlows = []) => {
        const shouldFilter = !_.isEmpty(selectedTags)
            && !_.isEmpty(allTags)
            && selectedTags.length !== allTags.length;

        if (shouldFilter) {
            const includeUntaggedFlows = _.includes(_.map(selectedTags, "id"), untaggedFlowsTag.id);
            const allFlowIds = _.map(allFlows, "id");
            const allTaggedFlowIds = _.chain(allTags)
                .flatMap(t => _.map(t.tagUsages, "entityReference.id"))
                .uniq()
                .value();
            const allUntaggedFlowIds = _.difference(allFlowIds, allTaggedFlowIds);
            const selectedTagFlowIds = _.chain(selectedTags)
                .flatMap(t => _.map(t.tagUsages, "entityReference.id"))
                .uniq()
                .value();

            return f => _.includes(selectedTagFlowIds, f.id)
                        || (includeUntaggedFlows && _.includes(allUntaggedFlowIds, f.id));
        }

        return f => true;
    }
};