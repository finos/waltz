
import _ from 'lodash';
import d3 from 'd3';

import { noop } from '../../../common';

const transitionSpeed = 150;

let redraw = noop;
let highlighted = null;

const dfltTweaker = {
    enter: noop,
    update: noop
};

function highlighter(selection, pred) {
    selection
        .attr('stroke-width', f => pred(f) ? 2 : 0.8)
        .attr('opacity', f => pred(f) ? 0.8 : 0.5);
}

function onHighlight(selection, attr) {
    selection
        .on('mouseover', obj => { highlighted = obj[attr]; redraw(); })
        .on('mouseout', () => { highlighted = null; redraw(); });
}


function getColumnScaleRange(dimensions) {
    return [
        dimensions.margin.top,
        dimensions.viz.height - dimensions.margin.bottom
    ];
}

function mkAppScale(apps, dimensions) {
    return d3.scale.ordinal()
        .domain(_.chain(apps)
            .sortBy(a => a.name.toLowerCase())
            .map('id')
            .value())
        .rangePoints(getColumnScaleRange(dimensions));
}


function drawTypesColumn(svg, dimensions, types, typeScale, tweaker) {

    const typesGroup = svg.selectAll('#types')
        .data([true]);

    typesGroup
        .enter()
        .append('g')
        .attr('id', 'types');

    typesGroup.attr('transform', `translate(${(dimensions.viz.width / 2)}, 0)`);

    const typesSelection = typesGroup
        .selectAll('.wafd-type')
        .data(types, dt => dt.code);

    typesSelection
        .enter()
        .append('g')
        .classed('wafd-type', true)
        .call(tweaker.enter || noop)
        .call(onHighlight, 'code')
        .attr('transform', `translate(0, ${dimensions.viz.height / 2})`)
        .append('text')
        .attr('text-anchor', 'middle')
        .text(type => _.trunc(type.name, 16));

    typesSelection.exit().remove();

    typesSelection
        .transition()
        .duration(transitionSpeed)
        .call(tweaker.update || noop)
        .attr('transform', type => `translate(0, ${typeScale(type.code)})`);

}


function drawSourcesColumn(svg, dimensions, sources, scale, tweaker) {

    const sourcesGroup = svg.selectAll('#sources')
        .data([true]);

    sourcesGroup
        .enter()
        .append('g')
        .attr('id', 'sources');

    sourcesGroup
        .attr('transform', `translate(${dimensions.label.width / 1.05}, 0)`);

    const sourcesSelection = sourcesGroup
        .selectAll('.wafd-source')
        .data(sources, s => s.id);

    sourcesSelection.enter()
        .append('g')
        .attr('transform', `translate(0, ${dimensions.viz.height / 2})`)
        .call(tweaker.enter || noop)
        .call(onHighlight, 'id')
        .classed('wafd-source', true)
        .append('text')
        .attr('text-anchor', 'end')
        .text(app => _.trunc(app.name, 26));

    sourcesSelection.exit().remove();

    sourcesSelection
        .transition()
        .duration(transitionSpeed)
        .call(tweaker.update || noop)
        .attr('transform', app => `translate(0, ${scale(app.id)})`);

}


function drawTargetsColumn(svg, dimensions, targets, scale, tweaker) {

    const targetsGroup = svg.selectAll('#targets')
        .data([true]);

    targetsGroup
        .enter()
        .append('g')
        .attr('id', 'targets');

    targetsGroup
        .attr('transform', `translate(${dimensions.viz.width - dimensions.label.width / 1.05}, 0)`);

    const targetsSelection = targetsGroup
        .selectAll('.wafd-target')
        .data(targets, t => t.id);

    targetsSelection.enter()
        .append('g')
        .attr('transform', `translate(0, ${dimensions.viz.height / 2})`)
        .classed('wafd-target', true)
        .call(tweaker.enter || noop)
        .call(onHighlight, 'id')
        .append('text')
        .attr('text-anchor', 'start')
        .text(app => _.trunc(app.name, 26));

    targetsSelection.exit().remove();

    targetsSelection
        .transition()
        .duration(transitionSpeed)
        .call(tweaker.update || noop)
        .attr('transform', app => `translate(0, ${scale(app.id)})`);
}


function drawTitleBar(svg, dimensions) {
    const titleBar = svg.selectAll('.wafd-title-bar')
        .data([true]);

    titleBar.enter()
        .append('g')
        .classed('wafd-title-bar', true);

    const titleSelection = titleBar
        .selectAll('text')
        .data(['Upstream Sources', 'Data Types', 'Downstream Targets']);

    titleSelection.enter()
        .append('text')
        .attr('text-anchor', 'middle')
        .text(d => d);

    titleSelection
        .attr('transform', (d, i) => {
            const translations = [
                `translate(${dimensions.margin.left}, ${dimensions.margin.top / 2})`,
                `translate(${dimensions.viz.width / 2}, ${dimensions.margin.top / 2})`,
                `translate(${dimensions.viz.width - dimensions.margin.right}, ${dimensions.margin.top / 2})`
            ];
            return translations[i];
        });


    titleBar.append('line')
        .attr({
            x1: 0, y1: dimensions.margin.top / 2 + 10,
            x2: dimensions.viz.width, y2: dimensions.margin.top / 2 + 10,
            stroke: '#ccc'
        });
}


