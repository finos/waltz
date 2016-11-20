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

import {rgb, schemeCategory20c, scaleOrdinal} from "d3";
import _ from "lodash";


export const amber = rgb('#D9923F');
export const green = rgb('#5BB65D');
export const red = rgb('#DA524B');
export const grey = rgb('#939393');
export const blue = rgb('#5271CC');
export const actor = rgb('#d7bbdb');

export const amberBg = rgb('#FCF2D7');
export const greenBg = rgb('#DFF1D2');
export const redBg = rgb('#F2D7D7');
export const greyBg = rgb('#F5F5F5');
export const actorBg = rgb('#ede5ee');


export const ragColorScale = scaleOrdinal()
    .domain(['R', 'A', 'G', 'Z'])
    .range([red, amber, green, grey]);


export const capabilityColorScale = ragColorScale;


export const investmentRatingColorScale = ragColorScale;


export const maturityColorScale = scaleOrdinal()
    .domain([
        'PLANNED',
        'INVEST',
        'HOLD',
        'DISINVEST',
        'UNSUPPORTED',
        'RESTRICTED'
    ])
    .range([
        blue,
        green,
        amber,
        red,
        red,
        red]);


export const authoritativeSourceColorScale = scaleOrdinal()
    .domain(['NON_STRATEGIC', 'SECONDARY', 'PRIMARY', 'NOT_APPLICABLE'])
    .range([red, amber, green, grey]);


export const authoritativeRatingColorScale = scaleOrdinal()
    .domain(['DISCOURAGED', 'SECONDARY', 'PRIMARY', 'NO_OPINION'])
    .range([red, amber, green, grey.darker()]);


export const authoritativeRatingBackgroundColorScale = scaleOrdinal()
    .domain(['DISCOURAGED', 'SECONDARY', 'PRIMARY', 'NO_OPINION'])
    .range([redBg, amberBg, greenBg, greyBg]);


export const environmentColorScale = scaleOrdinal()
    .domain(['DEV', 'PREPROD', 'PROD', 'QA', 'UAT'])
    .range([green, amber, blue, grey, red]);


export const operatingSystemColorScale = scaleOrdinal()
    .domain(['Windows', 'Linux', 'AS/400', 'OS/390', 'AIX', 'Solaris'])
    .range([blue, green, rgb('#777'), rgb('#555'), rgb('#473'), amber]);


export const lifecyclePhaseColorScale = scaleOrdinal()
    .domain(['PRODUCTION', 'CONCEPTUAL', 'DEVELOPMENT', 'RETIRED'])
    .range([blue, amber, green, grey]);


export const criticalityColorScale = scaleOrdinal()
    .domain(['LOW', 'MEDIUM', 'HIGH', 'VERY_HIGH', 'NONE', 'UNKNOWN'])
    .range([green, amber, red, red.darker(), grey, grey.darker()]);


export const flowDirectionColorScale = scaleOrdinal()
    .domain(['Inbound', 'Outbound', 'Intra', 'UNKNOWN'])
    .range([green, amber, blue, grey]);




const variableColorList = [
    {
        color: red,
        keys: [
            'NO',
            'FAIL',
            'DISINVEST',
            'UNSUPPORTED',
            'RESTRICTED',
            'DISCOURAGED',
            'NON_STRATEGIC',
            'NON_COMPLIANT',
            'R',
            'RED',
            'OVERDUE',
            'LATE',
            'BAD',
            'END_OF_LIFE'
        ]
    }, {
        color: green,
        keys: [
            'YES',
            'PASS',
            'COMPLETED',
            'SUCCESS',
            'INVEST',
            'SUPPORTED',
            'PRIMARY',
            'COMPLIANT',
            'ENCOURAGED',
            'STRATEGIC',
            'G',
            'GREEN',
            'GOOD',
            'NOT_END_OF_LIFE'
        ]
    }, {
        color: amber,
        keys: [
            'MAYBE',
            'PARTIAL',
            'HOLD',
            'IN_PROGRESS',
            'SECONDARY',
            'STRATEGIC_WITH_ISSUES',
            'PART_COMPLIANT',
            'PARTIALLY_COMPLIANT',
            'A',
            'AMBER',
            'YELLOW',
            'OKAY'
        ]
    }, {
        color: blue,
        keys: [
            'PLANNED',
            'OTHER',
            'CONCEPTUAL',
            'B',
            'NOT_STARTED',
            'BLUE'
        ]
    }, {
        color: grey,
        keys: [
            'UNKNOWN',
            'EXEMPT',
            'RETIRED',
            'GREY',
            'GRAY',
            'POSTPONED',
            'N/A',
            'NA',
            'NOT_APPLICABLE',
            'MEH'
        ]
    }
];

const variableScaleMap = _.reduce(
    variableColorList,
    (acc, colorSet) => {
        _.each(colorSet.keys, k => acc[k] = colorSet.color);
        return acc;
    },
    {});

const randomColorScale = scaleOrdinal(schemeCategory20c);


export const variableScale = (x) => {
    const key = x.toUpperCase();
    const foundColor = variableScaleMap[key];
    return foundColor || rgb(randomColorScale(x));
};


