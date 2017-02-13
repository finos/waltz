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
import _ from "lodash";
import {initialiseData} from "../../../common";
import {toMap} from "../../../common/map-utils";
import {containsAll} from "../../../common/list-utils";


/**
 * @name waltz-associated-perspectives
 *
 * @description
 * This component ...
 */


const bindings = {
    entityRef: '<',
    categoryId: '<',
    measurables: '<',
    perspectiveDefinitions: '<',
    ratings: '<'
};


const initialState = {
    perspectiveDefinitions: [],
    measurables: [],
    ratings: []
};


const template = require('./associated-perspectives.html');


function calcAssociatedPerspectives(categoryId,
                                    measurables = [],
                                    perspectiveDefinitions = [],
                                    ratings = []) {
    const measurableIdToCategoryId = toMap(measurables, m => m.id, m => m.categoryId);
    const usedCategoryIds = _.uniq(_.map(ratings, r => measurableIdToCategoryId[r.measurableId]));

    const possiblePerspectives = _.filter(
        perspectiveDefinitions,
        p => p.categoryX === categoryId || p.categoryY === categoryId);

    return _.map(
        possiblePerspectives,
        p => ({
            definition: p,
            available: containsAll(usedCategoryIds, [p.categoryX, p.categoryY])
        }));
}


function controller() {
    const vm = this;

    vm.$onInit = () => initialiseData(vm, initialState);

    vm.$onChanges = (c) => {
        vm.associatedPerspectives = calcAssociatedPerspectives(vm.categoryId, vm.measurables, vm.perspectiveDefinitions, vm.ratings);
    };

}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default component;
