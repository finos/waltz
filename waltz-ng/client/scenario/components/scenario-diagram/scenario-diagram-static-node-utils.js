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
    nodeCellRatingIndicator: "node-cell-rating-indicator",
};



export function updateUnit(selection, options) {

    const colorScale = options.colorScale;

    selection
        .select(`rect.${NODE_STYLES.nodeCell}`)
        .attr("stroke",  d => colorScale(d.state.rating))
        .attr("fill", d => colorScale(d.state.rating).brighter(2.5));

    selection
        .select(`rect.${NODE_STYLES.nodeCellRatingIndicator}`)
        .attr("fill", d => colorScale(d.state.rating))
        .attr("stroke",  d => colorScale(d.state.rating));

    selection
        .select(`text.${NODE_STYLES.nodeTitle}`)
        .text(d => (d.state.comment ? "!!" : "") + d.node.name);
}


export function drawUnit(selection, options) {

    const colorScale = options.colorScale;

    selection
        .append("rect")
        .classed(NODE_STYLES.nodeCell, true)
        .attr("width", NODE_DIMENSIONS.width)
        .attr("height", NODE_DIMENSIONS.height);

    selection
        .call(drawUnitTitle)
        .call(drawUnitExternalId)
        .call(drawRatingIndicator, options);

    selection
        .on("click", options.handlers.onNodeClick);
}


function drawRatingIndicator(selection, options) {

    const colorScale = options.colorScale;
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
        .call(truncateText, NODE_DIMENSIONS.width - (2 * NODE_DIMENSIONS.text.dx));
}

