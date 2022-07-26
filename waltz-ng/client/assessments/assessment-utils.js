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
import { nest } from "d3-collection";
import { grey } from "../common/colors";
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
    const assessmentsByDefinitionId = _.keyBy(assessments, "assessmentDefinitionId");

    return _
        .chain(definitions)
        .map(definition => {
            const scheme = _.get(schemesByIdByRatingId, `[${definition.ratingSchemeId}]`);
            const assessment = _.get(assessmentsByDefinitionId, `[${definition.id}]`, null);
            const ratingSchemeItem = assessment != null
                ? _.get(scheme, `ratingsById[${assessment.ratingId}]`)
                : null;

            const dropdownEntries = _.map(
                scheme.ratings,
                r => Object.assign(
                    {},
                    r,
                    { code: r.id }));

            return {
                definition,
                rating: assessment,
                ratingItem: ratingSchemeItem,
                dropdownEntries };
        })
        .orderBy("name")
        .value();
}


export function mkAssessmentSummaries(definitions = [], schemes = [], ratings = [], total = 0) {
    const indexedRatingSchemes = indexRatingSchemes(schemes);
    const definitionsById = _.keyBy(definitions, d => d.id);

    const nestedRatings = nest()
        .key(d => d.assessmentDefinitionId)
        .key(d => d.ratingId)
        .rollup(xs => xs.length)
        .entries(ratings);

    return _
        .chain(nestedRatings)
        .map(d => {
            const definition = definitionsById[Number(d.key)];
            const assignedTotal = _.sumBy(d.values, v => v.value);
            const values = _
                .chain(d.values)
                .map(v => {
                    const propPath = [definition.ratingSchemeId, "ratingsById", v.key];
                    const rating = _.get(indexedRatingSchemes, propPath);
                    return Object.assign({}, v, { rating, count: v.value });
                })
                .concat([{
                    key: "z",
                    rating: {
                        id: -1,
                        name: "Not Provided",
                        color: grey
                    },
                    count: _.max([0, total - assignedTotal])
                }])
                .filter(d => d.count > 0)
                .value();

            const extension = { definition, values };
            return Object.assign({}, d , extension);
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