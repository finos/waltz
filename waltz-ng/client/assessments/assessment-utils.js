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
import { indexRatingSchemes } from "../ratings/rating-utils";
import { refToString } from "../common/entity-utils";
import {CORE_API} from "../common/services/core-api-utils";
import {resolveResponses} from "../common/promise-utils";

/**
 * Creates an enriched assessment definition which adds fields for
 * the possible dropdown values and (if set) the current rating along
 * with the rating item (describing the dropdown option)
 *
 * @param definitions
 * @param schemes
 * @param assessments
 */
export function mkEnrichedAssessmentDefinitions(definitions = [],
                                                schemes = [],
                                                assessments = []) {
    const schemesByIdByRatingId = indexRatingSchemes(schemes);
    const assessmentsByDefinitionId = _.groupBy(assessments, "assessmentDefinitionId");

    return _
        .chain(definitions)
        .map(definition => {
            const scheme = _.get(schemesByIdByRatingId, `[${definition.ratingSchemeId}]`);

            const assessments = _.get(assessmentsByDefinitionId, `[${definition.id}]`, []);

            const ratings = _.map(assessments, d => ({
                rating: d,
                ratingItem: _.get(scheme, `ratingsById[${d.ratingId}]`)
            }));

            const dropdownEntries = scheme
                ? _.map(
                    scheme.ratings,
                    r => Object.assign(
                        {},
                        r,
                        {code: r.id}))
                : [];

            return {
                definition,
                ratings,
                dropdownEntries
            };
        })
        .orderBy(d => d.definition.name)
        .value();
}


/**
 * Given a list of entities, a list of assessment ratings and a desired
 * rating will filter the list of entities accordingly
 *
 * @param entities
 * @param assessmentRatings
 * @param requiredRating  looks like: `{ ratingId, assessmentId }`.  A rating id of -1 means 'not provided'
 * @returns {*}
 */
export function filterByAssessmentRating(entities = [],
                                         assessmentRatings = [],
                                         requiredRating) {
    const assessmentRatingsByRef = _
        .chain(assessmentRatings)
        .filter(d => d.assessmentDefinitionId === requiredRating.assessmentId)
        .keyBy(d => refToString(d.entityReference))
        .value();

    return _
        .chain(entities)
        .filter(entity => {
            const assocRating = assessmentRatingsByRef[refToString(entity)];
            if (requiredRating.ratingId === -1) {
                // desired rating is 'not provided' hence keep only entities with no rating
                return _.isNil(assocRating);
            } else {
                return assocRating && assocRating.ratingId === requiredRating.ratingId;
            }
        })
        .value();
}

/**
 * Loads all assessments for an entity kind and selector, returning the definitions and ratings that for that entity kind.
 * Returns an object as follows
 * {
 *     definitions,
 *     assessmentsByEntityId //<entity id -> assessment def external id -> assessment>
 * }
 * @param $q
 * @param serviceBroker
 * @param kind
 * @param options
 * @param primaryOnly
 * @returns {*}
 */
export function loadAssessmentsBySelector($q, serviceBroker, kind, options, primaryOnly = true) {
    const ratingsPromise = serviceBroker
        .loadViewData(
            CORE_API.AssessmentRatingStore.findByTargetKindForRelatedSelector,
            [kind, options],
            {force: true});

    return loadAssessments($q, serviceBroker, kind, ratingsPromise, primaryOnly);
}


/**
 * Loads all assessments for an entity kind, returning the definitions and ratings that for that entity kind.
 * Returns an object as follows
 * {
 *     definitions,
 *     assessmentsByEntityId //<entity id -> assessment def external id -> assessment>
 * }
 * @param $q
 * @param serviceBroker
 * @param kind
 * @param primaryOnly
 * @returns {*}
 */
export function loadAssessmentsForKind($q, serviceBroker, kind, primaryOnly = true) {
    const ratingsPromise = serviceBroker
        .loadViewData(
            CORE_API.AssessmentRatingStore.findByEntityKind,
            [kind],
            {force: true});

    return loadAssessments($q, serviceBroker, kind, ratingsPromise, primaryOnly);
}


/**
 * loads all assessment for a kind given the rating promise used (by selector or kind)
 * {
 *     definitions,
 *     assessmentsByEntityId //<entity id -> assessment def external id -> assessment>
 * }
 * @param $q
 * @param serviceBroker
 * @param kind
 * @param ratingsPromise
 * @param primaryOnly
 * @returns {*}
 */
function loadAssessments($q, serviceBroker, kind, ratingsPromise, primaryOnly = true) {
    const definitionsPromise = serviceBroker
        .loadViewData(
            CORE_API.AssessmentDefinitionStore.findByKind,
            [kind]);

    const ratingSchemePromise = serviceBroker
        .loadViewData(
            CORE_API.RatingSchemeStore.findAll);

    return $q
        .all([definitionsPromise, ratingsPromise, ratingSchemePromise])
        .then(responses => {
            const [assessmentDefinitions, assessmentRatings, ratingSchemes] = resolveResponses(responses);
            const ratingsByEntityId = _.groupBy(assessmentRatings, "entityReference.id");
            const filteredDefinitions = _.filter(assessmentDefinitions, primaryOnly ? d => d.visibility === "PRIMARY" : true);
            const enrichedByEntityId = _.mapValues(ratingsByEntityId, (v, k) => {
                const enriched = mkEnrichedAssessmentDefinitions(
                    filteredDefinitions,
                    ratingSchemes,
                    v);
                return _.keyBy(enriched, e => e.definition.externalId);
            });


            return {
                definitions: filteredDefinitions,
                assessmentsByEntityId: enrichedByEntityId // assessmentsByEntityId: entity id -> assessment def external id -> assessment
            };
        });
}


export function isFavourite(favouriteDefinitionIds, id) {
    return _.includes(favouriteDefinitionIds, id);
}


export function getIdsFromString(includedFavouritesString) {
    return _.isNil(includedFavouritesString)
        ? []
        : _.chain(includedFavouritesString.value)
            .split(",")
            .map(idString => _.toNumber(idString))
            .value();
}