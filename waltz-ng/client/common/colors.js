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
import {rgb} from "d3-color";
import {scaleOrdinal, schemeCategory20c} from "d3-scale";
import _ from "lodash";


export const amber = rgb("#D9923F");
export const green = rgb("#5BB65D");
export const red = rgb("#DA524B");
export const pink = rgb("#ffcffc");
export const grey = rgb("#939393");
export const lightGrey = rgb("#d1d1d1");
export const blue = rgb("#5271CC");
export const purple= rgb("#B687CC");
export const gold = rgb("#b7b04e");
export const actor = rgb("#d7bbdb");

export const amberBg = rgb("#FCF2D7");
export const greenBg = rgb("#DFF1D2");
export const redBg = rgb("#F2D7D7");
export const greyBg = rgb("#F5F5F5");
export const actorBg = rgb("#ede5ee");


export const ragColorScale = scaleOrdinal()
    .domain(["R", "A", "G", "Z", "X"])
    .range([red, amber, green, grey, lightGrey]);


export const capabilityColorScale = ragColorScale;


export const investmentRatingColorScale = ragColorScale;


export const maturityColorScale = scaleOrdinal()
    .domain([
        "PLANNED",
        "INVEST",
        "HOLD",
        "DISINVEST",
        "UNSUPPORTED",
        "RESTRICTED"
    ])
    .range([
        blue,
        green,
        amber,
        red,
        red,
        red]);


export const authoritativeSourceColorScale = scaleOrdinal()
    .domain(["NON_STRATEGIC", "SECONDARY", "PRIMARY", "NOT_APPLICABLE"])
    .range([red, amber, green, grey]);


export const authoritativeRatingColorScale = scaleOrdinal()
    .domain(["DISCOURAGED", "SECONDARY", "PRIMARY", "NO_OPINION"])
    .range([red, amber, green, grey.darker()]);


export const authoritativeRatingBackgroundColorScale = scaleOrdinal()
    .domain(["DISCOURAGED", "SECONDARY", "PRIMARY", "NO_OPINION"])
    .range([redBg, amberBg, greenBg, greyBg]);


export const environmentColorScale = scaleOrdinal()
    .domain(["DEV", "PREPROD", "PROD", "QA", "UAT", "DR"])
    .range([green, amber, gold, purple, pink, blue]);


export const operatingSystemColorScale = scaleOrdinal()
    .domain(["Windows", "Linux", "AS/400", "OS/390", "AIX", "Solaris"])
    .range([blue, green, rgb("#777"), rgb("#555"), rgb("#473"), amber]);


export const lifecyclePhaseColorScale = scaleOrdinal()
    .domain(["PRODUCTION", "CONCEPTUAL", "DEVELOPMENT", "RETIRED"])
    .range([gold, blue, green, grey]);


export const criticalityColorScale = scaleOrdinal()
    .domain(["LOW", "MEDIUM", "HIGH", "VERY_HIGH", "NONE", "UNKNOWN"])
    .range([green, amber, red, red.darker(), grey, grey.darker()]);


export const flowDirectionColorScale = scaleOrdinal()
    .domain(["Inbound", "Outbound", "Intra", "UNKNOWN"])
    .range([green, amber, blue, grey]);


const variableColorList = [
    {
        color: pink,
        keys: ["UAT"]
    },
    {
        color: red,
        keys: [
            "NO",
            "FAIL",
            "DISINVEST",
            "UNSUPPORTED",
            "RESTRICTED",
            "DISCOURAGED",
            "NON_STRATEGIC",
            "NON_COMPLIANT",
            "R",
            "RED",
            "OVERDUE",
            "LATE",
            "BAD",
            "END_OF_LIFE",
            "NON PAAS"
        ]
    }, {
        color: green,
        keys: [
            "YES",
            "PASS",
            "COMPLETED",
            "SUCCESS",
            "INVEST",
            "SUPPORTED",
            "PRIMARY",
            "COMPLIANT",
            "ENCOURAGED",
            "STRATEGIC",
            "G",
            "DEV",
            "GREEN",
            "GOOD",
            "NOT_END_OF_LIFE",
            "PAAS"
        ]
    }, {
        color: amber,
        keys: [
            "MAYBE",
            "PARTIAL",
            "HOLD",
            "IN_PROGRESS",
            "SECONDARY",
            "STRATEGIC_WITH_ISSUES",
            "PART_COMPLIANT",
            "PARTIALLY_COMPLIANT",
            "A",
            "AMBER",
            "YELLOW",
            "OKAY",
            "PREPROD"
        ]
    }, {
        color: blue,
        keys: [
            "PLANNED",
            "OTHER",
            "CONCEPTUAL",
            "DR",
            "B",
            "NOT_STARTED",
            "BLUE",
            "MEDIUM",
            "VIRTUAL",
            "DR"
        ]
    }, {
        color: blue.darker(),
        keys: ["HIGH"]
    }, {
        color: blue.brighter(),
        keys: ["LOW"]
    }, {
        color: purple,
        keys: ["OTHER", "UAT"]
    }, {
    },{
        color: gold,
        keys: [
            "PROD",
            "PRD"
        ]
    }, {
        color: grey,
        keys: [
            "UNKNOWN",
            "EXEMPT",
            "RETIRED",
            "GREY",
            "GRAY",
            "POSTPONED",
            "N/A",
            "NA",
            "NOT_APPLICABLE",
            "MEH",
            "PHYSICAL"
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

export const randomColorScale = scaleOrdinal(schemeCategory20c);


export const variableScale = (x = "") => {
    const key = x.toUpperCase();
    const foundColor = variableScaleMap[key];
    return foundColor || rgb(randomColorScale(x));
};


export function useBlackAsForeground(r, g, b) {
    if (_.isString(r)) {
        const c = rgb(r);
        r = c.r;
        g = c.g;
        b = c.b;
    }
    const brightness = (r * 299 + g * 587 + b * 114) / 1000;

    return brightness > 123;
}


export function mkRatingSchemeColorScale(scheme = {}) {
    const ratings = _.isArray(scheme)
        ? scheme
        : scheme.ratings || [];

    return scaleOrdinal()
        .domain(_.map(ratings, "rating"))
        .range(_.map(ratings, d => rgb(d.color)));
}


export function determineForegroundColor(r, g, b) {
    return useBlackAsForeground(r, g, b)
        ? "#000"
        : "#fff"
}