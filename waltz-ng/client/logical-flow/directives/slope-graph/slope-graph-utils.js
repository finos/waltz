/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */

import _ from "lodash";


function getUniqueSourceEntities(xs) {
    return _.chain(xs)
        .map('sourceEntity')
        .uniqBy('id')
        .value();
}


function getUniqueTargetEntities(xs) {
    return _.chain(xs)
        .map('targetEntity')
        .uniqBy('id')
        .value();
}


function convertFlows(fs, pred) {
    return _.chain(fs)
        .filter(pred)
        .map(f => ({
            source: f.source.id,
            target: f.target.id,
            type: f.dataType,
            highlighted: false
        }))
        .value();
}


function getColour(rating) {
    const ratingColours = {
        PRIMARY: 'green',
        SECONDARY: 'orange'
    };

    return ratingColours[rating] || 'red';
}


/**
 * We need flows to find the target entity and therefore the target's OU id.
 * @param authSources
 * @param flow
 * @returns {*|string}
 */
function getOutgoingRating(authSources, flow) {
//TODO: fix
    const ouId = 10;

    return _.chain(authSources)
            .filter({dataType: flow.type, parentReference: {id: ouId, kind: 'ORG_UNIT'}})
            .map('rating')
            .map(getColour)
            .head()
            .value() || 'red';
}


function getIncomingRating(authSources, flow) {
    return _.chain(authSources)
            .filter({dataType: flow.type, applicationReference: {id: flow.source}})
            .map('rating')
            .map(getColour)
            .head()
            .value() || 'red';
}
