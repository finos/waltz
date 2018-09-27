import {toPath, truncateText} from "../../../common/d3-utils";


const nodeWidth = 100;
const nodeHeight = 54;
const nodeFontSize = 12;


export const NODE_DIMENSIONS = {
    width: nodeWidth,
    height: nodeHeight,  // 3 * section.height
    section: {
        height: nodeHeight / 3
    },
    text: {
        dy: 12,
        dx: 4,
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


export function drawUnit(selection, ratingColorScheme) {

    selection
        .call(drawUnitTitle)
        .call(drawUnitExternalId)
        .call(drawUnitChangeInitiative)
        .call(drawStateChangeIndicator, ratingColorScheme);

    selection
        .append("rect")
        .classed(NODE_STYLES.nodeCell, true)
        .attr("width", NODE_DIMENSIONS.width)
        .attr("height", NODE_DIMENSIONS.height)
        .attr("stroke", "#CBCBCB")
        .attr("fill", "none");

    selection
        .on("click", d => console.log("Click ", d));
}


function drawStateChangeIndicator(selection, ratingColorScale) {
    const padding = {
        x: 10,
        y: 4
    };

    const w = (NODE_DIMENSIONS.width / 2) - (padding.x * 2);
    const h = NODE_DIMENSIONS.section.height * 2 - padding.y * 2;

    const dx = padding.x;
    const dy = NODE_DIMENSIONS.section.height + padding.y;

    const section = selection
        .append("g")
        .classed(NODE_STYLES.nodeCellChangeIndicator, true)
        .attr("transform", `translate(${dx} ${dy})`);

    const baseStateShape = [
        {x: 0, y: h * 0.3},
        {x: w * 0.3, y: h * 0.15},
        {x: w * 0.4, y: h * 0.5 },
        {x: w * 0.3, y: h * 0.85},
        {x: 0, y: h * 0.7},
        {x: w * 0.02, y: h * 0.5}
    ];

    const targetStateShape = [
        {x: w * 0.3, y: h * 0.2},
        {x: w * 0.9, y: 0},
        {x: w, y: h * 0.5 },
        {x: w * 0.9, y: h},
        {x: w * 0.3, y: h * 0.8}
    ];

    section
        .append("path")
        .classed(NODE_STYLES.nodeCellChangeIndicatorTargetState, true)
        .attr("d", toPath(targetStateShape))
        .attr("fill", d => ratingColorScale(d.change.target.rating))
        .attr("stroke", "#959797");

    section
        .append("path")
        .classed(NODE_STYLES.nodeCellChangeIndicatorBaseState, true)
        .attr("d", toPath(baseStateShape))
        .attr("fill", d => ratingColorScale(d.change.base.rating))
        .attr("stroke", "#959797");
}


function drawUnitChangeInitiative(selection) {
    selection
        .append("rect")
        .classed(NODE_STYLES.nodeChangeInitiative, true)
        .attr("x", NODE_DIMENSIONS.width / 2)
        .attr("y", NODE_DIMENSIONS.section.height * 2)
        .attr("width", NODE_DIMENSIONS.width / 2)
        .attr("height", NODE_DIMENSIONS.section.height)
        .attr("fill", d => d.changeInitiative ? "#eee": "#ddd");

    selection
        .append("text")
        .classed(NODE_STYLES.nodeChangeInitiative, true)
        .text(d => _.get(d, ["changeInitiative","externalId"], "none"))
        .attr("x", NODE_DIMENSIONS.width / 2)
        .attr("y", NODE_DIMENSIONS.section.height * 2)
        .attr("dy", NODE_DIMENSIONS.text.dy)
        .attr("dx", NODE_DIMENSIONS.text.dx)
        .attr("font-size", NODE_DIMENSIONS.text.fontSize - 2)
        .call(truncateText, NODE_DIMENSIONS.width - (2 * NODE_DIMENSIONS.text.dx));
}


function drawUnitExternalId(selection) {
    selection
        .append("rect")
        .classed(NODE_STYLES.nodeExternalId, true)
        .attr("width", NODE_DIMENSIONS.width / 2)
        .attr("height", NODE_DIMENSIONS.section.height)
        .attr("x", NODE_DIMENSIONS.width / 2)
        .attr("y", NODE_DIMENSIONS.section.height)
        .attr("fill", "#eee");

    selection
        .append("text")
        .classed(NODE_STYLES.nodeExternalId, true)
        .text(d => d.node.externalId)
        .attr("x", NODE_DIMENSIONS.width / 2)
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
        .attr("fill", "#eee");

    selection
        .append("text")
        .text(d => d.node.name)
        .classed(NODE_STYLES.nodeTitle, true)
        .attr("dy", NODE_DIMENSIONS.text.dy)
        .attr("dx", NODE_DIMENSIONS.text.dx)
        .attr("font-size", NODE_DIMENSIONS.text.fontSize)
        .call(truncateText, NODE_DIMENSIONS.width - (2 * NODE_DIMENSIONS.text.dx));
}

