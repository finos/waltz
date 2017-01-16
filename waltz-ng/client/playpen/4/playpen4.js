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
import {mkPerspective} from '../../perspective/perpective-utilities';
import {nest} from 'd3-collection'


const initialState = {

};


function controller($q,
                    $stateParams,
                    applicationStore,
                    measurableStore,
                    measurableRatingStore) {

    const vm = Object.assign(this, initialState);

    const entityReference = {
        id: $stateParams.id,
        kind: 'APPLICATION'
    };

    const idSelector = {
        entityReference,
        scope: 'EXACT'
    };

    const perspectiveDefinition = {
        id: 1,
        name: 'Function - Process',
        kindA: $stateParams.kindA || 'PROCESS',
        kindB: $stateParams.kindB || 'CAPABILITY',
        description: 'blah blah'
    };

    const perspectiveRatings = [
        {
            measurableA: 161,
            measurableB: 434,
            rating: 'R'
        }, {
            measurableA: 128,
            measurableB: 428,
            rating: 'G'
        }, {
            measurableA: 161,
            measurableB: 428,
            rating: 'Z'
        }, {
            measurableA: 163,
            measurableB: 432,
            rating: 'X'
        }, {
            measurableA: 145,
            measurableB: 446,
            rating: 'X'
        }, {
            measurableA: 149,
            measurableB: 428,
            rating: 'A'
        }
    ];

    const measurablesPromise = measurableStore
        .findMeasurablesBySelector(idSelector)
        .then(measurables => vm.measurables = measurables);

    const ratingsPromise = measurableRatingStore
        .findByAppSelector(idSelector)
        .then(ratings => vm.measurableRatings = ratings);

    $q.all([measurablesPromise, ratingsPromise])
        .then(() => vm.perspective = mkPerspective(
            perspectiveDefinition,
            vm.measurables,
            vm.measurableRatings,
            perspectiveRatings));
}


controller.$inject = [
    '$q',
    '$stateParams',
    'ApplicationStore',
    'MeasurableStore',
    'MeasurableRatingStore'
];


const view = {
    template: require('./playpen4.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;
