/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";
import {isEmpty} from "../../common";


const BINDINGS = {
    ratings: '=',
    entities: '=',
    visible: '='
};

function filterRatings(ratings,
                       entities = []) {
    return isEmpty(entities)
        ? ratings
        : _.filter(ratings, r => _.includes(entities, r.entityKind));
}


function controller($scope) {

    const vm = this;
    vm.filteredRatings = [];

    $scope.$watchGroup(
        ['ctrl.ratings', 'ctrl.entities'],
        ([ratings, entities = []]) => {
            if(!ratings) return;
            vm.filteredRatings = filterRatings(ratings, entities);
        }
    );

}

controller.$inject = ['$scope'];


export default () => ({
    replace: true,
    restrict: 'E',
    scope: {},
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller,
    template: require('./source-data-overlay.html')
});