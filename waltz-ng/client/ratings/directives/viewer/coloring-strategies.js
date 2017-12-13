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

import {perhaps} from "../../../common";


function drill(appId, capId, ratingMap) {
    if (! ratingMap) { return []; }
    return perhaps(() => ratingMap[appId][capId], []);

}

function drillForRags(appId, capId, ratingMap) {

    return perhaps(() => {
        const ratings = drill(appId, capId, ratingMap);
        const rags =  _.chain(ratings)
            .values()
            .map(xs => xs[0])
            .compact()
            .map('ragRating')
            .uniq()
            .value();
        return rags;
    }, []);

}

function selectWorstFn(appId, capId, ratingMap) {
    const rags = drillForRags(appId, capId, ratingMap);

    if (_.includes(rags, 'R')) return 'R';
    if (_.includes(rags, 'A')) return 'A';
    if (_.includes(rags, 'G')) return 'G';
    return '';
}


function selectBestFn(appId, capId, ratingMap) {
    const rags = drillForRags(appId, capId, ratingMap);

    if (_.includes(rags, 'G')) return 'G';
    if (_.includes(rags, 'A')) return 'A';
    if (_.includes(rags, 'R')) return 'R';
    return '';
}


/**
 * Constructs an anonymous function which will drill into a ratingMap
 * (where a rating map is [appId -> capId -> measure -> [rating]])
 * to retrieve a rating (or '' if not found)
 *
 * @param measureCode
 * @returns {Function}
 */
function selectByMeasureFn(measureCode) {

    return (appId, capId, ratingMap) => {
        return perhaps(() => drill(appId, capId, ratingMap)[measureCode][0].ragRating, '');
    };
}


// ---


export function mkSelectByMeasure(measure) {
    return {
        name: measure.name,
        description: `Focus on [${measure.description}] ratings`,
        fn: selectByMeasureFn(measure.code)
    }
}


export const selectBest = {
    name: 'Best',
    description: 'Colour by the \'best\' (G -> A -> R) of the ratings across all measures',
    fn: selectBestFn
};


export const selectWorst = {
    name: 'Worst',
    description: 'Colour by the \'worst\' (R -> A -> G) of the ratings across all measures',
    fn: selectWorstFn
};