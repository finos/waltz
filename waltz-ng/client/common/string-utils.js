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
