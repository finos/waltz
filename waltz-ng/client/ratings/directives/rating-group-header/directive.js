

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

import _ from 'lodash';
import angular from 'angular';
import d3 from 'd3';

import { defaultDimensions, setupCellScale } from '../common';


function render(measurables, svg, width) {
    const scale = setupCellScale(defaultDimensions.label.width, width, measurables);

    svg.attr('width', width);

    const titleBar = svg.selectAll('.measure-titles')
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
    const svg = d3.select(vizElem)
        .append('svg')
        .attr({ height: 80 });

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
