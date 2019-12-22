
/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
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
