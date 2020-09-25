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

import {lineWithArrowPath, responsivefy} from "../../../common/d3-utils";
import {event, select} from "d3-selection";
import {forceLink, forceManyBody, forceSimulation, forceX, forceY} from "d3-force";
import {drag} from "d3-drag";
import {symbol, symbolWye} from "d3-shape";
import {zoom, zoomIdentity} from "d3-zoom";
import "d3-selection-multi";
import _ from "lodash";
import {scalePow} from "d3-scale";

import {refToString} from "../../../common/entity-utils";

import template from "./boingy-graph.html";

const width = 900;
const height = 600;
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
    .force("charge", forceManyBody().strength(-110).distanceMin(1).distanceMax(400))
    .force("x", forceX())
    .force("y", forceY())
    .alphaTarget(0);

const actorSymbol = symbol()
    .size(128)
    .type(symbolWye);


function mkLinkData(flows = []) {
    const linkData = _
        .chain(flows)
        .map(f => ({
            id: f.source.id + "_" + f.target.id,
            source: f.source.id,
            target: f.target.id,
            data: f
        }))
        .uniqBy(d => d.id)
        .value();
    return linkData;
}


function drawLinks(links = [], holder, tweakers) {
    const linkSelection = holder
        .selectAll(".wdfd-link")
        .data(links, d => d.id);

    const newLinks = linkSelection
        .enter()
        .append("path")
        .classed("wdfd-link", true)
        .attr("stroke", "#444")
        .attr("stroke-opacity", 0.6)
        .call(tweakers.enter);

    linkSelection
        .exit()
        .remove();

    return linkSelection
        .merge(newLinks)
        .call(tweakers.update);
}


function addNodeLabel(selection) {
    selection
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
}


function drawNodes(nodes,
                   holder,
                   tweakers = DEFAULT_TWEAKER,
                   onSelectNode) {

    function dragStarted(d) {
        if (!event.active) {
            simulation
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
        .call(tweakers.enter);

    nodeSelection
        .exit()
        .call(tweakers.exit)
        .remove();

    const allNodes = nodeSelection
        .merge(newNodes);

    allNodes
        .select("text")
        .attr("opacity", d => opacityScale(d.flowCount));

    allNodes
        .select("circle")
        .attr("opacity", d => opacityScale(d.flowCount))
        .attr("r", d => nodeSizeScale(d.flowCount));

    allNodes
        .select("path")
        .attr("opacity", d => opacityScale(d.flowCount))
        .attr("r", d => nodeSizeScale(d.flowCount));

    const setOpacity = (selection, opacity) => {
        selection
            .selectAll("text, circle, path")
            .attr("opacity", opacity);
        return selection;
    };

    allNodes
        .on("mouseenter.opacityHover", function (d) {
            const selection = select(this);
            setOpacity(selection, 1);
            selection
                .select("circle")
                .attr("r", nodeSizeScale(d.flowCount) + 2);
            return selection;
        })
        .on("mouseout.opacityHover", function (d) {
            const selection = select(this);
            setOpacity(selection, opacityScale(d.flowCount));
            selection
                .select("circle")
                .attr("r", nodeSizeScale(d.flowCount));
            return selection;
        });

    return allNodes;
}


function setup(vizElem) {
    const svg = vizElem
        .append("svg")
        .attr("width", width)
        .attr("height", height)
        .attr("viewBox", [-width / 2, -height / 2, width, height]);

    const destroyResizeListener = responsivefy(svg);

    svg.append("g")
        .attr("class", "links");

    svg.append("g")
        .attr("class", "nodes");

    return { svg, destroyResizeListener };
}


function draw(data = [],
              parts,
              tweakers = {},
              onSelectNode = () => {}) {

    const linkTweakers = _.defaults(tweakers.link, DEFAULT_TWEAKER);
    const nodeTweakers = _.defaults(tweakers.node, DEFAULT_TWEAKER);

    const links = mkLinkData(data.flows);
    const nodes = data.entities;

    const linkSelection = drawLinks(
        links,
        parts.svg.select(".links"),
        linkTweakers);

    const nodeSelection = drawNodes(
        nodes,
        parts.svg.select(".nodes"),
        nodeTweakers,
        onSelectNode);

    const ticked = () => {
        nodeSelection
            .attr("transform", d => `translate(${d.x}, ${d.y})`);

        linkSelection
            .call(lineWithArrowPath);
    };

    simulation
        .nodes(nodes)
        .on("tick", ticked);

    simulation
        .force("link")
        .links(links);

    return simulation;
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
        .map(n => Object.assign(n, { flowCount: flowCounts[refToString(n)]}))
        .orderBy(d => d.flowCount)
        .value();

    return Object.assign(data, { entities: enrichedEntities });
}


function controller($timeout, $element) {
    const vm = initialiseData(this, initialState);

    const vizElem = select($element[0])
        .select(".viz");

    const parts = setup(vizElem);

    const debouncedDraw = _.debounce((data) => {
        const tooManyNodes = !vm.overrideManyNodesWarning && data.entities.length > DEFAULT_NODE_LIMIT ;
        $timeout(() => vm.showManyNodesWarning = tooManyNodes);

        if (tooManyNodes) {
            draw({entities: [], flows: []}, parts);
        } else {
            const enrichedData = enrichData(data);
            draw(enrichedData, parts, vm.tweakers, onSelectNode);
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
        parts.destroyResizeListener();
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

        simulation.alpha(1);
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