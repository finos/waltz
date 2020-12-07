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

import {initialiseData} from "../../../common";

import {event, select} from "d3-selection";
import {forceCenter, forceLink, forceManyBody, forceSimulation} from "d3-force";
import {drag} from "d3-drag";
import {symbol, symbolWye} from "d3-shape";
import {zoom, zoomIdentity} from "d3-zoom";
import "d3-selection-multi";
import _ from "lodash";
import {scalePow} from "d3-scale";

import {refToString} from "../../../common/entity-utils";

import template from "./boingy-graph.html";

const width = 1400; // viewbox width
const height = 800; // viewbox height
const DEFAULT_NODE_LIMIT = 500;


const bindings = {
    data: "<", // { decorators: [], entities: [], flows: [] }
    tweakers: "<",
    filterOptions: "<"

};

const initialState = {
    zoomEnabled: false,
    selectedNode: null,
    showFilters: false,
    showIsolated: false,
    showManyNodesWarning: false,
    overrideManyNodesWarning: false
};

const opacityScale = scalePow()
    .exponent(0.01)
    .range([0.4, 1])
    .clamp(true);

const nodeSizeScale = scalePow()
    .range([5, 10])
    .clamp(true);

const DEFAULT_TWEAKER = {
    enter: (selection) => selection,
    exit: (selection) => selection,
    update: (selection) => selection
};

const simulation = forceSimulation()
    .force("link", forceLink().id(d => d.id))
    .force("charge", forceManyBody()
        .strength(-200))
    .force("center", forceCenter(width / 2.5, height /2))
    .alphaTarget(0)
    .alphaDecay(0.08);

const actorSymbol = symbol()
    .size(128)
    .type(symbolWye);

let useStubs = false;

function mkLinkData(flows = []) {
    return _
        .chain(flows)
        .map(f => ({
            id: f.source.id + "_" + f.target.id,
            source: f.source.id,
            target: f.target.id,
            data: f
        }))
        .uniqBy(d => d.id)
        .value();
}


function addNodeLabel(selection) {
    return selection
        .append("text")
        .attr("dx", 9)
        .attr("dy", ".35em")
        .text(d => d.name);
}


function addNodeCircle(selection) {
    selection
        .filter(d => d.kind === "APPLICATION")
        .append("circle")
        .attr("class", "wdfd-glyph");

    selection
        .filter(d => d.kind === "ACTOR")
        .append("path")
        .attr("class", "wdfd-glyph")
        .attr("d", actorSymbol);

    return selection;
}


function setup(vizElem) {
    const svg = vizElem
        .append("svg")
        .style("min-height", "300px")
        .attr("preserveAspectRatio", "xMinYMin meet")
        .attr("viewBox", [0, 0, width, height])
        .attr("width", "100%")
        .attr("height", "800px")

    svg.append("defs")
        .append("marker")
        .attr("id", "wbg-arrowhead")
        .attr("viewBox", "-0 -5 10 10")
        .attr("refX", 18)
        .attr("refY", 0)
        .attr("orient", "auto")
        .attr("markerWidth", 5)
        .attr("markerHeight", 5)
        .append("svg:path")
        .attr("d", "M 0,-5 L 10 ,0 L 0,5")
        .attr("fill", "#999")
        .style("stroke","none");

    svg.append("g")
        .attr("class", "links");

    svg.append("g")
        .attr("class", "nodes");

    return svg;
}


function drawPartialEdges(
    selection,
    hoveredNode,
    useStubs = false)
{
    const lineLengthCoveredByNode = 5;
    return selection
        .attr("stroke-dasharray", d => {
            if (!useStubs) return "";

            if (hoveredNode  && (d.source.id === hoveredNode.id || d.target.id === hoveredNode.id)) {
                return "";
            }

            const dx = d.source.x - d.target.x;
            const dy = d.source.y - d.target.y;
            const hypotenuse = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
            const lineLength = hypotenuse - lineLengthCoveredByNode * 2;
            const stubLength = 16;

            if (lineLength < stubLength * 2) {
                return "";
            }

            return `${lineLengthCoveredByNode + stubLength} ${lineLength - stubLength * 2}`;
        });
}


function calcNeighborIds(linkData, focalNode) {
    return _
        .chain(linkData)
        .filter(x => x.source.id === focalNode.id || x.target.id === focalNode.id)
        .flatMap(x => [x.source.id, x.target.id])
        .uniq()
        .reject(x => x === focalNode.id)
        .value();
}


