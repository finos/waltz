
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
import d3 from 'd3';
import angular from 'angular';

import testData from './hier_data';

import { capabilityColorScale } from '../common/colors';


function render(container, data) {
    const reRender = () => render(container, data);

    const layout = d3.layout
        .partition()
        .size([container.attr('width'), container.attr('height')]);

    const nodes = layout(data);

    const barHeightShrinkageRate = 1.6; // higher number results in quickly diminishing size as depth increases

    const calcY = (d) => _.foldl(
        _.range(0, d.depth),
        (acc, depth) => acc + d.dy / Math.pow(barHeightShrinkageRate, depth),
        0);

    const calcHeight = (d) => d.dy / Math.pow(barHeightShrinkageRate, d.depth);

    container.selectAll('.node')
        .data(nodes, d => d.t)
        .enter()
        .append('rect')
        .classed('node', true)
        .attr({
            x: d => d.x,
            y: calcY,
            width: d => d.dx,
            height: calcHeight,

            stroke: '#aaa',
            'stroke-width': d => 0.8 / (d.depth + 1)
        })
        .on('mouseover', (d) => { d.over = true; reRender(); })
        .on('mouseout', (d) => { d.over = false; reRender(); });

    container.selectAll('.node')
        .attr({
            fill: d => capabilityColorScale(d.rating)
                .hsl()
                .brighter(d.over ? 0.3 : 0.5 + d.depth * 0.4)
        });

}


function directiveController($scope) {
    const vm = this;

    const dimensions = {
        width: 200,
        height: 48
    };

    function attemptRender(elem, data) {
        if (! data || ! elem) return;

        const svgElem = angular
            .element(elem)
            .find('svg')[0];

        if (! svgElem) return;

        dimensions.width = elem[0].clientWidth;
        const svg = d3.select(svgElem).attr(dimensions);

        render(svg, data);
    }

    $scope.$watch('elem', (elem) => {
        attemptRender(elem, vm.data);
    });

    $scope.$watch('ctrl.data', (data) => {
        attemptRender($scope.elem, data);
    });
}

directiveController.$inject = ['$scope'];


function controller($state, $window) {
    const vm = this;
    this.data = testData;
}

controller.$inject = [
    '$state', '$window'
];


const playpenView = {
    template: require('./playpen.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default (module) => {

    module.config([
        '$stateProvider',
        ($stateProvider) => {
            $stateProvider
                .state('main.playpen', {
                    url: 'playpen',
                    views: { 'content@': playpenView }
                });
        }
    ]);

    module.directive('partition', () => ({
        restrict: 'E',
        replace: true,
        template: '<div><svg></svg></div>',
        scope: {},
        bindToController: {
            data: '='
        },
        controllerAs: 'ctrl',
        controller: directiveController,
        link: (scope, elem) => scope.elem = elem
    }));
};
