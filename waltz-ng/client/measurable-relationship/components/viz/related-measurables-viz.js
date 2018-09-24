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

import _ from "lodash";
import {event, select} from "d3-selection";

import {determineCounterpart, sanitizeRelationships} from "../../measurable-relationship-utils";
import {initialiseData} from "../../../common";
import {stopPropagation} from "../../../common/browser-utils";
import {responsivefy} from "../../../common/d3-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import {easeLinear} from "d3-ease";
import {transition} from "d3-transition";

import template from "./related-measurables-viz.html";

/**
 * @name waltz-related-measurables-viz
 *
 * @description
 * This component shows a simple spider diagram which depicts the number of explicit connections
 * from the central measurable to other measurables and change initiatives.  Measurables
 * are split out into separate buckets based upon their category.
 */

const bindings = {
    parentEntityRef: "<",
    relationships: "<",
    onCategorySelect: "<",
    onCategoryClear: "<"
};


const initialState = {
    selectedCategoryId: null,
    categories: [],
    measurables: [],
    parentEntityRef: null,
    relationships: {},
    onCategorySelect: (c) => console.log("wrmv: default on-category-select", c),
    onCategoryClear: () => console.log("wrmv: default on-category-clear")
};


const dimensions = {
    width: 460,
    height: 460,
    centerNode: {
        r: 75
    },
    outerNode: {
        distanceFromCenter: 170,
        r: 45
    },
    bridge: {
        w: 2
    },
    nodeName: {
        dy: 1
    },
    nodeDescription: {
        dy: 16
    }
};

// initial angle, set to make label overlaps less likely
const ANGLE_OFFSET = -0.7;
const ANIMATION_DURATION = 300;

const TRANSITION = transition()
    .ease(easeLinear)
    .duration(ANIMATION_DURATION);

const styles = {
    centerNodes: "wrmv-center-nodes",
    centerNode: "wrmv-center-node",
    nodeName: "wrmv-name",
    nodeDetail: "wrmv-detail",
    outerNodes: "wrmv-outer-nodes",
    outerNode: "wrmv-outer-node",
    bridges: "wrmv-bridges",
    bridge: "wrmv-bridge",
    hasRelationships: "has-relationships",
    noRelationships: "no-relationships",
    selected: "wrmv-selected"
};


function prepareGroups(holder, onCategoryClear) {
    const svg = select(holder)
        .attr("width", dimensions.width)
        .attr("height", dimensions.height)
        .on("click", () => onCategoryClear());

    const bridges = svg.append("g").classed(styles.bridges, true);
    const centerNodes = svg.append("g").classed(styles.centerNodes, true);
    const outerNodes = svg.append("g").classed(styles.outerNodes, true);

    return {
        svg,
        centerNodes,
        bridges,
        outerNodes
    };
}


function calculatePositionOfOuterNode(angle) {
    const r = dimensions.outerNode.distanceFromCenter;
    const x = r * Math.cos(angle) + dimensions.width / 2;
    const y = r * Math.sin(angle) + dimensions.height / 2;
    return { x, y };
}


function drawCenterGroup(group, primaryEntity) {
    if (!group) return;
    const centerGroup = group
        .selectAll(`.${styles.centerNode}`)
        .data([primaryEntity], d => d.id)
        .enter()
        .append("g")
        .classed(styles.centerNode, true);

    centerGroup
        .append("circle")
        .attr("cx", dimensions.width / 2)
        .attr("cy", dimensions.height / 2)
        .attr("r", dimensions.centerNode.r)
        .attr("fill", "white")
        .attr("stroke", "red");

    centerGroup
        .append("text")
        .text(d => d.name)
        .classed(styles.nodeName, true)
        .attr("text-anchor", "middle")
        .attr("x", dimensions.width / 2)
        .attr("y", dimensions.height / 2)
        .attr("dy", dimensions.nodeName.dy);

    centerGroup
        .append("text")
        .text(d => d.category.name)
        .classed(styles.nodeDetail, true)
        .attr("text-anchor", "middle")
        .attr("x", dimensions.width / 2)
        .attr("y", dimensions.height / 2)
        .attr("dy", dimensions.nodeDescription.dy);
}