function draw(data = [],
              svg,
              tweakers = {},
              onSelectNode = () => {}) {

    let hoveredNode = null;
    let hoverNeighbors = [];

    const linkTweaker = _.defaults(tweakers.link, DEFAULT_TWEAKER);
    const nodeTweaker = _.defaults(tweakers.node, DEFAULT_TWEAKER);
    const linkData = mkLinkData(data.flows);
    const nodeData = data.entities;

    const linkSelection = drawLinks(linkData, svg.select(".links"));
    const nodeSelection = drawNodes(nodeData, svg.select(".nodes"));

    simulation
        .nodes(nodeData)
        .on("tick", ticked);

    const chargeStrengthScale = scalePow()
        .domain([0, 400])
        .range([-300, -100])
        .clamp(true);

    simulation
        .force("charge")
        .strength(chargeStrengthScale(nodeData.length));

    simulation
        .force("link")
        .links(linkData);


    function drawLinks(links = [], holder) {
        const linkSelection = holder
            .selectAll(".wdfd-link")
            .data(links, d => d.id);

        const newLinks = linkSelection
            .enter()
            .append("line")
            .classed("wdfd-link", true)
            .attr("stroke", "#444")
            .attr("marker-end","url(#wbg-arrowhead)")
            .call(linkTweaker.enter);

        linkSelection
            .exit()
            .remove();

        return linkSelection
            .merge(newLinks)
            .call(linkTweaker.update);
    }


    function drawNodes(nodes, holder) {
        function dragStarted(d) {
            if (!event.active) {
                simulation
                     .alpha(0.1)
                     .restart();
            }
            d.fx = d.x;
            d.fy = d.y;
        }

        function dragged(d) {
            d.fx = event.x;
            d.fy = event.y;
        }

        function dragEnded(d) {
            if (!event.active) {
                simulation
                    .restart();
            }
            d.fx = event.x;
            d.fy = event.y;
        }

        const nodeSelection = holder
            .selectAll(".wdfd-node")
            .data(nodes, d => d.id);

        const newNodes = nodeSelection
            .enter()
            .append("g")
            .classed("wdfd-node", true)
            .on("dblclick.unfix", d => { d.fx = null; d.fy = null })
            .on("click.node-selected", onSelectNode)
            .call(drag()
                .on("start", dragStarted)
                .on("drag", dragged)
                .on("end", dragEnded));

        newNodes
            .call(addNodeLabel)
            .call(addNodeCircle)
            .call(nodeTweaker.enter);

        nodeSelection
            .exit()
            .call(nodeTweaker.exit)
            .remove();

        const allNodes = nodeSelection
            .merge(newNodes);

        allNodes
            .select("text");

        allNodes
            .select("circle")
            .attr("r", d => nodeSizeScale(d.flowCount));

        allNodes
            .select("path")
            .attr("r", d => nodeSizeScale(d.flowCount));

        allNodes
            .on("mouseenter.opacityHover",  (d) => {
                hoveredNode = d;
                hoverNeighbors = calcNeighborIds(linkData, d);
                ticked();
            })
            .on("mouseout.opacityHover", (d) => {
                hoveredNode = null;
                hoverNeighbors = [];
                ticked();
            });

        return allNodes;
    }

    function ticked() {
        nodeSelection
            .attr("transform", d => "translate(" + d.x +", "+ d.y + ")")
            .each(function(d) {
                const isNeighbor = hoveredNode && _.includes(hoverNeighbors, d.id);
                const isFocus = hoveredNode && d.id === hoveredNode.id;
                const isNotFocus = hoveredNode && d.id !== hoveredNode.id;
                const isDefault = !hoveredNode;

                select(this)
                    .attr("opacity", () => {
                        if (isDefault) { return d.nodeOpacity; }
                        else if (isFocus) { return 1; }
                        else if (isNeighbor)  { return 0.7; }
                        else if (isNotFocus) { return 0.1; }
                    });

                select(this)
                    .select("text")
                    .style("font-weight", () => {
                        if (isDefault) { return 400; }
                        else if (isFocus) { return 700; }
                        else if (isNeighbor)  { return 400; }
                        else if (isNotFocus) { return 100; }
                    })
                    .style("font-size", () => {
                        if (isDefault) { return "9pt"; }
                        else if (isFocus) { return "12pt"; }
                        else if (isNeighbor)  { return "10pt"; }
                        else if (isNotFocus) { return "7pt"; }
                    });
            });

        nodeSelection
            .select("circle")
            .attr("r", d => {
                if (!hoveredNode) {
                    return d.nodeSize;
                } else {
                    return d.id === hoveredNode.id
                        ? d.nodeSize + 2
                        : d.nodeSize
                }
            });

        linkSelection
            .attr("x1", d => d.source.x)
            .attr("y1", d => d.source.y)
            .attr("x2", d => d.target.x)
            .attr("y2", d => d.target.y)
            .style("opacity", d => {
                if (! hoveredNode) {
                    return 0.6;
                } else {
                    return d.source.id === hoveredNode.id || d.target.id === hoveredNode.id
                        ? 1
                        : 0.15;
                }
            })
            .call(drawPartialEdges, hoveredNode, useStubs);
    }

    return ticked;
}


