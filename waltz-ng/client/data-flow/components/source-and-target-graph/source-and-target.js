import {initialiseData} from '../../../common';
import {authoritativeRatingColorScale} from '../../../common/colors';
import _ from 'lodash';
import d3 from 'd3';


const template = require('./source-and-target.html');


const bindings = {
    entityRef: '<',
    flows: '<',
    decorators: '<',
    tweakers: '<'
};


let highlighted = null;
let redraw = null;


function mkDfltTweaker(name) {
    return (d) => console.log(name, d); // default tweaker
}


function mkDfltTweakers(name) {
    return {
        onSelect: mkDfltTweaker(`wsat:${name}.onSelect`),
        onEnter: mkDfltTweaker(`wsat:${name}.onAppEnter`),
        onLeave: mkDfltTweaker(`wsat:${name}.onLeave`)
    };
}


const dfltTweakers = {
    source: mkDfltTweakers("source"),
    target: mkDfltTweakers("target"),
    type: mkDfltTweakers("type"),
};


const initialState = {
};


const animationDuration = 400;


const baseDimensions = {
    graph: {
        width: 1000,
        height: 200,
    },
    margin: {
        top: 40,
        left: 20,
        right: 20,
        bottom: 50
    },
    label: {
        height: 10,
        minSpacing: 8,
        width: 200
    },
    header: {
        height: 20
    },
    circleSize: 24
};


function drawTitleBar(titleBar, dimensions) {
    const dy = dimensions.margin.top / 2;

    const labels = ["Upstream Sources", "Data Types", "Downstream Targets"];

    const textLabels = titleBar.selectAll("text")
        .data(labels);

    textLabels
        .enter()
        .append('text')
        .text(d => d)
        .attr({
            'text-anchor': 'middle'
        });

    textLabels
        .attr({
            'transform': (d, i) => {
                switch (i) {
                    case 0: return `translate(${dimensions.label.width}, ${dy})`;
                    case 1: return `translate(${dimensions.graph.width / 2 - 20}, ${dy})`;
                    case 2: return `translate(${dimensions.graph.width - dimensions.label.width}, ${dy})`;
                }
            },
        });


    const line = titleBar
        .selectAll("line")
        .data([true]);

    line.enter()
        .append('line');

    line.attr({
        x1: 0,
        y1: dy + 10,
        x2: dimensions.graph.width - 40,
        y2: dimensions.margin.top / 2 + 10,
        stroke: '#ccc'
    });
}


function prepareGraph(svg) {
    const header = svg
        .append("g")
        .classed("wsat-header", true);

    const sources = svg
        .append("g")
        .classed("wsat-sources", true)
        .classed("wsat-apps", true);

    const targets = svg
        .append("g")
        .classed("wsat-targets", true)
        .classed("wsat-apps", true);

    const types = svg
        .append("g")
        .classed("wsat-types", true);

    const outbound = svg
        .append("g")
        .classed("wsat-outbound", true);

    const inbound = svg
        .append("g")
        .classed("wsat-inbound", true);

    svg.append('defs')
        .append('marker')
        .attr({
            id: 'wsat-arrowhead',
            refX: 10,
            refY: 4,
            markerWidth: 8,
            markerHeight: 8,
            orient: 'auto'
        })
        .append('path')
        .attr('d', 'M 0,0 V 8 L9,4 Z'); // arrowhead shape

    return {
        header,
        inbound,
        outbound,
        sources,
        svg,
        targets,
        types
    };
}


