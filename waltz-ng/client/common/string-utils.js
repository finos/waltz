/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
export function truncateMiddle(origStr = '',
                         maxLength = 16,
                         separator = ' ... ') {
    if (origStr.length <= maxLength) {
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
export function stringToBoolean(str = ''){
    switch(str.toLowerCase().trim()){
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
    let domain;
    //find & remove protocol (http, ftp, etc.) and get domain
    if (url.indexOf("://") > -1) {
        domain = url.split('/')[2];
    } else if(url.indexOf("mailto:") > -1) {
        domain = url.split('mailto:')[1];
    }
    else {
        domain = url.split('/')[0];
    }

    //find & remove port number
    domain = domain.split(':')[0];

    return domain;
}


/**
 * https://developer.mozilla.org/en/docs/Web/JavaScript/Guide/Regular_Expressions#Using_Special_Characters
 */
export function escapeRegexCharacters(str) {
    return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
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
            if (num >= si[i].value) {
                return (num / si[i].value)
                        .toFixed(digits)
                        .replace(/\.?0+$/, "") + si[i].symbol;
            }
        }
    } else {
        return num
            .toFixed(digits)
            .replace(/\.?0+$/, "");
    }
}

