
/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
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
