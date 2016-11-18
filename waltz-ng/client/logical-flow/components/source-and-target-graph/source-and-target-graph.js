import {initialiseData} from "../../../common";
import {authoritativeRatingColorScale} from "../../../common/colors";
import _ from "lodash";
import d3 from "d3";


const template = require('./source-and-target-graph.html');


const bindings = {
    entityRef: '<',
    logicalFlows: '<',
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
    typeBlock: mkDfltTweakers("typeBlock")
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
        left: 5,
        right: 5,
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


function mkModel({ logicalFlows = [], decorators = [], entityRef, allTypes = []}) {
    const logicalFlowIds = _.map(logicalFlows, 'id');
    const relevantDecorators = _.filter(
        decorators,
        d => _.includes(logicalFlowIds, d.dataFlowId));

    const { inbound = [], outbound = [] } = _.groupBy(
        logicalFlows,
        f => f.source.id === entityRef.id
            ? 'outbound'
            : 'inbound');

    const sources = _.chain(inbound)
        .map("source")
        .uniqBy('id')
        .value();

    const targets = _.chain(outbound)
        .map("target")
        .uniqBy('id')
        .value();

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


function determineLabelTextAdjustment(anchor) {
    switch (anchor) {
        case 'start':
            return 10;
        case 'end':
            return -10;
        default:
            return 0;
    }
}


function determineLabelIconAdjustment(anchor) {
    switch (anchor) {
        case 'start':
            return -6;
        case 'end':
            return -6;
        default:
            return 0;
    }
}


function drawLabels(section, items = [], scale, anchor = 'start', tweakers) {
    const labels = section
        .selectAll('.wsat-label')
        .data(items, d => d.id);

    const newLabels = labels
        .enter()
        .append('g')
        .classed('clickable', true)
        .classed('wsat-label', true)
        .attr({
            'transform':  (d, i) => `translate(0, ${ scale(d.id) })`,
            opacity: 0
        })
        .on('mouseenter.highlight', d => { highlighted = d.id; redraw(); })
        .on('mouseleave.highlight', d => { highlighted = null; redraw(); })
        .on('click.tweaker', (d) => tweakers.onSelect(d, d3.event))
        .on('mouseenter.tweaker', tweakers.onEnter)
        .on('mouseleave.tweaker', tweakers.onLeave);

    const textAdjustment = determineLabelTextAdjustment(anchor);
    const iconAdjustment = determineLabelIconAdjustment(anchor);

    newLabels
        .append("text")
        .attr({
            'text-anchor': anchor,
            dx: textAdjustment
        })
        .text(app => _.truncate(app.name, { length: 26 }));

    labels
        .classed('wsat-hover', (d) => highlighted === d.id)
        .transition()
        .duration(animationDuration)
        .attr({
            'transform':  (d, i) => `translate(0, ${ scale(d.id) })`,
            opacity: 1
        });


    newLabels
        .append('text')
        .classed('wsat-icon',true)
        .attr({
            'dx': iconAdjustment,
            "font-family": "FontAwesome"
        });

    if (tweakers.icon) {
        labels
            .selectAll('.wsat-icon')
            .attr({ fill: d => tweakers.icon(d).color })
            .text((d) => tweakers.icon(d).code || '');
    }

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
            'marker-end': d => `url(#arrowhead-${d.rating})`,
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
    const fixFn = () => {
        if (selection) {
            selection.each(
                function() {
                    if (this.parentNode) {
                        this.parentNode.insertBefore(this, this);
                    }
                });
        }

    };

    setTimeout(fixFn, 200);
}


function drawTypeBoxes(section, model, scale, dimensions, tweakers) {

    section.on('click', tweakers.onSelect);

    const boxes = section
        .selectAll('.wsat-type-box')
        .data(model.types, d => d.id);

    const hasIncoming = (type) => _.some(model.sourceToType, f => f.to === type);
    const hasOutgoing = (type) => _.some(model.typeToTarget, f => f.from === type);

    boxes
        .enter()
        .append('rect')
        .classed('wsat-type-box', true)
        .attr({
            fill: '#fafafa',
            stroke: '#ccc',
            y: d => scale(d.id) - dimensions.height - 2,
            x: dimensions.width / 2 * -1 + 2,
            opacity: 0
        });


    boxes
        .transition()
        .duration(animationDuration)
        .attr({
            x: (d) => {
                const x = dimensions.width / 2 * -1 + 2;
                return hasIncoming(d.id)
                    ? x
                    : x + 20;
            },
            y: d => scale(d.id) - dimensions.height - 2,
            width: (d) => {
                const b = dimensions.width - 4;
                return b - (hasIncoming(d.id) ? 0 : 20) - (hasOutgoing(d.id) ? 0 : 20);
            },
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


function drawCenterBox(section, dimensions) {
    const centerBox = section
        .selectAll('.center-box')
        .data([1], _.identity);

    centerBox
        .enter()
        .append('rect')
        .classed('center-box', true)
        .attr({
            fill: '#f5f5f5',
            stroke: '#ddd'
        });

    centerBox
        .attr({
            x: -90,
            y: 15,
            width: 180,
            height: dimensions.graph.height - dimensions.margin.bottom - 6
        });
}


function update(sections,
                model,
                tweakers) {
    redraw = () => update(sections, model, tweakers);

    const dimensions = calculateDimensions(model);

    setupSizing(sections, dimensions);

    drawTitleBar(sections.header, dimensions);
    drawCenterBox(sections.types, dimensions);

    const scales = setupScales(model, dimensions);
    drawLabels(sections.sources, model.sources, scales.source, 'end', tweakers.source, redraw);
    drawLabels(sections.targets, model.targets, scales.target, 'start', tweakers.target, redraw);

    drawTypeBoxes(sections.types, model, scales.type, dimensions.label, tweakers.typeBlock);
    drawLabels(sections.types, model.types, scales.type, 'middle', tweakers.type, redraw);

    drawInbound(sections.inbound, model.sourceToType, scales, dimensions);
    drawOutbound(sections.outbound, model.typeToTarget, scales, dimensions);
}


/**
 * Note: it is v. important the $element is an element with some width,
 * simply placing this in a element like a waltz-section will cause it
 * to render with 0x0....
 * @param $element
 * @param $window
 * @param dataTypeService
 */
function controller($element, $window, dataTypeService) {

    const vm = initialiseData(this, initialState);
    const svg = d3.select($element.find('svg')[0]);

    const svgSections = prepareGraph(svg);

    const render = () => {

        if (! vm.entityRef) { return; }

        baseDimensions.graph.width = $element
            .parent()[0]
            .clientWidth;

        const tweakers = _.defaultsDeep(vm.tweakers, dfltTweakers);

        dataTypeService
            .loadDataTypes()
            .then(types => {
                const data = {
                    logicalFlows: vm.logicalFlows || [],
                    decorators: vm.decorators || [],
                    entityRef: vm.entityRef,
                    allTypes: types
                };
                const model = mkModel(data);
                update(svgSections, model, tweakers);
            });
    };

    const debouncedRender = _.debounce(render, 100);

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
