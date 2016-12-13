/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";
import {green, red} from "../../common/colors";
import {notEmpty} from "../../common";
import {interpolateRgb} from "d3-interpolate";
import {axisBottom} from "d3-axis";
import {scaleBand, scaleLinear} from "d3-scale";
import {select} from "d3-selection";
import "d3-selection-multi";


const sizingDefaults = {
    h: 200,
    w: 600,
    padding: {
        bottom: 20,
        top: 40
    }
};


function bucket(complexityData) {
    const scores = _.map(
        complexityData,
        d => ({ score: d.overallScore, id: d.id }));

    const maxScore = notEmpty(scores)
        ? _.maxBy(scores, 'score').score
        : 0;

    const step = 0.05;
    const buckets = _.range(0, _.max([maxScore + step, 1]), step)
        .map(b => ({ begin: b, end: b + step, items: [] }));

    _.each(scores, s => {
        const score = s.score;
        const bucket = _.find(buckets, b => (score => b.begin) && (score < b.end));
        if (! bucket) {
            console.log('no bucket ', s, buckets);
        } else {
            bucket.items.push(s);
        }
    });

    return buckets;
}


function findBiggestBucket(buckets) {
    return _.maxBy(buckets, b => b.items.length);
}


function prepareScales(buckets, sizing) {
    const bucketCount = buckets.length;
    const biggestBucket = findBiggestBucket(buckets).items.length;

    const verticalPadding = sizing.padding.top + sizing.padding.bottom;

    const scales = {
        x: scaleBand()
            .domain(_.range(0, bucketCount))
            .range([0, sizing.w])
            .padding(0.2),
        y: scaleLinear()
            .domain([0, biggestBucket])
            .range([0, sizing.h - verticalPadding]),
        color: scaleLinear()
            .domain([0, bucketCount])
            .interpolate(interpolateRgb)
            .range([green, red])
    };

    return scales;
}


function drawXAxis(buckets, container, scale, sizing) {

    const xAxis = axisBottom()
        .scale(scale)
        .tickFormat(d => d % 4 == 0
            ? Number(buckets[d].begin).toFixed(1)
            : "");

    const xAxisGroup = container
        .selectAll('.x-axis')
        .data([1], () => 1);

    xAxisGroup.enter()
        .append('g')
        .attr('class', 'axis x-axis')
        .merge(xAxisGroup)
        .attr('transform', `translate(0, ${sizing.h - sizing.padding.bottom})`)
        .call(xAxis);
}


function drawBucketBars(buckets, container, scales, sizing, repaint, onSelect) {

    const groups = container
        .selectAll('.bucket-g')
        .data(buckets, d => `${d.begin}_${d.end}`);

    const newGroups = groups
        .enter()
        .append('g')
        .classed('bucket-g', true)
        .attr('transform', (d, i) => `translate(${ scales.x(i) }, 0)`);

    newGroups
        .append('rect')
        .classed('bucket-bar', true)
        .attr('stroke', '#444')
        .attr('width', scales.x.bandwidth())
        .attr('fill', (d, i) => scales.color(i));

    newGroups
        .append('rect')
        .classed('clickable', true)
        .classed('bucket-band', true)
        .style('visibility', 'hidden')
        .attr('pointer-events', 'all')
        .attr('width', scales.x.bandwidth())
        .attr('y', 0)
        .attr('height', sizing.h);

    const allGroups = groups
        .merge(newGroups);

    allGroups
        .selectAll('.bucket-bar')
        .data(d => [d])
        .attr('y', d => (sizing.h - sizing.padding.bottom) - (scales.y(d.items.length)))
        .attr('height', d => scales.y(d.items.length));

    allGroups
        .selectAll('.bucket-band')
        .data(d => [d])
        .on('click', d => { if (onSelect) { onSelect(d); } })
        .on('mouseenter.hover', d => { d.isMouseOver = true; repaint(); })
        .on('mouseleave.hover', d => { d.isMouseOver = false; repaint(); });

}


function drawBucketLabels(buckets, container, scales, sizing) {

    const bucketCountLabels = container.selectAll('.bucket-count-label')
        .data(buckets, (d, i) => i);

    const newLabels =bucketCountLabels
        .enter()
        .append('text')
        .classed('bucket-count-label', true);

    bucketCountLabels
        .merge(newLabels)
        .text(d => d.items.length)
        .attrs({
            'text-anchor': 'middle',
            y: d => (sizing.h - sizing.padding.bottom) - (scales.y(d.items.length)) - 4,
            x: (d, i) => scales.x(i) + scales.x.bandwidth() / 2,
            opacity: d => d.isMouseOver ? 1 : 0
        });
}


function render(config) {

    const repaint = () => render(config);

    const sizing = _.defaults(
        {},
        config.sizing,
        { w: config.elem.clientWidth },
        sizingDefaults
    );

    const buckets = config.buckets;
    const scales = prepareScales(buckets, sizing);

    const svg = select(config.elem)
        .selectAll('svg')
        .data([1]);

    const newSvg = svg
        .enter()
        .append('svg');

    const mergedSvg = svg
        .merge(newSvg)
        .attrs({
            width: sizing.w,
            height: sizing.h
        });

    drawXAxis(buckets, mergedSvg, scales.x, sizing);
    drawBucketLabels(buckets, mergedSvg, scales, sizing);
    drawBucketBars(buckets, mergedSvg, scales, sizing, repaint, config.onSelect);
}


function controller($scope, $element) {
    const vm = this;

    const watcher = (complexity) => {
        if (!complexity) return;

        const buckets = bucket(complexity);

        const onSelect = vm.onSelect
            ? d => $scope.$apply(vm.onSelect(d))
            : null;

        const config = {
            elem: $element[0],
            complexity,
            buckets,
            sizing: vm.sizing,
            onSelect
        };

        render(config);
    };

    $scope.$watch(
        'ctrl.complexity',
        watcher,
        true);
}


controller.$inject = [
    '$scope',
    '$element'
];


export default () => ({
    restrict: 'E',
    replace: true,
    controller,
    controllerAs: 'ctrl',
    scope: {},
    template: '<div class="waltz-complexity-viz-holder"></div>',
    bindToController: {
        complexity: '=',
        sizing: '=?',
        onSelect: '=?'
    }
});