function enrichData(data = []) {
    const flows = data.flows;
    const flowCounts = _.chain(flows)
        .flatMap(f => [f.source, f.target])
        .countBy(refToString)
        .value();

    const maxFlowCount = _.max(_.values(flowCounts));

    opacityScale.domain([1, maxFlowCount]);
    nodeSizeScale.domain([1, maxFlowCount]);

    const enrichedEntities = _
        .chain(data.entities)
        .map(n => {
            const flowCount = flowCounts[refToString(n)];
            const nodeSize = nodeSizeScale(flowCount);
            const nodeOpacity = opacityScale(flowCount);
            return Object.assign({}, n, {
                flowCount,
                nodeSize,
                nodeOpacity,
                x: Math.random() * width * 0.66 + width / 3,
                y: Math.random() * height * 0.66 + height / 3
            });
        })
        .orderBy(d => d.flowCount)
        .value();

    return Object.assign(data, { entities: enrichedEntities });
}


function controller($timeout, $element) {
    const vm = initialiseData(this, initialState);

    let update = null;

    const vizElem = select($element[0])
        .select(".viz");

    const svg = setup(vizElem);

    const debouncedDraw = _.debounce((data) => {
        const tooManyNodes = !vm.overrideManyNodesWarning && data.entities.length > DEFAULT_NODE_LIMIT ;
        $timeout(() => vm.showManyNodesWarning = tooManyNodes);

        if (tooManyNodes) {
            update = draw({entities: [], flows: []}, svg);
        } else {
            const enrichedData = enrichData(data);
            update = draw(enrichedData, svg, vm.tweakers, onSelectNode);
            simulation.alpha(0.3).restart();
        }
    }, 250);

    vm.$onChanges = (changes) => {
        if (changes.data) {
            // we draw using async to prevent clientWidth reporting '0'
            $timeout(() => debouncedDraw(vm.data));
        }
    };

    vm.$onInit = () => {
        simulation.restart();
    };

    vm.$onDestroy = () => {
        simulation.stop();
    };

    function zoomed() {
        const svg = vizElem
            .select("svg");

        const t = event.transform;

        svg.select(".nodes")
            .attr("transform", t);
        svg.select(".links")
            .attr("transform", t);
    }

    const myZoom = zoom()
        .scaleExtent([1 / 4, 2])
        .on("zoom", zoomed);

    vm.enableZoom = () => {
        vizElem
            .select("svg")
            .call(myZoom);
        vm.zoomEnabled = true;
    };

    vm.disableZoom = () => {
        vizElem
            .select("svg")
            .on(".zoom", null);

        vm.zoomEnabled = false;
    };

    vm.disableStubs = () => {
        useStubs = false;
        update();
    };

    vm.enableStubs = () => {
        useStubs = true;
        update();
    };

    vm.stubsEnabled = () => useStubs;

    function unPinAll() {
        _.forEach(vm.data.entities, d => {
            d.fx = null;
            d.fy = null;
        });
    }

    vm.resetSimulation = () => {
        unPinAll();
        vm.showIsolated = false;

        vizElem
            .select("svg")
            .transition()
            .duration(750)
            .call(myZoom.transform, zoomIdentity);

        simulation.alpha(2);
        debouncedDraw(vm.data);
    };

    // not registered as a method on `vm` as will be invoked via d3 handler code...
    function onSelectNode(node) {
        $timeout(() => {
            vm.onHideFilters();
            if (vm.selectedNode === node) {
                vm.onDeselectNode();
            } else {
                vm.selectedNode = node;
            }
        }, 0);
    }

    vm.onDeselectNode = () => {
        vm.selectedNode = null;
    };

    vm.onHideFilters = () => {
        vm.showFilters = false;
        vm.filtersEnabled = false;
    };

    vm.onShowFilters = () => {
        vm.onDeselectNode();
        vm.showFilters = true;
        vm.filtersEnabled = true;
    };

    vm.onUndoIsolate = () => {
        vm.showIsolated = false;
        debouncedDraw(vm.data);
    };

    vm.onIsolate = () => {
        vm.showIsolated = true;
        unPinAll();
        const entitiesByRef = _.keyBy(vm.data.entities, refToString);

        const flowFilter = f => f.source.id === vm.selectedNode.id || f.target.id === vm.selectedNode.id;
        const flows = _.filter(vm.data.flows, flowFilter);
        const entities = _.chain(flows)
            .flatMap(f => [refToString(f.source), refToString(f.target)])
            .uniq()
            .map(r => entitiesByRef[r])
            .value();

        const isolatedData = {
            flows,
            entities,
            decorators: vm.data.decorators
        };

        simulation.alpha(0.2);
        debouncedDraw(isolatedData);
    };

    vm.onOverrideManyNodesWarning = () => {
        vm.overrideManyNodesWarning = true;
        debouncedDraw(vm.data);
    };
}


controller.$inject = ["$timeout", "$element"];


const component = {
    bindings,
    template,
    controller,
    transclude: {
        "filterControl": "?filterControl",
        "selectedControl": "?selectedControl"
    }
};


export default component;