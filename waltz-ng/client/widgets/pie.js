/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */
import d3 from "d3";


const DEFAULT_SIZE = 70;

function renderArcs(holder, config, data) {

    const {
        colorProvider,
        valueProvider = (d) => d.count,
        idProvider = (d) => d.data.key,
        size = DEFAULT_SIZE
    } = config;

    const radius = size / 2;

    const arc = d3.svg.arc()
        .outerRadius(radius - 10)
        .innerRadius(0);

    function tweenPie(finish) {
        const start = {
            startAngle: 0,
            endAngle: 0
        };
        const i = d3.interpolate(start, finish);
        return (d) => arc(i(d));
    }

    const pie = d3.layout.pie()
        .sort(null)
        .value(valueProvider);

    const pieData = pie(_.filter(data, r => r.count > 0));

    const arcs = holder
        .selectAll('.arc')
        .data(pieData, idProvider);

    arcs.enter()
        .append('path')
        .classed('arc', true);

    arcs.attr({
        fill: d => colorProvider(d).brighter(),
        stroke: d => colorProvider(d)
    });

    arcs.exit()
        .remove();

    arcs.transition()
        .duration(400)
        .attrTween('d', tweenPie);

    const emptyPie = holder.selectAll('.empty-pie')
        .data(data.length ? [] : [1]);

    emptyPie.enter()
        .append('circle')
        .attr({
            cx: 0,
            cy: 0,
            r: radius / 2,
            fill: '#eee',
            stroke: '#bbb',
            'stroke-dasharray': [5, 1]
        })
        .classed('empty-pie', true);

    emptyPie.exit()
        .remove();
}


function render(svg, config, data) {
    const { size = DEFAULT_SIZE } = config;
    const width = size;
    const height = size;

    svg.attr( { width, height });

    const mainGroup = svg.selectAll('.main-group')
        .data([1]);

    mainGroup
        .enter()
        .append('g')
        .classed('main-group', true);

    mainGroup
        .attr('transform', `translate(${width / 2},${height / 2})`);

    renderArcs(mainGroup, config, data);
}


function link(scope, elem) {
    const vizElem = elem[0].querySelector('.viz');
    const svg = d3.select(vizElem).append('svg');

    scope.$watchGroup(['data', 'config'], ([data, config]) => {
        if (!data || !config) return;
        render(svg, config, data);
    });

}

/**
 * data: [....]
 *
 * config: {
 *   colorProvider,
 *   valueProvider : d => d.count,
 *   labelProvider : d => ''
 * }
 */
export default () => ({
    restrict: 'E',
    replace: true,
    template: '<span><span class="viz"></span></span>',
    scope: {
        data: '=',
        config: '='
    },
    link
});
