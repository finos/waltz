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

import {nest} from "d3-collection";


const BINDINGS = {
    applications: '=',
    ratings: '=',
    colorStrategy: '=',
    capabilities: '='
};


/**
 * Convert raw ratings into a nested
 * structure similar to:
 *
 * { appId -> cabilityId -> measurable -> [rag] }
 *
 * Note that the final rag will always be a single value, however
 * d3.nest() will always wrap that in an array.
 *
 * @param ratings
 * @returns {*}
 */
function perpareRatingData(ratings) {
    return nest()
        .key(d => d.parent.id)
        .key(d => d.capability.id)
        .key(d => d.measurable.code)
        .object(ratings);
}


function controller($scope) {
    const vm = this;

    $scope.$watch('ctrl.ratings', (ratings => {
        if (!ratings) return;
        vm.ratingMap = perpareRatingData(ratings);
    }));

    vm.determineRating = (appId, capId) => vm.colorStrategy.fn(appId, capId, vm.ratingMap);
}

controller.$inject = ['$scope'];


export default () => ({
    replace: true,
    restrict: 'E',
    scope: {},
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller,
    template: require('./app-rating-table.html')
});
