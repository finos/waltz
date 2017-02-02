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
import {initialiseData} from '../../../common';


/**
 * @name waltz-perspective-editor
 *
 * @description
 * This component ...
 */


const bindings = {
    perspectiveDefinition: '<',
    measurables: '<',
    measurableRatings: '<',
    perspectiveRatings: '<'
};


const initialState = {
    activeRating: 'A',
    measurables: [],
    measurableRatings: [],
    perspectiveRatings: [],
    existingOverrides: {},
    pendingOverrides: {}
};


const template = require('./perspective-editor.html');


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


function controller($timeout) {
    const vm = initialiseData(this, initialState);
    console.log('perspective-editor - init', vm);


    const overrideCell = (d) => {
        const updated = updateOverrides(vm.pendingOverrides, d, vm.activeRating);
        if (updated != vm.pendingOverrides) {
            vm.pendingOverrides = updated;
            $timeout(() => {}); // kick angular
        }
    };

    vm.$onChanges = c => {
        console.log('c', c, vm)
        if (c.perspectiveRatings) {
            vm.existingOverrides = mkOverrides(vm.perspectiveRatings);
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

    vm.onRatingSelect = r => vm.activeRating = r;

    vm.apply = () => {
        vm.existingOverrides = Object.assign({}, vm.existingOverrides, vm.pendingOverrides);
        vm.pendingOverrides = {};
    };

    vm.undo = () => {
        vm.pendingOverrides = {};
    };
}


controller.$inject = [
    '$timeout'
];


const component = {
    template,
    bindings,
    controller
};


export default component;