function mkModel({ flows = [], decorators = [], entityRef, allTypes = []}) {
    const flowIds = _.map(flows, 'id');
    const relevantDecorators = _.filter(
        decorators,
        d => _.includes(flowIds, d.dataFlowId));

    const { inbound = [], outbound = [] } = _.groupBy(
        flows,
        f => f.source.id === entityRef.id
            ? 'outbound'
            : 'inbound');

    const sources = _.chain(inbound).map("source").uniqBy('id').value();
    const targets = _.chain(outbound).map("target").uniqBy('id').value();

    const allTypesById = _.keyBy(allTypes, 'id');

    const decoratorsByFlowId = _.chain(relevantDecorators)
        .filter(d => d.decoratorEntity.kind === 'DATA_TYPE')
        .groupBy("dataFlowId")
        .value();

    const sourceToType = _.chain(inbound)
        .flatMap(f => _.map(
            decoratorsByFlowId[f.id] || [],
            d => ({ from: f.source.id, to: d.decoratorEntity.id, rating: d.rating })))
        .value();

    const typeToTarget = _.chain(outbound)
        .flatMap(f => _.map(
            decoratorsByFlowId[f.id] || [],
            d => ({ from: d.decoratorEntity.id, to: f.target.id, rating: d.rating })))
        .value();

    const types = _.chain(relevantDecorators)
        .map(d => d.decoratorEntity)
        .filter(d => d.kind === 'DATA_TYPE')
        .map(d => d.id)
        .uniq()
        .map(id => allTypesById[id])
        .orderBy(t => t.name)
        .value();

    return {
        sources,
        targets,
        types,
        sourceToType,
        typeToTarget
    };
}


function calculateDimensions(model) {

    const minSize = 4;
    const { sources = [], targets = [], types = [] } = model;
    const largest = _.max([sources.length, targets.length, types.length, minSize]);

    const canvas = {
        width : baseDimensions.graph.width - (baseDimensions.margin.left + baseDimensions.margin.right),
        height: largest * (baseDimensions.label.height + baseDimensions.label.minSpacing)
    };

    const graph = {
        height : canvas.height
            + baseDimensions.header.height
            + baseDimensions.margin.top
            + baseDimensions.margin.bottom
    };

    return _.defaultsDeep( { graph, canvas }, baseDimensions);
}


function translate(elem, dx = 0, dy = 0) {
    return elem.attr(
        "transform",
        `translate(${dx}, ${dy})`);
}


function setupSizing(sections, dimensions) {
    sections.svg.attr({
        width : dimensions.graph.width,
        height: dimensions.graph.height
    });

    const sdx = dimensions.margin.left + dimensions.label.width;
    const sdy = dimensions.margin.top;
    translate(sections.sources, sdx, sdy);

    const tdx = dimensions.canvas.width - dimensions.label.width;
    const tdy = dimensions.margin.top;
    translate(sections.targets, tdx, tdy);

    const dtdx = dimensions.canvas.width / 2;
    const dtdy = dimensions.margin.top;
    translate(sections.types, dtdx, dtdy);
}


function getColumnScaleRange(dimensions) {
    return [
        dimensions.margin.top,
        dimensions.graph.height - dimensions.margin.bottom
    ];
}


function mkScale(items, dimensions) {
    return d3
        .scale
        .ordinal()
        .domain(_.chain(items)
            .sortBy(a => a.name.toLowerCase())
            .map('id')
            .value())
        .rangePoints(getColumnScaleRange(dimensions));
}


function setupScales(model, dimensions) {
    const source = mkScale(model.sources, dimensions);
    const target = mkScale(model.targets, dimensions);
    const type = mkScale(model.types, dimensions);

    return {
        source,
        target,
        type
    };
}


function drawLabels(section, items = [], scale, anchor = 'start', tweakers) {
    const labels = section
        .selectAll('.wsat-label')
        .data(items, d => d.id);

    labels
        .enter()
        .append('text')
        .classed('clickable', true)
        .classed('wsat-label', true)
        .attr({
            'text-anchor': anchor,
            opacity: 0
        })
        .on('mouseenter.highlight', d => { highlighted = d.id; redraw(); })
        .on('mouseleave.highlight', d => { highlighted = null; redraw(); })
        .on('click.tweaker', tweakers.onSelect)
        .on('mouseenter.tweaker', tweakers.onEnter)
        .on('mouseleave.tweaker', tweakers.onLeave)
        .text(app => _.truncate(app.name, 26));

    labels
        .classed('wsat-hover', (d) => highlighted === d.id)
        .transition()
        .duration(animationDuration)
        .attr({
            'transform':  (d, i) => `translate(0, ${ scale(d.id) })`,
            opacity: 1
        });

    labels
        .exit()
        .remove();
}


