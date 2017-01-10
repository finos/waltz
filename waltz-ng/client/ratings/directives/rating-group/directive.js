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
import angular from 'angular';

import { init, draw } from './diagram';


function link(scope, elem) {
    scope.vizElem = elem[0].querySelector('.viz');
    scope.svg = init(scope.vizElem);
}


function controller(scope, $window) {

    const debouncedRender = _.debounce(() => {
        if (!( scope.vizElem
            && scope.data
            && scope.tweakers)) return;

        const { tweakers } = scope;

        const highestRatingCount = scope.highestRatingCount || scope.data.raw.length;

        draw(
            scope.data,
            scope.vizElem.offsetWidth,
            scope.svg,
            tweakers,
            highestRatingCount);

    }, 100);


    angular.element($window).on('resize', () => debouncedRender());
    scope.$watch('maxGroupRatings', () => debouncedRender());
    scope.$watch('data', () => debouncedRender(), true);
    scope.$watch('highestRatingCount', () => debouncedRender());
}


controller.$inject = ['$scope', '$window'];

export default () => ({
    restrict: 'E',
    replace: true,
    scope: {
        data: '=',
        tweakers: '=',
        highestRatingCount: '='
    },
    template: '<div><div class="viz"></div></div>',
    link,
    controller
});
