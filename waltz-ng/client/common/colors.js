
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


export const amber = d3.rgb('#D9923F');
export const green = d3.rgb('#5BB65D');
export const red = d3.rgb('#DA524B');
export const grey = d3.rgb('#939393');
export const blue = d3.rgb('#5271CC');


export const ragColorScale = d3.scale.ordinal()
    .domain(['R', 'A', 'G', 'Z'])
    .range([red, amber, green, grey]);


export const capabilityColorScale = ragColorScale;


export const investmentRatingColorScale = ragColorScale;


export const maturityColorScale = d3.scale.ordinal()
    .domain([
        'PLANNED',
        'INVEST',
        'HOLD',
        'DISINVEST', 'UNSUPPORTED', 'RESTRICTED'
    ])
    .range([
        blue,
        green,
        amber,
        red, red, red]);


export const authoritativeSourceColorScale = d3.scale.ordinal()
    .domain(['NON_STRATEGIC', 'SECONDARY', 'PRIMARY', 'NOT_APPLICABLE'])
    .range([red, amber, green, grey]);


export const authoritativeRatingColorScale = d3.scale.ordinal()
    .domain(['DISCOURAGED', 'SECONDARY', 'PRIMARY', 'NO_OPINION'])
    .range([red, amber, green, grey.darker()]);


export const environmentColorScale = d3.scale.ordinal()
    .domain(['DEV', 'PREPROD', 'PROD', 'QA', 'UAT'])
    .range([green, amber, blue, grey, red]);


export const operatingSystemColorScale = d3.scale.ordinal()
    .domain(['Windows', 'Linux', 'AS/400', 'OS/390', 'AIX', 'Solaris'])
    .range([blue, green, d3.rgb('#777'), d3.rgb('#555'), d3.rgb('#473'), amber]);


export const lifecyclePhaseColorScale = d3.scale.ordinal()
    .domain(['PRODUCTION', 'CONCEPTUAL', 'DEVELOPMENT', 'RETIRED'])
    .range([blue, amber, green, grey]);


export const riskRatingColorScale = d3.scale.ordinal()
    .domain(['LOW', 'MEDIUM', 'HIGH', 'VERY_HIGH'])
    .range([green, amber, red, red.darker()]);


export const flowDirectionColorScale = d3.scale.ordinal()
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

const randomColorScale = d3.scale.category20();


export const variableScale = (x) => {
    var key = x.toUpperCase();
    var foundColor = variableScaleMap[key];
    return foundColor || d3.rgb(randomColorScale(x));
};


