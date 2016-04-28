/*
 *  This file is part of Waltz.
 *
 *  Waltz is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Waltz is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import _ from "lodash";
import {green, red} from "../../common/colors";


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

    const maxScore = scores.length > 0
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

    return {
        x: d3.scale.ordinal().domain(_.range(0, bucketCount)).rangeBands([0, sizing.w], 0.2),
        y: d3.scale.linear().domain([0, biggestBucket]).range([0, sizing.h - verticalPadding]),
        color: d3.scale.linear().domain([0, bucketCount]).range([green.brighter(1.2), red.brighter(1.2)])
    };
}


function drawXAxis(buckets, container, scale, sizing) {

    var xAxis = d3.svg.axis()
        .scale(scale)
        .tickFormat(d => d % 4 == 0 ? d3.round(buckets[d].begin, 2) : "")
        .orient('bottom');

    const xAxisGroup = container.selectAll('.x-axis')
        .data([1], () => 1);

    xAxisGroup.enter()
        .append('g')
        .attr('class', 'axis x-axis');

    xAxisGroup.attr('transform', `translate(0, ${sizing.h - sizing.padding.bottom})`)
        .call(xAxis);
}


function drawBucketBars(buckets, container, scales, sizing, repaint, onSelect) {

    const bucketBars = container.selectAll('.bucket-bar')
        .data(buckets);

    bucketBars
        .enter()
        .append('rect')
        .classed('bucket-bar', true);

    bucketBars
        .classed('clickable', onSelect != null)
        .attr({
            fill: (d, i) => scales.color(i),
            stroke: '#444',
            x: (d, i) => scales.x(i),
            width: scales.x.rangeBand(),
            y:  d => (sizing.h - sizing.padding.bottom) - (scales.y(d.items.length)),
            height: d => scales.y(d.items.length),
            opacity: d => d.isMouseOver ? 1 : 0.7
        })
        .on('click', d => { if (onSelect) { onSelect(d); } })
        .on('mouseenter', d => { d.isMouseOver = true; repaint(); })
        .on('mouseleave', d => { d.isMouseOver = false; repaint(); });
}


function drawBucketLabels(buckets, container, scales, sizing) {

    const bucketCountLabels = container.selectAll('.bucket-count-label')
        .data(buckets, (d, i) => i);

    bucketCountLabels.enter()
        .append('text')
        .classed('bucket-count-label', true);

    bucketCountLabels
        .text(d => d.items.length)
        .attr({
            'text-anchor': 'middle',
            y: d => (sizing.h - sizing.padding.bottom) - (scales.y(d.items.length)) - 4,
            x: (d, i) => scales.x(i) + scales.x.rangeBand() / 2,
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

    const svg = d3
        .select(config.elem)
        .selectAll('svg')
        .data([1]);

    svg.enter()
        .append('svg');

    svg.attr({
            width: sizing.w,
            height: sizing.h
        });

    drawXAxis(buckets, svg, scales.x, sizing);
    drawBucketBars(buckets, svg, scales, sizing, repaint, config.onSelect);
    drawBucketLabels(buckets, svg, scales, sizing);
}


function controller($scope) {
    const vm = this;

    const watcher = ([elem, complexity]) => {
        if (!elem || !complexity) return;

        const buckets = bucket(complexity);

        const onSelect = vm.onSelect ? d => $scope.$apply(vm.onSelect(d)) : null;

        const config = {
            elem,
            complexity,
            buckets,
            sizing: vm.sizing,
            onSelect
        };

        render(config);
    };

    $scope.$watchGroup(['elem', 'ctrl.complexity'], watcher);
}

controller.$inject = ['$scope'];


function link(scope, elem, attr) {
    scope.elem = elem[0];
}


export default () => ({
    restrict: 'E',
    replace: true,
    link,
    controller,
    controllerAs: 'ctrl',
    scope: {},
    template: '<div class="viz-holder"></div>',
    bindToController: {
        complexity: '=',
        sizing: '=?',
        onSelect: '=?'
    }
});