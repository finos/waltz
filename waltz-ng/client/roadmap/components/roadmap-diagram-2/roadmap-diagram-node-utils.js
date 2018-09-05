import {toPath, truncateText} from "../../../common/d3-utils";


export const NODE_STYLES = {
    node: "wrd-node",
    nodeTitle: "wrd-node-title",
    nodeExternalId: "wrd-node-external-id",
    nodeChangeInitiative: "wrd-node-change-initiative",
    nodeCell: "wrd-node-cell"
};


export const NODE_DIMENSIONS = {
    width: 120,
    height: 54,  // 3 * section.height
    section: {
        height: 18
    },
    text: {
        dy: 12,
        dx: 4,
        fontSize: 12
    }
};


export function drawUnit(selection, ratingColorScheme) {
    selection
        .append("rect")
        .classed(NODE_STYLES.nodeCell, true)
        .attr("width", NODE_DIMENSIONS.width)
        .attr("height", NODE_DIMENSIONS.height)
        .attr("fill", "#ffffff")
        .attr("stroke", "#888");

    selection
        .call(drawUnitTitle)
        .call(drawUnitExternalId)
        .call(drawUnitChangeInitiative)
        .call(drawStateChangeIndicator, ratingColorScheme)

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
        .append('g')
        .attr('transform', `translate(${dx} ${dy})`);

    const currentStateShape = [
        {x: 0, y: h * 0.3},
        {x: w * 0.3, y: h * 0.15},
        {x: w * 0.4, y: h * 0.5 },
        {x: w * 0.3, y: h * 0.85},
        {x: 0, y: h * 0.7},
        {x: w * 0.02, y: h * 0.5}
    ];

    const futureStateShape = [
        {x: w * 0.3, y: h * 0.2},
        {x: w * 0.9, y: 0},
        {x: w, y: h * 0.5 },
        {x: w * 0.9, y: h},
        {x: w * 0.3, y: h * 0.8}
    ];

    section
        .append('path')
        .attr('d', toPath(futureStateShape))
        .attr('fill', d => ratingColorScale(d.change.future.rating))
        .attr('stroke', '#959797');

    section
        .append('path')
        .attr('d', toPath(currentStateShape))
        .attr('fill', d => ratingColorScale(d.change.current.rating))
        .attr('stroke', '#959797');
}


function drawUnitChangeInitiative(selection) {
    selection
        .append("rect")
        .classed(NODE_STYLES.nodeChangeInitiative, true)
        .attr("x", NODE_DIMENSIONS.width / 2)
        .attr("y", NODE_DIMENSIONS.section.height * 2)
        .attr("width", NODE_DIMENSIONS.width / 2)
        .attr("height", NODE_DIMENSIONS.section.height)
        .attr("fill", "#ddd")
        .attr("stroke", "#888");

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
        .attr("fill", "#ddd")
        .attr("stroke", "#888");

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
        .attr("fill", "#eee")
        .attr("stroke", "#888");

    selection
        .append("text")
        .text(d => d.node.name)
        .classed(NODE_STYLES.nodeTitle, true)
        .attr("dy", NODE_DIMENSIONS.text.dy)
        .attr("dx", NODE_DIMENSIONS.text.dx)
        .attr("font-size", NODE_DIMENSIONS.text.fontSize)
        .call(truncateText, NODE_DIMENSIONS.width - (2 * NODE_DIMENSIONS.text.dx));
}

