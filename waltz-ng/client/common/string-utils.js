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

/**
 * Truncate a string
 *
 * i.e.
 *
 * truncate('abcdefghijkl', 7, '...') => 'abcd...'
 *
 * @param origStr
 * @param maxLength
 * @param end
 * @returns {*}
 */
export function truncate(origStr = "",
                         maxLength = 16,
                         end = "...") {
    if (! origStr) {
        return "";
    }
    if (origStr.length <= maxLength) {
        return origStr;
    }

    const endLength = end.length;
    const charsToShow = maxLength - endLength;

    const truncated = origStr.substr(0, charsToShow);

    return truncated + end;
}


/**
 * Truncates via cutting out middle of string and replacing with separator
 *
 * i.e.
 *
 * truncateMiddle('abcdefghijkl', 7, '...') => 'ab...kl'
 *
 * @param origStr
 * @param maxLength
 * @param separator
 * @returns {*}
 */
export function truncateMiddle(origStr = "",
                         maxLength = 16,
                         separator = " ... ") {
    if (_.size(origStr) <= maxLength) {
        return origStr;
    }

    const sepLength = separator.length;
    const charsToShow = maxLength - sepLength;

    const preLength = Math.ceil(charsToShow/2);
    const postLength = Math.floor(charsToShow/2);

    const pre = origStr.substr(0, preLength);
    const post = origStr.substr(origStr.length - postLength)

    return pre + separator + post;
}


/**
 * Attempts to convert a string to a boolean using some common terms
 * (case insensitive)
 *
 * - true | false
 * - yes | no
 * - 1 | 0
 *
 * If no matches from above, then reverts to `Boolean(str)`
 *
 * @param str
 * @returns {boolean}
 */
export function stringToBoolean(str){
    switch(_.toLower(str).trim()){
        case "true":
        case "yes":
        case "1":
            return true;
        case "false":
        case "no":
        case "0":
        case null:
        case undefined:
            return false;
        default:
            return Boolean(str);
    }
}



/**
 * Given a url, turns it to a domain name i.e. www.test.com/blah becomes www.test.com
 * if a mail link is supplied, i.e. mailto:mail@somewhere.com, this becomes mail@somehwere.com
 * @param url
 * @returns {*}
 */
export function toDomain(url) {
    if (_.isEmpty(url)) {
        return "";
    }

    let domain;
    //find & remove protocol (http, ftp, etc.) and get domain
    if (url.indexOf("://") > -1) {
        domain = url.split("/")[2];
    } else if(url.indexOf("mailto:") > -1) {
        domain = url.split("mailto:")[1];
    }
    else {
        domain = url.split("/")[0];
    }

    //find & remove port number
    domain = domain.split(":")[0];

    return domain;
}


/**
 * https://developer.mozilla.org/en/docs/Web/JavaScript/Guide/Regular_Expressions#Using_Special_Characters
 */
export function escapeRegexCharacters(str) {
    return str.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}


/**
 * Takes a number and limits it to the given number
 * of digits.
 * Examples:
 *   numberFormatter(500_000, 0) :: 500k
 *   numberFormatter(5_000_000, 0) :: 5M
 *   numberFormatter(5_000_000_000, 0) :: 5B
 * @param num
 * @param digits
 * @param simplify
 * @returns {*}
 */
export function numberFormatter(num, digits = 0, simplify = true) {
    const si = [
        { value: 1E12, symbol: "T" },
        { value: 1E9,  symbol: "B" },
        { value: 1E6,  symbol: "M" },
        { value: 1E3,  symbol: "k" }
    ];

    if (simplify) {
        for (let i = 0; i < si.length; i++) {
            if (Math.abs(num) < 0.5) {
                return 0;
            }
            if (Math.abs(num) >= si[i].value) {
                return (Number(num) / si[i].value)
                    .toFixed(digits)
                    .replace(/\.?0+$/, "") + si[i].symbol;
            }
        }
        return num;
    } else {
        return num
            .toFixed(digits)
            .replace(/\.?0+$/, "");
    }
}


/**
 * Given a numerator and denominator returns a percentage value (no percent sign)
 * or '-' if the denominator is zero.  The precision is controlled by `fixedPlaces`.2
 * @param numerator
 * @param denominator
 * @param fixedPlaces, defaults to 1
 * @returns {string}
 */
export function toPercentage(numerator = 0, denominator = 0, fixedPlaces = 1) {
    return denominator === 0
        ? "-"
        : Number(((numerator / denominator) * 100).toFixed(fixedPlaces)).toString();
}


/**
 * Formats the given object into a JSON string representation
 * @param d - object to format
 */
export function fmt(d){
    return JSON.stringify(d, null, 2);
}


/**
 * Formats the given string into upper case with all spaces replaces with underscores
 * @param d - string to format
 */
export function toUpperSnakeCase(d){
    return _.toUpper(_.snakeCase(d));
}