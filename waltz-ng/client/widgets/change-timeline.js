/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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

import {axisBottom} from "d3-axis";
import {select} from 'd3-selection';
import {scaleLinear, scaleBand} from 'd3-scale';
import 'd3-selection-multi';

import _ from "lodash";
import angular from "angular";


function yqToVal(yq) {
    return yq.year * 10 + yq.quarter;
}


function prepareYears(st, en) {
    const currentYear = new Date().getFullYear();
    const start = st || { year: currentYear };
    const end = en || { year: start.year + 5 };

    return _.range(start.year, end.year + 1);
}


function prepareYearQuarters(years) {
    return _.chain(years)
        .map(y => _.map([1, 2, 3, 4], q => ({ year: y, quarter: q, id: y * 10 + q })))
        .flatten()
        .value();
}


function renderAxis(container, scale) {

    const yearAxis = axisBottom(scale)
        .tickFormat(d=> d % 10 === 1 ? Math.round(d / 10) : '');  // only show for Q1's

    const quarterAxis = axisBottom(scale)
        .tickFormat(d => 'Q' + d % 10)
        .tickSize(4)
        .orient('bottom');

    container.append('g')
        .attr('class', 'year axis')
        .attr('transform', 'translate(0, 12)')
        .call(yearAxis);

    container.append('g')
        .attr('class', 'yq axis')
        .call(quarterAxis);
}


function renderChanges(container, yqScale, barScale, changes, selected) {

    const barWidth = yqScale.rangeBand();
    return container.selectAll('.bar')
        .data(changes)
        .enter()
        .append('rect')
        .classed('changes', true)
        .classed('applied', d=> selected && yqToVal(selected) > yqToVal(d))
        .classed('pending', d=> !selected || yqToVal(selected) < yqToVal(d))
        .classed('selected', d => selected && selected.year === d.year && selected.quarter === d.quarter)
        .attrs({
            x: d => yqScale(yqToVal(d)),
            y: d=> barScale(d.size),
            width: barWidth,
            height: d => barScale(0) - barScale(d.size)
        });
}


function render(svg, data) {

    const height = 80;

    const axisHeight = 4 + 24; // tick + 2 * font

    const width = Number(svg.attr('width'));
    const years = prepareYears(data.start, data.end);
    const yqs = prepareYearQuarters(years);

    const yqScale = scaleBand()
        .domain(_.map(yqs, 'id'))
        .range([0, width], 0.2);

    const barScale = scaleLinear()
        .domain([0, _.maxBy(data.changes, 'size').size])
        .range([height - (axisHeight + 10), 4]);

    // scrub
    svg.selectAll('*').remove();

    svg.attr('height', height);

    const axisHolder = svg
        .append('g')
        .attr('transform', `translate(0, ${height - (axisHeight + 10) })`);

    renderAxis(axisHolder, yqScale);

    renderChanges(svg, yqScale, barScale, data.changes, data.selected);

    svg.selectAll('.click-handler')
        .data(yqScale.domain())
        .enter()
        .append('rect')
        .classed('click-handler', true)
        .attrs({
            fill: 'none',
            'pointer-events': 'visible', // allows clicking on fill:'none'
            x: d => yqScale(d),
            y: 0,
            width: yqScale.rangeBand(),
            height: height
        })
        .on('click', (d) => {
            data.onSelect({ year: Math.round(d / 10), quarter: d % 10 });
        });

}


function controller($scope, $window) {

    function ready() {
        return true;
    }

    const debouncedRender = _.debounce(() => {
        if (!ready()) return;

        const width = $scope.vizElem.offsetWidth;
        $scope.svg.attr('width', width);

        const data = {
            start: this.start,
            end: this.end,
            changes: this.changes,
            onSelect: (d) => $scope.$apply(this.onSelect()(d)),
            selected: this.selected
        };

        render($scope.svg, data);
    }, 10);

    angular.element($window).on('resize', () => debouncedRender());
    $scope.$watch('svg', () => debouncedRender());
    $scope.$watch('ctrl.selected', () => debouncedRender());
}

controller.$inject = ['$scope', '$window'];


function link(scope, elem) {
    scope.vizElem = elem[0].querySelector('.viz');
    scope.svg = select(scope.vizElem).append('svg');
}


export default () => ({
    restrict: 'E',
    replace: true,
    scope: {
        start: '=',
        end: '=',
        changes: '=',
        selected: '=',
        onSelect: '&'
    },
    template: '<div><div class="viz waltz-ct"></div></div>',
    controllerAs: 'ctrl',
    bindToController: true,
    controller,
    link
});