/*
 * This works round a bug with IE8+ where using markers with
 * svg causes elements not to be rendered/updated unless a
 * redrawn is forced.  In this case we force by re-adding
 * (non-duplicative)
 */
function internetExplorerFix(selection) {
    setTimeout(
        () => selection.each(function() { this.parentNode.insertBefore(this, this); }),
        100
    );
}


function drawIncomingLines(svg, dimensions, incoming, sourceScale, typeScale, tweaker) {
    const incomingFlows = svg.selectAll('#incoming-flows').data([true]);


    incomingFlows
        .enter()
        .append('g')
        .attr('id', 'incoming-flows');

    const inbound = incomingFlows
        .selectAll('.wafd-flow')
        .data(incoming, f => f.type + '_' + f.source);

    inbound.enter()
        .append('line')
        .attr('stroke', '#999')
        .attr('marker-end', 'url(#arrowhead)')
        .classed('wafd-flow', true)
        .call(tweaker.enter || noop)
        .attr({
            x1: dimensions.label.width + 5,
            x2: dimensions.viz.width / 2 - dimensions.label.width / 2,
            y1: dimensions.viz.height / 2,
            y2: dimensions.viz.height / 2
        });

    inbound.exit().remove();


    const isHighlighted = f => highlighted === f.source || highlighted === f.type;

    inbound
        .transition()
        .duration(transitionSpeed)
        .call(highlighter, isHighlighted)
        .call(tweaker.update || noop)
        .attr({
            x1: dimensions.label.width + 5,
            x2: dimensions.viz.width / 2 - dimensions.label.width / 2
        })
        .attr('y1', f => sourceScale(f.source) - dimensions.label.height / 2)
        .attr('y2', f => typeScale(f.type) - dimensions.label.height / 2);

    inbound.call(internetExplorerFix);
}


function drawOutgoingLines(svg, dimensions, outgoing, targetScale, typeScale, tweaker) {
    const outgoingFlows = svg.selectAll('#outgoing-flows').data([true]);

    outgoingFlows
        .enter()
        .append('g')
        .attr('id', 'outgoing-flows');

    const outbound = outgoingFlows
        .selectAll('.wafd-flow')
        .data(outgoing, f => f.type + '_' + f.target);

    outbound.enter()
        .append('line')
        .attr('stroke', '#999')
        .attr('marker-end', 'url(#arrowhead)')
        .call(tweaker.enter || noop)
        .classed('wafd-flow', true)
        .attr({
            x1: dimensions.viz.width / 2 + dimensions.label.width / 2,
            x2: dimensions.viz.width - dimensions.label.width - 5,
            y1: dimensions.viz.height / 2,
            y2: dimensions.viz.height / 2
        });

    outbound.exit().remove();

    const isHighlighted = f => highlighted === f.target || highlighted === f.type;

    outbound
        .transition()
        .duration(transitionSpeed)
        .call(highlighter, isHighlighted)
        .call(tweaker.update || noop)
        .attr({
            x1: dimensions.viz.width / 2 + dimensions.label.width / 2,
            x2: dimensions.viz.width - dimensions.label.width - 5
        })
        .attr('y1', f => typeScale(f.type) - dimensions.label.height / 2)
        .attr('y2', f => targetScale(f.target) - dimensions.label.height / 2);

    outbound.call(internetExplorerFix);
}


function draw(data, dimensions, svg, tweakers) {

    redraw = () => draw(data, dimensions, svg, tweakers);


    svg.attr({width: dimensions.viz.width, height: dimensions.viz.height});

    const { incoming, outgoing, types, sources, targets } = data;

    const typeScale = d3.scale.ordinal()
        .domain(_.chain(types)
            .sortBy('name')
            .map('code')
            .value())
        .rangePoints(getColumnScaleRange(dimensions));

    const sourceScale = mkAppScale(sources, dimensions);
    const targetScale = mkAppScale(targets, dimensions);

    drawTitleBar(svg, dimensions);
    drawTypesColumn(svg, dimensions, types, typeScale, tweakers.type || dfltTweaker);
    drawSourcesColumn(svg, dimensions, sources, sourceScale, tweakers.source || dfltTweaker);
    drawTargetsColumn(svg, dimensions, targets, targetScale, tweakers.target || dfltTweaker);

    drawIncomingLines(svg, dimensions, incoming, sourceScale, typeScale, tweakers.incoming || dfltTweaker);
    drawOutgoingLines(svg, dimensions, outgoing, targetScale, typeScale, tweakers.outgoing || dfltTweaker);
}


export function init(vizElem) {
    const svg = d3.select(vizElem)
        .append('svg');

    svg.append('defs').append('marker')
        .attr({
            id: 'arrowhead',
            refX: 10,
            refY: 4,
            markerWidth: 8,
            markerHeight: 8,
            orient: 'auto'
        })
        .append('path')
        .attr('d', 'M 0,0 V 8 L8,4 Z'); // arrowhead shape

    return svg;
}

export function render(svg, dimensions, data, tweakers) {
    draw(data, dimensions, svg, tweakers);
}

