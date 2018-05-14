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

import _ from "lodash";


export function mkRatingsKeyHandler(ratings,
                             selectHandler = () => {},
                             cancelHandler = () => {}) {
    const ratingsByKey = _.keyBy(ratings, r => r.rating.toUpperCase());
    return (evt) => {
        if (evt.keyCode === 27 /* esc */) {
            cancelHandler();
        } else {
            const rating = ratingsByKey[evt.key.toUpperCase()];
            if (rating && rating.userSelectable) {
                return selectHandler(rating.rating);
            }
        }
    };
}

export const baseRagNames = {
    'R': {
        rating: 'R',
        name: 'Poor',
    },
    'A': {
        rating: 'A',
        name: 'Adequate'
    },
    'G': {
        rating: 'G',
        name: 'Good'
    },
    'Z': {
        rating: 'Z',
        name: 'Unknown'
    },
    'X': {
        rating: 'X',
        name: 'Not Applicable'
    }
};