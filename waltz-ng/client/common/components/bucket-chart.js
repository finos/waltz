
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
import angular from "angular";
import {scaleLinear, scaleBand} from 'd3-scale';
import {axisBottom} from 'd3-axis';
import {select} from 'd3-selection';
import 'd3-selection-multi';


const bindings = {
    buckets: '=',
    onBucketSelect: '=?'
};


function render(config, onBucketSelect) {

    const { svg, buckets, dimensions } = config;

    svg.attrs({
        width: dimensions.width,
        height: dimensions.height
    });

    const maxBucketSize = _.chain(buckets)
        .map(b => b.size)
        .max()
        .value();

    const blobScale = scaleLinear()
        .domain([0, maxBucketSize])
        .range([0, ( dimensions.height / 1.6) / 2]);

    const xScale = scaleBand()
        .domain(_.map(buckets, 'name'))
        .range([0, dimensions.width], 0.3);

    const xAxis = axisBottom()
        .scale(xScale)
        .tickSize(4);

    svg.append('g')
        .attr('class', 'x axis');

    svg.select('.axis')
        .attr('transform', `translate(0, ${ dimensions.height - dimensions.margin.bottom } )`)
        .call(xAxis);

    const bucketElems = svg.selectAll('.bucket')
        .data(buckets);


    const newBucketElems = bucketElems
        .enter()
        .append('circle')
        .classed('bucket', true)
        .on('click', d => {
            if (onBucketSelect) {
                onBucketSelect(d);
            }
        });

    newBucketElems
        .append('title')
        .text(d => `# = ${d.size}`);

    bucketElems
        .merge(newBucketElems)
        .attrs({
            r: d => blobScale(d.size),
            cx: d => xScale(d.name) + xScale.bandwidth() / 2,
            cy: dimensions.height / 2
        });

}


function controller($window, $element) {
    const vm = this;
    const vizElem = $element.find('div')[0];

    const draw = () => {
        if (vm.buckets) {

            const config = {
                svg: select(vizElem)
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
