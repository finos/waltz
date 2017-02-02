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


const initialState = {
    activeRating: 'X',
    existingOverrides: {
    },
    pendingOverrides: {
    }

};


function updateOverrides(overrides = {}, d, rating) {
    const x = d.col.measurable.id;
    const y = d.row.measurable.id;
    const key = `${x}_${y}`;

    if (_.get(overrides, `${key}.rating`) !== rating ) {
        const override = {
            [key] : {
                measurableX: x,
                measurableY: y,
                rating
            }
        };

        return Object.assign(
            {},
            overrides,
            override);

    } else {
        return overrides;
    }
}


function mkOverrides(ratings) {
    const reducer = (acc, r) => {
        const key = `${r.measurableX}_${r.measurableY}`;
        acc[key] = r;
        return acc;
    };
    return _.reduce(ratings, reducer, {});
}


function controller($stateParams,
                    $timeout,
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
        .then(p => perspectiveRatingStore.findForEntity(p.categoryX, p.categoryY, entityReference))
        .then(rs => vm.existingOverrides = mkOverrides(rs));

    measurableStore
        .findMeasurablesBySelector(idSelector)
        .then(measurables => vm.measurables = measurables);

    measurableRatingStore
        .findByAppSelector(idSelector)
        .then(ratings => vm.measurableRatings = ratings);

    const overrideCell = (d) => {
        const updated = updateOverrides(vm.pendingOverrides, d, vm.activeRating);
        if (updated != vm.pendingOverrides) {
            vm.pendingOverrides = updated;
            $timeout(() => {}); // kick angular
        }
    };

    vm.handlers = {
        onCellDrag: overrideCell,
        onCellClick: overrideCell,
        onCellDown: overrideCell,
        onCellOver: d => {
            $timeout(() => vm.hovered = d);
        },
        onCellLeave: d => {
            $timeout(() => vm.hovered = null);
        }
    };

    vm.apply = () => {
        vm.existingOverrides = Object.assign({}, vm.existingOverrides, vm.pendingOverrides);
        vm.pendingOverrides = {};
    };

    vm.undo = () => {
        vm.pendingOverrides = {};
    };

    vm.onRatingSelect = r => vm.activeRating = r;
}


controller.$inject = [
    '$stateParams',
    '$timeout',
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
