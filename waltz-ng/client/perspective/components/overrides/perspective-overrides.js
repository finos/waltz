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
import {initialiseData} from "../../../common";
import {baseRagNames} from "../../../ratings/rating-utils";
import {nest} from "d3-collection";

/**
 * @name waltz-perspective-overrides
 *
 * @description
 * This component ...
 */


const bindings = {
    baseMeasurable: '<',
    baseRating: '<',
    ragNames: '<',
    overrides: '<',
    entityReference: '<',
    perspectiveDefinitions: '<'
};


const initialState = {

    ragNames: baseRagNames,
    overrides: [],
    perspectiveDefinitions: []
};


const template = require('./perspective-overrides.html');


// => [ { key: rating, values: [ { key: categoryId, values: [ measurables... ] } ] } ]
const nester = nest()
    .key(d => d.rating)
    .key(d => d.measurable.categoryId)
    .rollup(xs => _.map(xs, 'measurable'));


function mkNest(overrides = [], baseRating) {
    return nester.entries(overrides);
}


function controller() {
    const vm = this;

    vm.$onInit = () => initialiseData(vm, initialState);

    vm.$onChanges = (c) => {
        vm.overrideNest = mkNest(vm.overrides, vm.baseRating);
    };

    vm.calcPopoverText = (rating, override) => {
        if (! rating || ! override || ! vm.baseMeasurable) return '';

        return rating === 'X'
                ? `${vm.baseMeasurable.name} is not applicable when combined with ${override.name} `
                : `When ${vm.baseMeasurable.name} combines with ${override.name} the rating is ${vm.ragNames[rating].name}`;
    };
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default component;