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
import {scalePoint} from "d3-scale";
import {event, select} from "d3-selection";
import {color} from "d3-color";
import "d3-selection-multi";

import {initialiseData} from "../../../common";
import {mkLineWithArrowPath} from "../../../common/d3-utils";
import {CORE_API} from "../../../common/services/core-api-utils";

import template from "./source-and-target-graph.html"
import {amberHex} from "../../../common/colors";
import {sidebarExpanded} from "../../../navbar/sidebar-store";
import {getSymbol} from "../../../common/svg-icon";
import {flowDirection as FlowDirection} from "../../../common/services/enums/flow-direction";


const bindings = {
    flowClassifications: "<",
    changeUnits: "<",
    decorators: "<",
    entityRef: "<",
    logicalFlows: "<",
    ratingDirection: "<",
    tweakers: "<"
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


const styles = {
    ARC: "wsat-arc",
    ARC_PENDING: "wsat-arc-pending",
    ARC_REMOVED: "wsat-arc-removed",
    HOVER: "wsat-hover"
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

    const newTextLabels = textLabels
        .enter()
        .append("text")
        .text(d => d)
        .attr("text-anchor", "middle");

    textLabels
        .merge(newTextLabels)
        .attr("transform", (d, i) => {
            switch (i) {
                case 0: return `translate(${dimensions.label.width}, ${dy})`;
                case 1: return `translate(${dimensions.graph.width / 2 - 20}, ${dy})`;
                case 2: return `translate(${dimensions.graph.width - dimensions.label.width}, ${dy})`;
            }
        });

    const line = titleBar
        .selectAll("line")
        .data([true]);

    const newLine = line
        .enter()
        .append("line");

    line.merge(newLine)
        .attr("x1", 0)
        .attr("y1", dy + 10)
        .attr("x2", dimensions.graph.width - 40)
        .attr("y2", dimensions.margin.top / 2 + 10)
        .attr("stroke", "#ccc");
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


function mkModel({ logicalFlows = [], decorators = [], entityRef, allTypes = [], ratingDirection}) {
    const logicalFlowIds = _.map(logicalFlows, "id");
    const relevantDecorators = _.filter(
        decorators,
        d => _.includes(logicalFlowIds, d.dataFlowId));

    const { inbound = [], outbound = [] } = _.groupBy(
        logicalFlows,
        f => f.source.id === entityRef.id
            ? "outbound"
            : "inbound");

    const sources = _.chain(inbound)
        .map("source")
        .uniqBy("id")
        .value();

    const targets = _.chain(outbound)
        .map("target")
        .uniqBy("id")
        .value();

    const allTypesById = _.keyBy(allTypes, "id");

    const decoratorsByFlowId = _.chain(relevantDecorators)
        .filter(d => d.decoratorEntity.kind === "DATA_TYPE")
        .groupBy("dataFlowId")
        .value();

    const sourceToType = _.chain(inbound)
        .flatMap(f => _.map(
            decoratorsByFlowId[f.id] || [],
            d => {
                const rating = ratingDirection === FlowDirection.OUTBOUND.key ? d.rating : d.targetInboundRating;
                return { from: f.source.id, to: d.decoratorEntity.id, rating, entityLifecycleStatus: f.entityLifecycleStatus};
            }))
        .value();

    const typeToTarget = _.chain(outbound)
        .flatMap(f => _.map(
            decoratorsByFlowId[f.id] || [],
            d => {
                const rating = ratingDirection === FlowDirection.OUTBOUND.key ? d.rating : d.targetInboundRating;
                return { from: d.decoratorEntity.id, to: f.target.id, rating, entityLifecycleStatus: f.entityLifecycleStatus }
            }))
        .value();

    const types = _.chain(relevantDecorators)
        .map(d => d.decoratorEntity)
        .filter(d => d.kind === "DATA_TYPE")
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
        typeToTarget,
        entityRef
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
    sections.svg
        .attr("width", dimensions.graph.width)
        .attr("height", dimensions.graph.height);

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
    const range = getColumnScaleRange(dimensions);

    return scalePoint()
        .domain(_.chain(items)
            .sortBy(a => _.toLower(a.name))
            .map("id")
            .value())
        .range(range);
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
        case "start":
            return 20;
        case "end":
            return -20;
        default:
            return 0;
    }
}


function determineLabelIconAdjustment(anchor) {
    switch (anchor) {
        case "start":
            return 6;
        case "end":
            return -8;
        default:
            return 0;
    }
}


function determineLabelCUIconAdjustment(anchor) {
    switch (anchor) {
        case "start":
            return -12;
        case "end":
            return 10;
        default:
            return 0;
    }
}


function drawLabels(section, items = [], scale, anchor = "start", tweakers) {
    const labels = section
        .selectAll(".wsat-label")
        .data(items, d => d.id);

    const newLabels = labels
        .enter()
        .append("g")
        .classed("clickable", true)
        .classed("wsat-label", true)
        .attr("transform",  (d, i) => `translate(0, ${ scale(d.id) })`)
        .attr("opacity", 0);

    const textAdjustment = determineLabelTextAdjustment(anchor);
    const iconAdjustment = determineLabelIconAdjustment(anchor);
    const cuIconAdjustment = determineLabelCUIconAdjustment(anchor);

    newLabels
        .append("text")
        .attr("text-anchor", anchor)
        .text(d => _.truncate(d.name, {length: 26}))
        .attr("transform", `translate(${textAdjustment},0)`)
        .style("fill", d => d.deprecated ? amberHex : "inherit");

    if (tweakers.pfIcon) {
        newLabels
            .append("path")
            .classed("wsat-icon", true)
            .attr("d", d => tweakers.pfIcon(d).svgIcon)
            .attr("stroke", d => tweakers.pfIcon(d).color)
            .attr("transform", `translate(${iconAdjustment}, -6)`)
            .attr("fill", "none");
    }


    if (tweakers.cuIcon) {
        newLabels
            .append("path")
            .classed(".wsat-cuIcon", true)
            .attr("d", d => tweakers.cuIcon(d).svgIcon)
            .attr("stroke", d => tweakers.cuIcon(d).color)
            .attr("transform", `translate(${cuIconAdjustment}, -6)`)
            .attr("fill", "none");
    }

    labels
        .merge(newLabels)
        .classed("wsat-hover", (d) => highlighted === d.id)
        .on("mouseenter.highlight", d => {
            highlighted = d.id;
            redraw();
        })
        .on("mouseleave.highlight", d => {
            highlighted = null;
            redraw();
        })
        .on("click.tweaker", (d) => tweakers.onSelect(d, event))
        .on("mouseenter.tweaker", tweakers.onEnter)
        .on("mouseleave.tweaker", tweakers.onLeave)
        .transition()
        .duration(animationDuration)
        .attr("transform",  (d, i) => `translate(0, ${ scale(d.id) })`)
        .attr("opacity", 1);

    if (tweakers.pfIcon) {
        labels
            .merge(newLabels)
            .select(".wsat-icon")
            .append("path")
            .attr("d", d => tweakers.pfIcon(d).svgIcon)
            .attr("stroke", d => tweakers.pfIcon(d).color)
            .attr("fill", "none");
    }

    if(tweakers.cuIcon) {
        labels
            .merge(newLabels)
            .select(".wsat-cuIcon")
            .append("path")
            .attr("d", d => tweakers.cuIcon(d).svgIcon)
            .attr("stroke", d => tweakers.cuIcon(d).color)
            .attr("fill", "none");
    }

    labels
        .merge(newLabels)
        .select("title")
        .text((d) => {
            const iconDesc = tweakers.icon ? (tweakers.icon(d).description || "") : "";
            const cuIconDesc = tweakers.cuIcon ? (tweakers.cuIcon(d).description || "") : "";
            return `${iconDesc} \n${cuIconDesc}`;
        });

    labels
        .exit()
        .remove();
}


function drawArcs(section, model, layoutFn, flowClassificationsByCode, ratingDirection) {
    const arcs = section
        .selectAll(`.${styles.ARC}`)
        .data(model, d => d.from + "-" + d.to);

    const newArcs = arcs
        .enter()
        .append("path")
        .classed(styles.ARC, d => d.entityLifecycleStatus === "ACTIVE")
        .classed(styles.ARC_REMOVED, d => d.entityLifecycleStatus === "REMOVED")
        .classed(styles.ARC_PENDING, d => d.entityLifecycleStatus === "PENDING")
        .attr("opacity", 0)

    arcs
        .merge(newArcs)
        .classed(styles.HOVER, d => d.to === highlighted || d.from === highlighted)
        .attr("stroke", d => _.get(flowClassificationsByCode, [d.rating, "color"], "#fafafa"))
        .attr("fill", d => color(_.get(flowClassificationsByCode, [d.rating, "color"], "#fafafa")).brighter())
        .transition()
        .duration(animationDuration)
        .call(layoutFn)
        .attr("opacity", 1);

    arcs
        .exit()
        .remove();
}




function drawTypeBoxes(section, model, scale, dimensions, tweakers) {

    section.on("click", tweakers.onSelect);

    const boxes = section
        .selectAll(".wsat-type-box")
        .data(model.types, d => d.id);

    const hasIncoming = (type) => _.some(model.sourceToType, f => f.to === type);
    const hasOutgoing = (type) => _.some(model.typeToTarget, f => f.from === type);

    const newBoxes = boxes
        .enter()
        .append("rect")
        .classed("wsat-type-box", true)
        .attr("fill", "#fafafa")
        .attr("stroke", "#ccc")
        .attr("y", d => scale(d.id) - dimensions.height - 2)
        .attr("x", dimensions.width / 2 * -1 + 2)
        .attr("opacity", 0);

    newBoxes
        .filter(d => d.deprecated)
        .append("title")
        .text(d => "This type has been marked as deprecated and should not be used");

    boxes
        .merge(newBoxes)
        .transition()
        .duration(animationDuration)
        .attr("x", (d) => {
            const x = dimensions.width / 2 * -1 + 2;
            return hasIncoming(d.id)
                ? x
                : x + 20;
        })
        .attr("y", d => scale(d.id) - dimensions.height - 2)
        .attr("width", (d) => {
            const b = dimensions.width - 4;
            return b - (hasIncoming(d.id) ? 0 : 20) - (hasOutgoing(d.id) ? 0 : 20);
        })
        .attr("height", dimensions.height + 6)
        .attr("opacity", 1);

    boxes
        .exit()
        .remove();
}


function drawInbound(section, model, scales, dimensions, flowClassificationsByCode) {
    const inboundLayout = (selection) => selection
        .attr("d", d =>
            mkLineWithArrowPath(
                dimensions.margin.left + dimensions.label.width + 20,
                dimensions.margin.top + scales.source(d.from) - dimensions.label.height / 2,
                (dimensions.canvas.width / 2) - (dimensions.label.width / 2),
                dimensions.margin.top + scales.type(d.to) - dimensions.label.height / 2));

    drawArcs(section, model, inboundLayout, flowClassificationsByCode);
}


function drawOutbound(section, model, scales, dimensions, flowClassificationsByCode) {
    const outboundLayout = (selection) => selection
        .attr("d", d =>
            mkLineWithArrowPath(
                (dimensions.canvas.width / 2) + (dimensions.label.width / 2),
                dimensions.margin.top + scales.type(d.from) - dimensions.label.height / 2,
                dimensions.canvas.width - (dimensions.label.width + 10) - 15,
                dimensions.margin.top + scales.target(d.to) - dimensions.label.height / 2));

    drawArcs(section, model, outboundLayout, flowClassificationsByCode);
}


function drawCenterBox(section, dimensions, name = "") {
    const centerBox = section
        .selectAll(".center-box")
        .data([1], _.identity);

    const newCenterBox = centerBox
        .enter()
        .append("rect")
        .classed("center-box", true)
        .attr("fill", "#f5f5f5")
        .attr("stroke", "#ddd");

    centerBox
        .merge(newCenterBox)
        .attr("x", -90)
        .attr("y", 0)
        .attr("width", 180)
        .attr("height", dimensions.graph.height - dimensions.margin.bottom + 8);

    section.append("text")
        .attr("transform", "translate(0,16)")
        .attr("fill", "#888")
        .attr("text-anchor", "middle")
        .text(_.truncate(name, { length: 24 }))
}


function update(sections,
                model,
                tweakers,
                flowClassificationsByCode) {

    redraw = () => update(sections, model, tweakers, flowClassificationsByCode);

    const dimensions = calculateDimensions(model);

    setupSizing(sections, dimensions);

    drawTitleBar(sections.header, dimensions);
    drawCenterBox(sections.types, dimensions, model.entityRef.name);

    const scales = setupScales(model, dimensions);

    drawLabels(sections.sources, model.sources, scales.source, "end", tweakers.source);
    drawLabels(sections.targets, model.targets, scales.target, "start", tweakers.target);

    drawTypeBoxes(sections.types, model, scales.type, dimensions.label, tweakers.typeBlock);
    drawLabels(sections.types, model.types, scales.type, "middle", tweakers.type);

    drawInbound(sections.inbound, model.sourceToType, scales, dimensions, flowClassificationsByCode);
    drawOutbound(sections.outbound, model.typeToTarget, scales, dimensions, flowClassificationsByCode);
}


/**
 * Note: it is v. important the $element is an element with some width,
 * simply placing this in a element like a waltz-section will cause it
 * to render with 0x0...
 * @param $element
 * @param $window
 * @param serviceBroker
 */
function controller($element, $window, serviceBroker) {

    const vm = initialiseData(this, initialState);
    const svgDomElem = $element.find("svg")[0];
    const svg = select(svgDomElem);

    const svgSections = prepareGraph(svg);

    const render = () => {
        if (! vm.entityRef) { return; }

        baseDimensions.graph.width = $element
            .parent()[0]
            .clientWidth;

        const tweakers = _.defaultsDeep(vm.tweakers, dfltTweakers);

        vm.flowClassificationsByCode = _.chain(vm.flowClassifications)
            .filter(d => d.direction === vm.ratingDirection)
            .keyBy(d => d.code)
            .value();

        serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => {
                const types = r.data;
                const data = {
                    logicalFlows: vm.logicalFlows || [],
                    decorators: vm.decorators || [],
                    entityRef: vm.entityRef,
                    allTypes: types,
                    ratingDirection: vm.ratingDirection || FlowDirection.INBOUND.key
                };
                const model = mkModel(data);
                update(svgSections, model, tweakers, vm.flowClassificationsByCode);
            });
    };

    const debouncedRender = _.debounce(render, 100);

    vm.$onChanges = (changes) => {
        if(changes.changeUnits) {
            vm.changeUnitsByPhysicalFlowId = _.chain(vm.changeUnits)
                .filter(cu => cu.subjectEntity.kind === "PHYSICAL_FLOW")
                .keyBy(cu => cu.subjectEntity.id)
                .value();
        }

        debouncedRender();
    };


    let sidebarListenerUnregisterFn = null;


    vm.$onInit = () => {
        angular
            .element($window)
            .on("resize", debouncedRender);

        sidebarListenerUnregisterFn = sidebarExpanded.subscribe(debouncedRender);
    };

    vm.$onDestroy = () => {
        angular
            .element($window)
            .off("resize", debouncedRender);

        if (sidebarListenerUnregisterFn) {
            sidebarListenerUnregisterFn();
        }
    };
}


controller.$inject = [
    "$element",
    "$window",
    "ServiceBroker"
];


const component = {
    bindings,
    template,
    controller
};


const id = "waltzSourceAndTargetGraph";


export default {
    component,
    id
};