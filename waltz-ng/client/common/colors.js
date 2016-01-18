
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

import d3 from 'd3';

const amber = d3.rgb('#ff7f0e');
const green = d3.rgb('#2ca02c');
const red = d3.rgb('#d62728');
const grey = d3.rgb('#999');
const blue = d3.rgb('#28a1b6');

export const capabilityColorScale = d3.scale.ordinal()
    .domain(['R', 'A', 'G', 'Z'])
    .range([red, amber, green, grey]);


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
    .range([blue, green, amber, red]);


export const flowDirectionColorScale = d3.scale.ordinal()
    .domain(['INBOUND', 'OUTBOUND', 'INTRA', 'UNKNOWN'])
    .range([green, amber, blue, grey]);


const underlyingVariableScale = d3.scale.category20();

export const variableScale = (x) => d3.rgb(underlyingVariableScale(x));
