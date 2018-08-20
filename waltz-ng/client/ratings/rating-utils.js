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
        position: 30,
        color: '#DA524B'
    },
    'A': {
        rating: 'A',
        name: 'Adequate',
        position: 20,
        color: '#D9923F'
    },
    'G': {
        rating: 'G',
        name: 'Good',
        position: 10,
        color: '#5BB65D'
    },
    'Z': {
        rating: 'Z',
        name: 'Unknown',
        position: 40,
        color: '#939393'
    },
    'X': {
        rating: 'X',
        name: 'Not Applicable',
        position: 50,
        color: '#D1D1D1'
    }
};


export const investmentRagNames = {
    'R': {
        rating: 'R',
        name: 'Disinvest',
        position: 30,
        color: '#DA524B'
    },
    'A': {
        rating: 'A',
        name: 'Maintain',
        position: 20,
        color: '#D9923F'
    },
    'G': {
        rating: 'G',
        name: 'Invest',
        position: 10,
        color: '#5BB65D'
    },
    'Z': {
        rating: 'Z',
        name: 'Unknown',
        position: 40,
        color: '#939393'
    },
    'X': {
        rating: 'X',
        name: 'Not Applicable',
        position: 50,
        color: '#D1D1D1'
    }
};


const ragNameSchemes = {
    base: baseRagNames,
    investment: investmentRagNames
};


export function ragToRatingSchemeItem(code = 'Z', schemeName = 'base') {
    const scheme = ragNameSchemes[schemeName] || baseRagNames;
    return scheme[code] || scheme['Z'];
}


/**
 * Given a flat list of rating schemes returns them indexed by their ids.  Also each scheme has
 * an additional maps giving 'ratingsByCode' and 'ratingsById' .
 * @param schemes
 */
export function indexRatingSchemes(schemes = []) {
    return _.chain(schemes)
        .map(scheme => Object.assign({}, scheme, {
            ratingsByCode: _.keyBy(scheme.ratings, 'rating'),
            ratingsById: _.keyBy(scheme.ratings, 'id'),
        }))
        .keyBy("id")
        .value();
}

export function distinctRatingCodes(schemes = {}) {
    return _.chain(schemes)
        .flatMap((v,k) => v.ratings)
        .map('rating')
        .uniq()
        .value();
}


export function mkAuthoritativeRatingSchemeItems(displayNameService) {
    const resolveName = k => displayNameService.lookup('AuthoritativenessRating', k);
    return {
        'DISCOURAGED': {
            rating: 'R',
            name: resolveName('DISCOURAGED'),
            position: 30,
            color: '#DA524B'
       },
       'SECONDARY': {
            rating: 'A',
            name: resolveName('SECONDARY'),
            position: 20,
            color: '#D9923F'
       },
       'PRIMARY': {
            rating: 'G',
            name: resolveName('PRIMARY'),
            position: 10,
            color: '#5BB65D'
        },
       'NO_OPINION': {
            rating: 'Z',
            name: resolveName('NO_OPINION'),
            position: 40,
            color: '#939393'
        }
    };
}