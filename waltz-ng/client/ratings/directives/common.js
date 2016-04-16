
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

import d3 from "d3";
import _ from "lodash";
import {noop, perhaps} from "../../common";

export function calculateHighestRatingCount(groups) {
    return _.chain(groups)
        .map('summaries')
        .flatten()
        .map(s => _.values(s))
        .flatten()
        .union([0])  // required since _.max([]) will give -Infinity
        .max()
        .value();
}


export const defaultDimensions = {
    margin: { top: 8, left: 10, right: 10, bottom: 4 },
    label: { width: 160, height: 12},
    ratingCell: { width: 40, height: 18, padding: 6 },
    viz: {}
};


export function setupCellScale(labelWidth, vizWidth, measurables) {

    const cellsStart = labelWidth;
    const cellsEnd = vizWidth;

    const domain = _.map(measurables, m => m.id || m.code);

    return d3.scale
        .ordinal()
        .domain(domain)
        .rangeBands([cellsStart, cellsEnd], 0.2);
}

export function setupSummaryBarScales(dimensions, cellScale, highestRatingCount) {
    return {
        x: d3.scale
            .linear()
            .domain([0, highestRatingCount])
            .range([0, cellScale.rangeBand()]),
        y: d3.scale
            .ordinal()
            .domain(['G', 'A', 'R', 'Z'])
            .rangeBands([0, dimensions.ratingCell.height], 0.2)
    };
}

export const noopTweaker = { enter: noop, update: noop };

/**
 * transposes and counts the data so a ratings grid like:
 *
 *   R, A, G   --->  subject rows
 *   G, A, G
 *      ^
 *      |
 *   measurable columns
 *
 * gives
 *
 *   {R:1, G:1} , { A: 2 }, { G : 2 }
 *
 * notice each element of the output array describes each input 'column'
 * @param rawData
 * @returns {Array}
 */
export function calculateGroupSummary(rawData) {

    const transposed = d3.transpose(_.map(rawData, 'ratings'));
    return _.map(transposed, measurableColumn => {
        const counts = _.countBy(measurableColumn, c => c.current);
        return { ...counts, measurable: measurableColumn[0].measurable };
    });
}


export function determineChanges(group) {

    const hasChanged = ({ current, original }) => current !== original;

    const enrichRating = (r, subject) => {
        const measurable = _.find(group.measurables, {code: r.measurable});

        return {
            original: r.original,
            current: r.current,
            measurable,
            subject
        };
    };

    const prepareSubject = ({ratings, subject}) => _.chain(ratings)
        .filter(hasChanged)
        .map(r => enrichRating(r, subject))
        .value();

    return _.chain(group.raw)
        .map(prepareSubject)
        .flatten()
        .value();
}


export function mkAppRatingsGroup(appRef, measurables, capabilities, ratings) {

    const bySubjectThenMeasurable = d3.nest()
        .key(r => r.capability.id)
        .key(r => r.measurable.code)
        .map(ratings);


    const raw = _.chain(capabilities)
        .map(s => ({
            ratings: _.map(
                measurables,
                m => {
                    const ragRating = perhaps(() => bySubjectThenMeasurable[s.id][m.code][0].ragRating, 'Z');
                    return { original: ragRating, current: ragRating, measurable: m.code || m.id };
                }),
            subject: s
        }))
        .sortBy('subject.name')
        .value();

    return {
        groupRef: appRef,
        measurables,
        capabilities,
        raw: raw,
        summaries: calculateGroupSummary(raw),
        collapsed: false
    };
}