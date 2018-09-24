import {CELL_DIMENSIONS, ROW_CELL_DIMENSIONS, ROW_DIMENSIONS} from "./roadmap-diagram-dimensions";
import {NODE_DIMENSIONS} from "./roadmap-diagram-static-node-utils";

export const rowAxisWidth = 150;
export const columnAxisHeight = 50;


const AXIS_STLYES = {
    header: "wrd-axis-header"
};


function drawRowHeaders(holder, headerData, layout) {

    const headers = holder
        .selectAll(`.${AXIS_STLYES.header}`)
        .data(headerData, d => d.id);

    const newHeaders = headers
        .enter()
        .append("text")
        .text(d => d.name)
        .classed(AXIS_STLYES.header, true);

    headers
        .exit()
        .remove();

    headers
        .merge(newHeaders)
        .attr("y", (d, i) => {
            const padding = ROW_DIMENSIONS.padding * i;
            const rowOffset = layout.cumulativeRowHeights[i] * CELL_DIMENSIONS.height;
            const actualRowHeight = layout.rowHeights[i] * CELL_DIMENSIONS.height;
            return rowOffset + padding - actualRowHeight / 2 + NODE_DIMENSIONS.text.fontSize;
        });
}


function drawColumnHeaders(holder, headerData, layout) {

    const headers = holder
        .selectAll(`.${AXIS_STLYES.header}`)
        .data(headerData, d => d.id);

    const newHeaders = headers
        .enter()
        .append("text")
        .text(d => d.name)
        .classed(AXIS_STLYES.header, true);

    headers
        .exit()
        .remove();

    headers
        .merge(newHeaders)
        .attr("text-anchor", "middle")
        .attr("y", 16)
        .attr("x", (d, i) => {
            const padding = ROW_CELL_DIMENSIONS.padding * i;
            const columnOffset = layout.cumulativeColWidths[i] * CELL_DIMENSIONS.width;
            const actualColumnWidth = layout.colWidths[i] * CELL_DIMENSIONS.width;
            return columnOffset + padding - actualColumnWidth / 2;
        });
}




export function drawAxis(columnAxisContent, rowAxisContent, dataWithLayout) {
    const layout = dataWithLayout.layout;
    drawRowHeaders(rowAxisContent, dataWithLayout.rowHeaders, layout);
    drawColumnHeaders(columnAxisContent, dataWithLayout.columnHeaders, layout);
}

