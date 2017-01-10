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



const initialState = {

};


function controller($q,
                    applicationStore,
                    measurableStore,
                    measurableRatingStore) {

    const vm = Object.assign(this, initialState);

    const entityReference = {
        id: 10,
        kind: 'ORG_UNIT'
    };

    const idSelector = {
        entityReference,
        scope: 'CHILDREN'
    };

    measurableStore
        .findMeasurablesBySelector(idSelector)
        .then(measurables => vm.measurables = measurables);

    measurableRatingStore
        .statsByAppSelector(idSelector)
        .then(ratings => vm.measurableRatings = ratings);

    vm.loadDetail = (d) => {
        const measurableSelector = {
            entityReference: { id: d.id, kind: 'MEASURABLE' },
            scope: 'CHILDREN'
        };

        const ratingsPromise = measurableRatingStore.findByMeasurableSelector(measurableSelector);
        const appPromise = applicationStore.findBySelector(measurableSelector);
        return $q
            .all([ratingsPromise, appPromise])
            .then(([ratings, applications]) => ({ ratings, applications }));
    };
}


controller.$inject = [
    '$q',
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
