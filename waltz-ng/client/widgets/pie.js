
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


function renderArcs(holder, config, data) {

    const {
        colorProvider,
        valueProvider = (d) => d.count,
        idProvider = (d) => d.data.key,
        size = 100
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

}


function render(svg, config, data) {
    const { size = 100 } = config;
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
