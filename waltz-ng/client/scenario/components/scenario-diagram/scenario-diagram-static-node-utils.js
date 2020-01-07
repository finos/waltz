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

import {truncateText} from "../../../common/d3-utils";
import { determineForegroundColor } from "../../../common/colors";


const nodeWidth = 160;
const nodeHeight = 26;
const nodeFontSize = 12;


export const NODE_DIMENSIONS = {
    width: nodeWidth,
    height: nodeHeight,  // 3 * section.height
    section: {
        height: (nodeHeight - 2) / 2
    },
    text: {
        dy: 12,
        dx:nodeWidth / 8 + nodeWidth / 16,
        fontSize: nodeFontSize
    }
};


export const NODE_STYLES = {
    nodeTitle: "wrd-node-title",
    nodeExternalId: "wrd-node-external-id",
    nodeChangeInitiative: "wrd-node-change-initiative",
    nodeCell: "wrd-node-cell",
    nodeCellRatingIndicator: "node-cell-rating-indicator",
    nodeCellCommentIndicator: "node-cell-comment-indicator"
};


export function updateUnit(selection, options) {

    const colorScale = options.colorScale;

    selection
        .selectAll("text")
        .attr("fill", d => {
            const color = colorScale(d.state.rating).brighter(2);
            return determineForegroundColor(color.r, color.g, color.b);
        });

    selection
        .select(`rect.${NODE_STYLES.nodeCell}`)
        .attr("stroke",  d => colorScale(d.state.rating))
        .attr("fill", d => colorScale(d.state.rating).brighter(2));

    selection
        .select(`rect.${NODE_STYLES.nodeCellRatingIndicator}`)
        .attr("fill", d => colorScale(d.state.rating))
        .attr("stroke",  d => colorScale(d.state.rating));

    selection
        .select(`path.${NODE_STYLES.nodeCellCommentIndicator}`)
        .attr("fill", d => colorScale(d.state.rating).brighter(d.state.comment ? 0 : 2));
}


export function drawUnit(selection, options) {

    selection
        .append("rect")
        .classed(NODE_STYLES.nodeCell, true)
        .attr("width", NODE_DIMENSIONS.width)
        .attr("height", NODE_DIMENSIONS.height);

    selection
        .call(drawUnitTitle)
        .call(drawUnitExternalId)
        .call(drawRatingIndicator)
        .call(drawCommentIndicator)
        .call(drawRemovedIndicator);

    selection
        .on("click", options.handlers.onNodeClick);
}

function drawRemovedIndicator(selection) {
    selection
        .filter(d => d.node.entityLifecycleStatus === "REMOVED")
        .attr("opacity", 0.6)
        .append("line")
        .attr("x1", 0)
        .attr("x2", NODE_DIMENSIONS.width)
        .attr("y1", NODE_DIMENSIONS.height)
        .attr("y2", 0)
        .attr("stroke", "#c2462c")
        .attr("opacity", 0.4)
        .attr("stroke-width", 3);

}

function drawCommentIndicator(selection) {

    const w = NODE_DIMENSIONS.width / 12;
    const h = NODE_DIMENSIONS.height / 2;


    const trianglePath = `
        M${NODE_DIMENSIONS.width - w} ${NODE_DIMENSIONS.height}
        h ${w}
        v ${h * -1}
        Z `;

    selection
        .append("path")
        .attr("transform", "translate(-1 -1)")
        .classed(NODE_STYLES.nodeCellCommentIndicator, true)
        .attr("d", trianglePath);
}


function drawRatingIndicator(selection) {
    const w = (NODE_DIMENSIONS.width / 8);
    const h = NODE_DIMENSIONS.height;

    selection
        .append("rect")
        .classed(NODE_STYLES.nodeCellRatingIndicator, true)
        .attr("width", w)
        .attr("height", h);
}


function drawUnitExternalId(selection) {
    selection
        .append("rect")
        .classed(NODE_STYLES.nodeExternalId, true)
        .attr("width", NODE_DIMENSIONS.width)
        .attr("height", NODE_DIMENSIONS.section.height)
        .attr("y", NODE_DIMENSIONS.section.height)
        .attr("fill", "none");

    selection
        .append("text")
        .classed(NODE_STYLES.nodeExternalId, true)
        .text(d => d.node.externalId)
        .attr("y", NODE_DIMENSIONS.section.height)
        .attr("dy", NODE_DIMENSIONS.text.dy)
        .attr("dx", NODE_DIMENSIONS.text.dx)
        .attr("font-size", NODE_DIMENSIONS.text.fontSize - 2);
}


function drawUnitTitle(selection) {
    selection
        .append("rect")
        .classed(NODE_STYLES.nodeTitle, true)
        .attr("width", NODE_DIMENSIONS.width)
        .attr("height", NODE_DIMENSIONS.section.height)
        .attr("fill", "none");

    selection
        .append("text")
        .classed(NODE_STYLES.nodeTitle, true)
        .attr("dy", NODE_DIMENSIONS.text.dy)
        .attr("dx", NODE_DIMENSIONS.text.dx)
        .attr("font-size", NODE_DIMENSIONS.text.fontSize)
        .text(d => d.node.name)
        .call(truncateText, NODE_DIMENSIONS.width - (2 * NODE_DIMENSIONS.text.dx));
}