function drawOuterNodes(group, buckets = [], deltaAngle, handlers) {
    if (!group) return;

    const outerNodes = group
        .selectAll(`.${styles.outerNode}`)
        .data(buckets, d => d.id);


    // -- ENTER --
    const newOuterNodes = outerNodes
        .enter()
        .append("g")
        .classed(styles.outerNode, true)
        .on("click", d => {
            handlers.onCategorySelect(d);
            stopPropagation(event);
        })
        .attr("transform", `translate(${dimensions.width / 2}, ${dimensions.height / 2})`);

    newOuterNodes
        .append("circle")
        .attr("stroke", "#ccc")
        .attr("fill", "white");

    newOuterNodes
        .append("text")
        .classed(styles.nodeName, true)
        .text(d => d.name)
        .attr("text-anchor", "middle")
        .attr("dy", dimensions.nodeName.dy);

    newOuterNodes
        .append("text")
        .classed(styles.nodeDetail, true);


    // -- UPDATE --
    const allOuterNodes = newOuterNodes
        .merge(outerNodes);



    allOuterNodes
        .classed(styles.selected, d => d.isSelected)
        .classed(styles.hasRelationships, d => d.count > 0)
        .classed(styles.noRelationships, d => d.count === 0)
        .transition(TRANSITION)
        .attr("transform", (d, i) => {
            const { x, y } = calculatePositionOfOuterNode(deltaAngle(i));
            return `translate(${x}, ${y})`;
        });

    allOuterNodes
        .select("circle")
        .transition(TRANSITION)
        .attr("r", d => {
            const hasRelationships = d.count > 0;
            const scaleFactor = hasRelationships
                ? 1
                : 0.8;
            return dimensions.outerNode.r * scaleFactor;
        });

    allOuterNodes
        .select(`.${styles.nodeDetail}`)
        .text(d => d.count ? d.count : "-")
        .attr("text-anchor", "middle")
        .attr("dy", dimensions.nodeDescription.dy);


    // -- EXIT --
    outerNodes
        .exit()
        .remove();
}


function drawBridges(group, categories = [], deltaAngle) {
    if (!group) return;

    const bridges = group
        .selectAll(`.${styles.bridge}`)
        .data(categories, d => d.id);


    // -- ENTER
    const newBridges = bridges
        .enter()
        .append("line")
        .classed(styles.bridge, true)
        .attr("x1", dimensions.width / 2)
        .attr("y1", dimensions.height / 2)
        .attr("stroke", "#aaa")
        .attr("stroke-width", dimensions.bridge.w);


    // -- UPDATE --
    const allBridges = newBridges
        .merge(bridges);

    allBridges
        .each(function (d, i) {
            const { x, y } = calculatePositionOfOuterNode(deltaAngle(i));
            select(this)
                .attr("x2", x)
                .attr("y2", y);
        });


    // -- EXIT --
    bridges
        .exit()
        .remove();
}


function mkBuckets(categories = [], measurables = [], primaryEntity, relationships = [], selectedCategoryId) {

    if (! primaryEntity) return [];

    const measurablesByCategory = _.groupBy(measurables, m => m.categoryId);
    const measurablesById = _.keyBy(measurables, m => m.id);

    const counterparts = _.map(relationships, r => determineCounterpart(primaryEntity, r, { skipChecks: true }));

    const countsById = _.countBy(counterparts, c =>  {
        if (c.kind === "MEASURABLE") {
            const counterpartMeasurable = measurablesById[c.id];
            return "MEASURABLE_CATEGORY/"+counterpartMeasurable.categoryId;
        } else {
            return c.kind;
        }
    });

    const buckets = _
        .chain(categories)
        .map(c => {
            const relatedMeasurableIds = _.map(measurablesByCategory[c.id] || [], m => m.id);
            const filter = er => {
                const counterpart = determineCounterpart(primaryEntity, er, { skipChecks: true });
                return counterpart.kind === "MEASURABLE" && _.includes(relatedMeasurableIds, counterpart.id);
            };

            const id = "MEASURABLE_CATEGORY/"+c.id;

            return {
                id,
                ref: {
                    id: c.id,
                    kind: c.kind
                },
                name: c.name,
                relationshipFilter: filter,
                count: countsById[id] || 0,
                isSelected: selectedCategoryId === id
            };
        })
        .filter(b => b.count > 0)
        .orderBy("name")
        .value();

    buckets.push(mkChangeInitiativeBucket(primaryEntity, countsById, selectedCategoryId));
    buckets.push(mkAppGroupBucket(primaryEntity, countsById, selectedCategoryId));
    buckets.push(mkAddViewpointBucket(selectedCategoryId));

    return buckets;
}


function mkChangeInitiativeBucket(primaryEntity, countsById, selectedCategoryId) {
    const id = "CHANGE_INITIATIVE";
    return {
        id,
        name: "Change Initiative",
        relationshipFilter: er => id === determineCounterpart(primaryEntity, er).kind,
        count: countsById[id] || 0,
        isSelected: selectedCategoryId === id
    };
}


