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
import {rgb} from "d3-color";
import {scaleOrdinal, schemeCategory20c} from "d3-scale";
import _ from "lodash";




export const blackHex = "#000000";
export const greyHex = "#939393";
export const lightGreyHex = "#D1D1D1";
export const blueHex = "#1F7FE0";
export const greenHex = "#5BB65D";
export const yellowHex = "#E0E314";
export const goldHex = "#B7A61F";
export const amberHex = "#D9923F";
export const redHex = "#DA524B";
export const purpleHex = "#B687CC";
export const pinkHex = "#FFCFFC";
export const pinkBgHex = "#ffe9fd";
export const greyBgHex = "#F5F5F5";
export const amberBgHex = "#FCF2D7";
export const yellowBgHex = "#fefff0";
export const greenBgHex = "#DFF1D2";
export const redBgHex = "#F2D7D7";
export const purpleBgHex = "#F7EEFF";
export const blueBgHex = "#E2F0FD";
export const darkAmberHex = "#c24000";


export const black = rgb(blackHex);
export const grey = rgb(greyHex);
export const lightGrey = rgb(lightGreyHex);
export const blue = rgb(blueHex);
export const green = rgb(greenHex);
export const yellow = rgb(yellowHex);
export const gold = rgb(goldHex);
export const amber = rgb(amberHex);
export const red = rgb(redHex);
export const pink = rgb(pinkHex);
export const purple= rgb(purpleHex);
export const actor = rgb("#D7BBDB");

export const amberBg = rgb(amberBgHex);
export const greenBg = rgb(greenBgHex);
export const redBg = rgb(redBgHex);
export const greyBg = rgb(greyBgHex);
export const blueBg = rgb(blueBgHex);
export const pinkBg = rgb(pinkBgHex);
export const actorBg = rgb("#EDE5EE");

export const darkAmber = rgb(darkAmberHex);

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

export function mkColorScaleFromEnumValues(enumValues = []) {
    return scaleOrdinal()
        .domain(_.map(enumValues, d => d.key))
        .range(_.map(enumValues, d => d.iconColor));
}

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

export const attestationStatusColorScale = scaleOrdinal()
    .domain(["ATTESTED", "NEVER_ATTESTED"])
    .range([green, grey]);


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
            "NON PAAS",
            "HIGH RISK",
            "HIGH"
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
            "PAAS",
            "LOW RISK",
            "LOW"
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
            "PREPROD",
            "MEDIUM RISK",
            "MEDIUM"
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
            "VIRTUAL",
            "DR"
        ]
    }, {
        color: purple,
        keys: ["OTHER", "UAT"]
    }, {
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

    return brightness > 150;
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
        ? "rgba(50, 50, 50, 0.9)"
        : "rgba(255, 255, 255, 0.9)";
}


function componentToHex(c) {
    const hex = c.toString(16);
    return hex.length === 1
        ? "0"+hex
        : hex;
}

export function rgbToHex(r,g,b) {
    return `#${componentToHex(r)}${componentToHex(g)}${componentToHex(b)}`
}