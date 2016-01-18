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
import d3 from 'd3';
import _ from 'lodash';
import angular from 'angular';


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

    const yearAxis = d3.svg.axis()
        .scale(scale)
        .tickFormat(d=> d % 10 === 1 ? Math.round(d / 10) : '')  // only show for Q1's
        .orient('bottom');

    const quarterAxis = d3.svg.axis()
        .scale(scale)
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
        .attr({
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

    const yqScale = d3.scale.ordinal()
        .domain(_.map(yqs, 'id'))
        .rangeBands([0, width], 0.2);

    const barScale = d3.scale.linear()
        .domain([0, _.max(data.changes, 'size').size])
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
        .attr({
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
    scope.svg = d3.select(scope.vizElem).append('svg');
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
