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
import _ from 'lodash';
import {mkOverrides} from '../../perspective/perpective-utilities';


const initialState = {

};


function controller($q,
                    $stateParams,
                    perspectiveDefinitionStore,
                    perspectiveRatingStore,
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

    perspectiveDefinitionStore
        .findAll()
        .then(ps => vm.perspectiveDefinition = _.find(ps, { id: $stateParams.perspective || 1 }))
        .then(p => perspectiveRatingStore.findForEntity(entityReference))
        .then(rs => {
            vm.perspectiveRatings = rs;
            vm.perspectiveOverrides = mkOverrides(rs);
        });

    measurableStore
        .findMeasurablesBySelector(idSelector)
        .then(measurables => vm.measurables = measurables);

    measurableRatingStore
        .findByAppSelector(idSelector)
        .then(ratings => vm.measurableRatings = ratings);

    vm.save = (values = []) => {
        const p = vm.perspectiveDefinition;
        return perspectiveRatingStore
            .updateForEntity(p.categoryX, p.categoryY, entityReference, values)
            .then(() => perspectiveRatingStore.findForEntity(p.categoryX, p.categoryY, entityReference))
            .then(rs => vm.perspectiveRatings = rs);

    };

}


controller.$inject = [
    '$q',
    '$stateParams',
    'PerspectiveDefinitionStore',
    'PerspectiveRatingStore',
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