function mkAppGroupBucket(primaryEntity, countsById, selectedCategoryId) {
    const id = "APP_GROUP";
    return {
        id,
        name: "Application Group",
        relationshipFilter: er => id === determineCounterpart(primaryEntity, er).kind,
        count: countsById[id] || 0,
        isSelected: selectedCategoryId === id
    };
}


function mkAddViewpointBucket(selectedCategoryId) {
    const id = "MEASURABLE";
    return {
        id,
        name: "Add Viewpoint",
        relationshipFilter: () => false,
        count: null,
        isSelected: selectedCategoryId === id
    };
}


function draw(groups, data, handlers) {
    if (! groups) return;
    if (! data.primaryEntity) return;
    if (! data.categories) return;

    const buckets = mkBuckets(
        data.categories,
        data.measurables,
        data.primaryEntity,
        data.relationships,
        data.selectedCategoryId);

    const deltaAngle = i => i * (Math.PI * 2) / buckets.length + ANGLE_OFFSET;

    drawCenterGroup(groups.centerNodes, data.primaryEntity);
    drawOuterNodes(groups.outerNodes, buckets, deltaAngle, handlers);
    drawBridges(groups.bridges, buckets, deltaAngle);
}


function mkHandlers(vm, $timeout) {
    return {
        onCategoryClear: () => {
            vm.selectedCategoryId = null;
            vm.onCategoryClear();
            $timeout(() => vm.$onChanges(), 0);
        },
        onCategorySelect: c => {
            vm.selectedCategoryId = c.id;
            vm.onCategorySelect(c);
            $timeout(() => vm.$onChanges(), 0);
        }
    };
}


function mkPrimaryEntity(ref, measurables = [], categories = [], serviceBroker) {

    const kind = ref.kind;

    if (kind === "MEASURABLE") {
        const measurable = _.find(measurables, { id: ref.id });
        const category = _.find(categories, { id: measurable.categoryId });
        return Promise.resolve({
            id: ref.id,
            kind: ref.kind,
            name: measurable.name,
            category
        });
    } else if (kind === "CHANGE_INITIATIVE") {
        return serviceBroker
            .loadViewData(CORE_API.ChangeInitiativeStore.getById, [ref.id])
            .then(r => {
                return {
                    id: ref.id,
                    kind: ref.kind,
                    name: r.data.name,
                    category: {
                        name: "Change Initiative"
                    }
                };
            })
    } else if (kind === "APP_GROUP") {
        return serviceBroker
                .loadViewData(CORE_API.AppGroupStore.getById, [ref.id])
                .then(r => {
                    return {
                        id: ref.id,
                        kind: ref.kind,
                        name: r.data.appGroup.name,
                        category: {
                            name: "Application Group"
                        }
                    };
                })
    } else {
        return Promise.reject("Cannot handle kind: " + kind);
    }
}


function mkData(vm) {
    const data = {};
    data.categories = vm.categories || [];
    data.measurables = vm.measurables || [];
    data.relationships = sanitizeRelationships(vm.relationships || [], data.measurables, data.categories);
    data.primaryEntity = vm.primaryEntity || { name: "Loading", category: { name: "..." }, id: -1, kind: "ZZZ"};
    data.selectedCategoryId = vm.selectedCategoryId;
    return data;
}


function controller($element, $q, $timeout, serviceBroker) {
    const vm = this;

    const loadData = (force = false) => {
        const p1 = serviceBroker
            .loadAppData(CORE_API.MeasurableStore.findAll, [], { force })
            .then(r => vm.measurables = r.data);

        const p2 = serviceBroker
            .loadAppData(CORE_API.MeasurableCategoryStore.findAll, [], { force })
            .then(r => vm.categories = r.data);

        return $q.all([p1, p2])
            .then(() => mkPrimaryEntity(
                    vm.parentEntityRef,
                    vm.measurables,
                    vm.categories,
                    serviceBroker))
            .then(primaryEntity => vm.primaryEntity = primaryEntity);
    };

    let destroyResizeListener = () => {};
    let groups = {};

    vm.$onInit = () => {
        initialiseData(vm, initialState);
        const holder = $element.find("svg")[0];
        const handlers = mkHandlers(vm, $timeout);
        groups = prepareGroups(holder, handlers.onCategoryClear);
        destroyResizeListener = responsivefy(groups.svg);
        loadData()
            .then(() => draw(groups, mkData(vm), handlers));
    };

    vm.$onChanges = () => {
        draw(groups, mkData(vm), mkHandlers(vm, $timeout));
    };

    vm.$onDestroy = () => {
        destroyResizeListener();
    };
}


controller.$inject = [
    "$element",
    "$q",
    "$timeout",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default component;