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
import template from "./measurable-rating-panel.html";

/**
 * @name waltz-measurable-rating-panel
 *
 * @description
 * This component wraps a <code>measurable-rating-tree</code>
 * and provides a detail view when a node is clicked on.
 *
 * It is intended to be used when viewing a single application
 * and a single 'kind' of measurables.
 */


const bindings = {
    allocations: "<",
    allocationSchemes: "<",
    category: "<",
    entityReference: "<",
    measurables: "<",
    ratings: "<",
    ratingScheme: "<"
};


const initialState = {
    allocations: [],
    allocationSchemes: [],
    measurables: [],
    ratings: [],
    selected: null
};


function controller() {
    const vm = this;

    vm.$onInit = () => initialiseData(vm, initialState);

    vm.$onChanges = () => {
        vm.ratingsByCode = _.keyBy(_.get(vm.ratingScheme, "ratings", []), r => r.rating);
        vm.allocationSchemesById = _.keyBy(vm.allocationSchemes, s => s.id);
    };

    vm.onSelect = (measurable, rating) => {
        const relevantAllocations = _
            .chain(vm.allocations)
            .filter(a => a.measurableId === measurable.id)
            .map(allocation => {
                const scheme = vm.allocationSchemesById[allocation.schemeId];
                return scheme
                    ? Object.assign(allocation, {scheme})
                    : null;
            })
            .compact()
            .sortBy(a => a.scheme.name)
            .value();

        vm.selected = {
            allocations: relevantAllocations,
            measurable,
            rating,
        };
    }
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};



export default {
    component,
    id: "waltzMeasurableRatingPanel"
};
