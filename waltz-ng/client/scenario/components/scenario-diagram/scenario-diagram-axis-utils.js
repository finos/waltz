import _ from "lodash";
import {CELL_DIMENSIONS, ROW_CELL_DIMENSIONS, ROW_DIMENSIONS} from "./scenario-diagram-dimensions";
import {NODE_DIMENSIONS} from "./scenario-diagram-static-node-utils";
import {truncate} from "../../../common/string-utils";
import {d3ContextMenu} from "../../../common/d3-context-menu";

export const rowAxisWidth = 150;
export const columnAxisHeight = 50;


const AXIS_STYLES = {
    header: "wrd-axis-header"
};


function drawRowHeaders(holder, headerData, layout, options) {
    const headers = holder
        .selectAll(`.${AXIS_STYLES.header}`)
        .data(headerData, d => d.id);

    const contextMenu = _.get(options, ["handlers", "contextMenus", "axisItem"], null);

    const newHeaderGroups = headers
        .enter()
        .append("g")
        .classed(AXIS_STYLES.header, true)
        .on("contextmenu", contextMenu ? d3ContextMenu(contextMenu) : null);

    newHeaderGroups
        .append("rect")
        .attr("fill", "none")
        .attr("width", 1000)
        .style("pointer-events", "visible");

    newHeaderGroups
        .append("text")
        .text(d => d.name);

    headers
        .exit()
        .remove();

    const allHeaderGroups = headers
        .merge(newHeaderGroups);

    allHeaderGroups
        .attr("transform", (d,i) => {
            const padding = ROW_DIMENSIONS.padding * i;
            const rowOffset = layout.rowOffsets[i] * CELL_DIMENSIONS.height;
            return `translate (0 ${rowOffset + padding})`;
        });

    allHeaderGroups
        .select("rect")
        .attr("height", (d,i) => {
            const padding = ROW_DIMENSIONS.padding / 2;
            const actualRowHeight = layout.rowHeights[i] * CELL_DIMENSIONS.height;
            return padding + actualRowHeight;
        });

    allHeaderGroups
        .select("text")
        .attr("y", (d, i) => {
            const actualRowHeight = layout.rowHeights[i] * CELL_DIMENSIONS.height;
            return actualRowHeight / 2 + NODE_DIMENSIONS.text.fontSize;
        });
}


function drawColumnHeaders(holder, headerData, layout, options) {
    const headers = holder
        .selectAll(`.${AXIS_STYLES.header}`)
        .data(headerData, d => d.id);


    const contextMenu = _.get(options, ["handlers", "contextMenus", "axisItem"], null);

    const newHeaderGroups = headers
        .enter()
        .append("g")
        .classed(AXIS_STYLES.header, true)
        .on("contextmenu", contextMenu ? d3ContextMenu(contextMenu) : null);

    newHeaderGroups
        .append("rect")
        .attr("fill", "none")
        .attr("height", 1000)
        .style("pointer-events", "visible");

    const newHeaders = newHeaderGroups
        .append("text");

    // tooltip
    newHeaders
        .filter(d => d.name.length > 4)
        .append("title")
        .text(d => d.name);

    headers
        .exit()
        .remove();

    const allHeaderGroups = headers
        .merge(newHeaderGroups);

    allHeaderGroups
        .attr("transform", (d,i) => {
            const padding = ROW_CELL_DIMENSIONS.padding * i;
            const columnOffset = layout.colOffsets[i] * CELL_DIMENSIONS.width;
            return `translate (${columnOffset + padding} 0)`;
        });

    allHeaderGroups
        .select("rect")
        .attr("width", (d,i) => {
            const padding = ROW_CELL_DIMENSIONS.padding / 2;
            const actualColWidth = layout.colWidths[i] * CELL_DIMENSIONS.width;
            return padding + actualColWidth;
        });

    allHeaderGroups
        .select("text")
        .attr("text-anchor", "middle")
        .attr("y", 16)
        .attr("x", (d, i) => {
            const actualColWidth = layout.colWidths[i] * CELL_DIMENSIONS.width;
            return actualColWidth / 2;
        })
        .text((d, i) => truncate(d.name, 25 * layout.colWidths[i], "..."));

}


export function drawAxis(columnAxisContent, rowAxisContent, dataWithLayout, options) {
    const layout = dataWithLayout.layout;
    drawRowHeaders(rowAxisContent, dataWithLayout.rowHeaders, layout, options);
    drawColumnHeaders(columnAxisContent, dataWithLayout.columnHeaders, layout, options);
}

