
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

import { EventTypes } from './events.js';
import { perhaps } from '../../../common';

function controller($scope) {
    const vm = this;

    const onSelect = data => $scope.$applyAsync(() => vm.eventData = data);

    this.eventDispatcher.subscribe(onSelect, EventTypes.ORG_UNIT_RATING_SELECTED);
    this.eventDispatcher.subscribe(onSelect, EventTypes.ORG_UNIT_SELECTED);

    vm.eventTypes = EventTypes;
}

controller.$inject = ['$scope'];


export default [
    () => ({
        restrict: 'E',
        replace: true,
        template: require('./rated-flow-summary-detail.html'),
        scope: {},
        controller,
        controllerAs: 'ctrl',
        bindToController: {
            eventDispatcher: '='
        }
    })
];
