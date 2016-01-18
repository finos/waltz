
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


function directiveController($scope, $window) {
    const vm = this;


    const render = (config) => {

        const { svg, buckets, dimensions } = config;

        svg.attr({
            width: dimensions.width,
            height: dimensions.height
        });

        const maxBucketSize = _.chain(buckets)
            .map(b => b.size)
            .max()
            .value();

        const blobScale = d3.scale.linear()
            .domain([0, maxBucketSize])
            .range([0, ( dimensions.height / 1.6) / 2]);

        const xScale = d3.scale.ordinal()
            .domain(_.map(buckets, 'name'))
            .rangeBands([0, dimensions.width], 0.3);

        const xAxis = d3.svg.axis()
            .scale(xScale)
            .tickSize(4);

        svg.append('g')
            .attr('class', 'x axis');

        svg.select('.axis')
            .attr('transform', `translate(0, ${ dimensions.height - dimensions.margin.bottom } )`)
            .call(xAxis);

        svg.selectAll('.bucket')
            .data(buckets)
            .enter()
            .append('circle')
            .classed('bucket', true)
            .on('click', d => {
                if (vm.eventDispatcher) {
                    const filterCmd = {
                        type: 'COST.FILTER',
                        data: { field: 'amount', min: d.min, max: d.max }
                    };
                    vm.eventDispatcher.dispatch(filterCmd);
                }
            })
            .append('title');

        svg.selectAll('.bucket')
            .data(buckets)
            .attr({
                r: d => blobScale(d.size),
                cx: d => xScale(d.name) + xScale.rangeBand() / 2,
                cy: dimensions.height / 2
            });

        svg.selectAll('.bucket title')
            .data(buckets)
            .text(d => `# = ${d.size}`);
    };


    const reallyDraw = (vizElem, buckets) => {
        const config = {
            svg: d3.select(vizElem).select('svg'),
            buckets,
            dimensions: {
                width: vizElem.clientWidth,
                height: 160,
                margin: { top: 10, bottom: 20 }
            }
        };

        render(config);
    };

    const draw = () => {
        if ($scope.vizElem && vm.buckets) {
            reallyDraw($scope.vizElem, vm.buckets);
        }
    };

    const debouncedDraw = _.debounce(draw, 200);

    $scope.$watch('vizElem', () => debouncedDraw());
    $scope.$watch('ctrl.buckets', () => debouncedDraw());

    angular.element($window).on('resize', () => debouncedDraw());
}

directiveController.$inject = ['$scope', '$window'];


export default function() {
    return {
        restrict: 'E',
        replace: true,
        template: '<div><div class="viz-elem wbc"><svg></svg></div></div>',
        scope: {},
        bindToController: {
            buckets: '=',
            eventDispatcher: '=?'
        },
        controllerAs: 'ctrl',
        controller: directiveController,
        link: (scope, elem) => {
            const vizElem = elem[0].querySelector('.viz-elem');
            scope.vizElem = vizElem;
        }
    };
}
