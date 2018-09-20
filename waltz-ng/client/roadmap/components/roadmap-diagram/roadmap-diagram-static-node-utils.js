import {toPath, truncateText} from "../../../common/d3-utils";


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
    nodeCellChangeIndicator: "node-cell-change-indicator",
    nodeCellChangeIndicatorTargetState: "node-cell-change-indicator-target-state",
    nodeCellChangeIndicatorBaseState: "node-cell-change-indicator-base-state"
};


export function drawUnit(selection, ratingColorScale) {

    selection
        .append("rect")
        .classed(NODE_STYLES.nodeCell, true)
        .attr("width", NODE_DIMENSIONS.width)
        .attr("height", NODE_DIMENSIONS.height)
        .attr("stroke",  d => ratingColorScale(d.state.rating))
        .attr("fill", d => ratingColorScale(d.state.rating).brighter(2.5));

    selection
        .call(drawUnitTitle)
        .call(drawUnitExternalId)
        .call(drawRatingIndicator, ratingColorScale);

    selection
        .on("click", d => console.log("Click ", d));
}


function drawRatingIndicator(selection, ratingColorScale) {

    const w = (NODE_DIMENSIONS.width / 8);
    const h = NODE_DIMENSIONS.height;

    selection
        .append("rect")
        .classed(NODE_STYLES.nodeCellChangeIndicator, true)
        .attr("width", w)
        .attr("height", h)
        .attr("fill", d => ratingColorScale(d.state.rating))
        .attr("stroke",  d => ratingColorScale(d.state.rating));
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
        .attr("font-size", NODE_DIMENSIONS.text.fontSize - 2)
        .call(truncateText, NODE_DIMENSIONS.width - (2 * NODE_DIMENSIONS.text.dx));
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
        .text(d => d.node.name)
        .classed(NODE_STYLES.nodeTitle, true)
        .attr("dy", NODE_DIMENSIONS.text.dy)
        .attr("dx", NODE_DIMENSIONS.text.dx)
        .attr("font-size", NODE_DIMENSIONS.text.fontSize)
        .call(truncateText, NODE_DIMENSIONS.width - (2 * NODE_DIMENSIONS.text.dx));
}

