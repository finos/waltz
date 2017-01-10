

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
import {select} from 'd3-selection';

import { defaultDimensions, setupCellScale } from '../common';


function render(measurables, svg, width) {
    const scale = setupCellScale(defaultDimensions.label.width, width, measurables);

    svg.attr('width', width);

    const titleBar = svg
        .selectAll('.measure-titles')
        .data([measurables]);

    titleBar.enter()
        .append('g')
        .classed('measure-titles', true);


    const headings = titleBar.selectAll('.measure-heading')
        .data(d => d, d => d.code || d.id);

    headings.enter()
        .append('text')
        .classed('measure-heading', true)

    headings
        .attr('fill', '#444')
        .attr('transform', d => `translate(${scale(d.id || d.code) + scale.rangeBand() * 0.45}, 80) rotate(300)`)
        .text(d => d.name);
}

function init(vizElem) {
    const svg = select(vizElem)
        .append('svg')
        .attr("height", 80);

    return svg;
}


function link(scope, elem) {
    scope.vizElem = elem[0].querySelector('.viz');
    scope.svg = init(scope.vizElem);
}


function controller($scope, $window) {
    const debouncedRender = _.debounce(() => {
        if (!($scope.measurables)) return;

        render($scope.measurables, $scope.svg, $scope.vizElem.offsetWidth);
    });

    angular.element($window).on('resize', () => debouncedRender());
    $scope.$watch('measurables', () => debouncedRender());
}

controller.$inject = ['$scope', '$window'];


export default () => ({
    restrict: 'E',
    replace: true,
    scope: {
        measurables: '=',
        tweakers: '='
    },
    template: '<div><div class="viz"></div></div>',
    controller,
    link
});
