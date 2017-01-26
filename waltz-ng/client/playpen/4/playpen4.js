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
import {select, event} from 'd3-selection'


const initialState = {
    activeRating: 'X',
    existingOverrides: {
    },
    pendingOverrides: {
    }

};


function updateOverrides(overrides = {}, d, rating) {
    const key = `${d.col.measurable.id}_${d.row.measurable.id}`;

    if (_.get(overrides, `${key}.rating`) !== rating ) {
        const override = {
            [key] : {
                measurableA: d.col.measurable.id,
                measurableB: d.row.measurable.id,
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


function controller($stateParams,
                    $timeout,
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

    vm.perspectiveDefinition = {
        id: 1,
        name: 'Function - Process',
        kindA: $stateParams.kindA || 'PROCESS',
        kindB: $stateParams.kindB || 'CAPABILITY',
        description: 'blah blah'
    };

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