function drawArcs(section, model, layoutFn) {
    const arcs = section
        .selectAll('.wsat-arc')
        .data(model, d => d.from + '-' + d.to);

    arcs
        .enter()
        .append('line')
        .classed('wsat-arc', true)
        .attr({
            opacity: 0,
            'marker-end': 'url(#wsat-arrowhead)',
            stroke: d => authoritativeRatingColorScale(d.rating)
        });

    arcs
        .classed('wsat-hover', d => d.to === highlighted || d.from === highlighted)
        .transition()
        .duration(animationDuration)
        .call(layoutFn)
        .attr('opacity', 1)
        .call(internetExplorerFix);

    arcs
        .exit()
        .remove();
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
        200
    );
}


function drawTypeBoxes(section, items, scale, dimensions) {
    const boxes = section
        .selectAll('.wsat-type-box')
        .data(items, d => d.id);

    boxes
        .enter()
        .append('rect')
        .classed('wsat-type-box', true)
        .attr({
            fill: '#fafafa',
            stroke: '#eee',
            x: dimensions.width / 2 * -1 + 2,
            y: 0,
            opacity: 0
        });

    boxes
        .transition()
        .duration(animationDuration)
        .attr({
            x: dimensions.width / 2 * -1 + 2,
            y: d => scale(d.id) - dimensions.height - 2,
            width: dimensions.width - 4,
            height: dimensions.height + 6,
            opacity: 1
        });

    boxes
        .exit()
        .remove();
}


function drawInbound(section, model, scales, dimensions) {
    const inboundLayout = (selection) => selection
        .attr({
            x1: dimensions.margin.left + dimensions.label.width + 10,
            x2 : (dimensions.canvas.width / 2) - (dimensions.label.width / 2),
            y1: d => dimensions.margin.top + scales.source(d.from) - dimensions.label.height / 2,
            y2: d => dimensions.margin.top + scales.type(d.to) - dimensions.label.height / 2,
        });
    drawArcs(section, model, inboundLayout);
}


function drawOutbound(section, model, scales, dimensions) {
    const outboundLayout = (selection) => selection
        .attr({
            x1: (dimensions.canvas.width / 2) + (dimensions.label.width / 2),
            x2: dimensions.canvas.width - (dimensions.label.width + 10),
            y1: d => dimensions.margin.top + scales.type(d.from) - dimensions.label.height / 2,
            y2: d => dimensions.margin.top + scales.target(d.to) - dimensions.label.height / 2,
        });

    drawArcs(section, model, outboundLayout);
}


function update(
    sections,
    model,
    tweakers) {

    redraw = () => update(sections, model, tweakers);

    const dimensions = calculateDimensions(model);

    setupSizing(sections, dimensions);

    drawTitleBar(sections.header, dimensions);

    const scales = setupScales(model, dimensions);

    drawLabels(sections.sources, model.sources, scales.source, 'end', tweakers.source, redraw);
    drawLabels(sections.targets, model.targets, scales.target, 'start', tweakers.target, redraw);

    drawTypeBoxes(sections.types, model.types, scales.type, dimensions.label);
    drawLabels(sections.types, model.types, scales.type, 'middle', tweakers.type, redraw);

    drawInbound(sections.inbound, model.sourceToType, scales, dimensions);
    drawOutbound(sections.outbound, model.typeToTarget, scales, dimensions);
}


function controller($element, $window, dataTypeService) {

    const vm = initialiseData(this, initialState);
    const svg = d3.select($element.find('svg')[0]);

    const svgSections = prepareGraph(svg);

    const debouncedRender = _.debounce(() => {
        if (! vm.entityRef) { return; }

        baseDimensions.graph.width = $element
            .parent()[0]
            .clientWidth;

        const tweakers = _.defaultsDeep(vm.tweakers, dfltTweakers);

        dataTypeService
            .loadDataTypes()
            .then(types => {
                const data = {
                    flows: vm.flows || [],
                    decorators: vm.decorators || [],
                    entityRef: vm.entityRef,
                    allTypes: types
                };


                const model = mkModel(data);

                update(svgSections, model, tweakers);
            });
    }, 100);

    vm.$onChanges = (changes) => debouncedRender();

    angular
        .element($window)
        .on('resize', () => debouncedRender());

}


controller.$inject = [
    '$element',
    '$window',
    'DataTypeService'
];


const component = {
    bindings,
    template,
    controller
};


export default component;
