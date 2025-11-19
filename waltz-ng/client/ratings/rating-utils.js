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


export function mkRatingsKeyHandler(
    ratings,
    selectHandler = () => {},
    cancelHandler = () => {})
{
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
    "R": {
        rating: "R",
        name: "Poor",
        position: 30,
        color: "#DA524B"
    },
    "A": {
        rating: "A",
        name: "Adequate",
        position: 20,
        color: "#D9923F"
    },
    "G": {
        rating: "G",
        name: "Good",
        position: 10,
        color: "#5BB65D"
    },
    "Z": {
        rating: "Z",
        name: "Unknown",
        position: 40,
        color: "#939393"
    },
    "X": {
        rating: "X",
        name: "Not Applicable",
        position: 50,
        color: "#D1D1D1"
    }
};


export const investmentRagNames = {
    "C": {
        rating: "C",
        name: "Disinvest - Certified",
        position: 40,
        color: "#1F7FE0"
    },
    "R": {
        rating: "R",
        name: "Disinvest - Non-Certified",
        position: 30,
        color: "#DA524B"
    },
    "A": {
        rating: "A",
        name: "Maintain",
        position: 20,
        color: "#D9923F"
    },
    "G": {
        rating: "G",
        name: "Invest",
        position: 10,
        color: "#5BB65D"
    },
    "Z": {
        rating: "Z",
        name: "Unknown",
        position: 50,
        color: "#939393"
    },
    "X": {
        rating: "X",
        name: "Not Applicable",
        position: 60,
        color: "#D1D1D1"
    }
};


const ragNameSchemes = {
    base: baseRagNames,
    investment: investmentRagNames
};


export function ragToRatingSchemeItem(code = "Z", schemeName = "base") {
    const scheme = ragNameSchemes[schemeName] || baseRagNames;
    return scheme[code] || scheme["Z"];
}


/**
 * Given a flat list of rating schemes returns them indexed by their ids.  Also each scheme has
 * an additional maps giving 'ratingsByCode' and 'ratingsById' .
 * @param schemes
 */
export function indexRatingSchemes(schemes = []) {
    return _.chain(schemes)
        .map(scheme => Object.assign({}, scheme, {
            ratingsByCode: _.keyBy(scheme.ratings, d => d.rating),
            ratingsById: _.keyBy(scheme.ratings, d => d.id),
        }))
        .keyBy(d => d.id)
        .value();
}

export function distinctRatingCodes(schemes = {}) {
    return _.chain(schemes)
        .flatMap((v) => v.ratings)
        .map(d => d.rating)
        .uniq()
        .value();
}


export function mkAuthoritativeRatingSchemeItems(displayNameService) {
    const resolveName = k => displayNameService.lookup("AuthoritativenessRating", k);
    return {
        "DISCOURAGED": {
            rating: "R",
            name: resolveName("DISCOURAGED"),
            position: 30,
            color: "#DA524B"
        },
        "SECONDARY": {
            rating: "A",
            name: resolveName("SECONDARY"),
            position: 20,
            color: "#D9923F"
        },
        "PRIMARY": {
            rating: "G",
            name: resolveName("PRIMARY"),
            position: 10,
            color: "#5BB65D"
        },
        "NO_OPINION": {
            rating: "Z",
            name: resolveName("NO_OPINION"),
            position: 40,
            color: "#939393"
        }
    };
}


export function getDefaultRating(ratings = []) {
    const defaultRating = _
        .chain(ratings)
        .sortBy(["position"])
        .head()
        .value();

    const fallback = () => {
        console.log("Could not determine default rating, using hard-coded [G]", { givenRatings: ratings });
        return "G";
    };

    return _.get(defaultRating, ["rating"], fallback());
}
