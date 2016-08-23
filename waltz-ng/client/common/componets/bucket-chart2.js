
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

    console.log("config", config)
    const { svg, buckets, dimensions } = config;

    svg.attr({
        width: dimensions.width,
        height: dimensions.height
    });

    const largestBucketSize = d3.max(
        buckets,
        b => b.size);

    const barScale = d3
        .scale
        .linear()
        .domain([0, largestBucketSize])
        .range([0, dimensions.width / 2]);

    const yScale = d3
        .scale
        .ordinal()
        .domain(_.map(buckets, 'name'))
        .rangeBands([0, dimensions.height], 0.3);

    const yAxis = d3.svg
        .axis()
        .scale(yScale)
        .tickSize(4);

    svg.append('g')
        .attr('class', 'y axis');

    svg.select('.axis')
        .attr('transform', ``)
        .call(yAxis);

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
            r: d => barScale(d.size),
            cx: d => yScale(d.name) + xScale.rangeBand() / 2,
            cy: dimensions.height / 2
        });

    svg.selectAll('.bucket title')
        .data(buckets)
        .text(d => `# = ${d.size}`);

};

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

            render(config);

        }
    };

    const debouncedDraw = _.debounce(draw, 200);

    vm.$onChanges = debouncedDraw;

    angular
        .element($window)
        .on('resize', () => debouncedDraw());
}


controller.$inject = [
    '$window',
    '$element'
];


const component = {
    template: '<div class="viz-elem wbc"><svg></svg></div>',
    controller,
    bindings
};


export default component;
