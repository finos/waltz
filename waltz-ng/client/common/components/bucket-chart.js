
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

import _ from "lodash";
import d3 from "d3";
import angular from "angular";


const bindings = {
    buckets: '=',
    onBucketSelect: '=?'
};


function render(config, onBucketSelect) {

    const { svg, buckets, dimensions } = config;

    svg.attr({
        width: dimensions.width,
        height: dimensions.height
    });

    const maxBucketSize = _.chain(buckets)
        .map(b => b.size)
        .max()
        .value();

    const blobScale = d3
        .scale
        .linear()
        .domain([0, maxBucketSize])
        .range([0, ( dimensions.height / 1.6) / 2]);

    const xScale = d3
        .scale
        .ordinal()
        .domain(_.map(buckets, 'name'))
        .rangeBands([0, dimensions.width], 0.3);

    const xAxis = d3.svg
        .axis()
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
            if (onBucketSelect) {
                onBucketSelect(d);
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
}


function controller($window, $element) {
    const vm = this;
    const vizElem = $element.find('div')[0];

    const draw = () => {
        if (vm.buckets) {

            const config = {
                svg: d3
                    .select(vizElem)
                    .select('svg'),
                buckets: vm.buckets,
                dimensions: {
                    width: vizElem.clientWidth,
                    height: 160,
                    margin: { top: 10, bottom: 20 }
                }
            };

            render(config, vm.onBucketSelect);

        }
    };

    const debouncedDraw = _.debounce(draw, 100);

    vm.$onChanges = debouncedDraw;

    vm.$onInit = () => angular
        .element($window)
        .on('resize', debouncedDraw);

    vm.$onDestroy = () => angular
        .element($window)
        .off('resize', debouncedDraw);
}


controller.$inject = [
    '$window',
    '$element'
];


const component = {
    template: '<div class="wbc"><svg></svg></div>',
    controller,
    bindings
};


export default component;
