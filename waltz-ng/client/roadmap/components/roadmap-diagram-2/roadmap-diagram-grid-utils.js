import _ from "lodash";
import {CELL_DIMENSIONS, ROW_CELL_DIMENSIONS, ROW_DIMENSIONS} from "./roadmap-diagram-dimensions";
import {drawNodeGrid, nodeGridLayout} from "./roadmap-diagram-node-grid-utils";
import {drawRow, ROW_STYLES} from "./roadmap-diagram-row-utils";
import {toCumulativeCounts} from "../../../common/list-utils";


export const GRID_STYLES = {
    columnDivider: "wrd-column-divider",
    rowDivider: "wrd-row-divider"
};


export function gridLayout(rowData = [], options) {
    const dataWithLayout = _.map(rowData, row => rowLayout(row, options));

    const rowHeights = _.map(dataWithLayout, d => d.layout.maxCellRows);

    const allColWidths = _.map(dataWithLayout, d => d.layout.colWidths);
    const transposed = _.unzip(allColWidths);
    const colWidths = _.map(transposed, _.max);

    const cumulativeColWidths = toCumulativeCounts(colWidths);
    const cumulativeRowHeights = toCumulativeCounts(rowHeights);

    return {
        layout: {
            rowHeights,
            colWidths,
            cumulativeColWidths,
            cumulativeRowHeights,
            totalHeight: _.sum(rowHeights),
            totalWidth: _.sum(colWidths)
        },
        data: dataWithLayout
    };
}


export function drawGrid(holder, dataWithLayout, colorScheme) {
    const rows = holder
        .selectAll(`.${ROW_STYLES.row}`)
        .data(dataWithLayout.data);

    const newRows = rows
        .enter()
        .append("g")
        .classed(ROW_STYLES.row, true);

    rows.exit()
        .remove();

    rows
        .merge(newRows)
        .attr("transform", (d, i) => {
            const rowOffset = _.sum(_.take(dataWithLayout.layout.rowHeights, i)) * CELL_DIMENSIONS.height;
            const padding = (i * ROW_DIMENSIONS.padding);
            const dy = rowOffset + padding;
            return `translate(0 ${ dy })`;
        })
        .call(drawRow, colorScheme, dataWithLayout.layout.colWidths);

    drawRowDividers(holder, dataWithLayout.layout);
    drawColumnDividers(holder, dataWithLayout.layout);
}


export function drawColumnDividers(selection, layout) {

    const colDividers = selection
        .selectAll(`.${GRID_STYLES.columnDivider}`)
        .data(layout.cumulativeColWidths);

    const newColDividers = colDividers
        .enter()
        .append("line")
        .classed(GRID_STYLES.columnDivider, true)
        .attr("stroke", "#ddd")
        .attr("stroke-width", 2);

    colDividers
        .exit()
        .remove();

    const dividerHeight = (layout.totalHeight * CELL_DIMENSIONS.height) + (layout.rowHeights.length * ROW_DIMENSIONS.padding);

    const calcX = (d, i) => d * CELL_DIMENSIONS.width + (i * ROW_CELL_DIMENSIONS.padding) + ROW_DIMENSIONS.padding / 2;

    colDividers
        .merge(newColDividers)
        .attr("x1", calcX)
        .attr("x2", calcX)
        .attr("y1", 0)
        .attr("y2", dividerHeight);
}


export function drawRowDividers(selection, layout) {

    const dividers = selection
        .selectAll(`.${GRID_STYLES.rowDivider}`)
        .data(layout.rowHeights);

    const newDividers = dividers
        .enter()
        .append("line")
        .classed(GRID_STYLES.rowDivider, true)
        .attr("x1", 0)
        .attr("stroke", "#eee")
        .attr("stroke-width", 2);

    const calcY = (d, i) => layout.cumulativeRowHeights[i] * CELL_DIMENSIONS.height + (i * ROW_DIMENSIONS.padding) + ROW_DIMENSIONS.padding / 2;

    dividers
        .merge(newDividers)
        .attr("x2", _.sum(layout.colWidths) * CELL_DIMENSIONS.width)
        .attr("y1", calcY)
        .attr("y2", calcY);

}





/**
 * Given an _array of arrays_ (row-cells containing node-cells)
 * will lay the node-cells into grids and return a new _object_
 * which looks like:
 *
 * ```
 * {
 *     layout: {
 *         maxCellRows:  n,  // largest number of node cell rows  (gives height of row)
 *         maxCellCols:  n   // where n <= options.cols
 *     },
 *     data: originalDataEnrichedWithLayout
 * }
 * ```
 * @param data
 * @returns {{layout: {maxCellRows: *, maxCellCols: *}, data: *}}
 */
export function rowLayout(data = [], options = { cols: 3 }) {
    const gridData = _.map(data, d => nodeGridLayout(d, options));

    const maxCellRows = _
        .chain(gridData)
        .map(d => d.layout.rowCount)
        .max()
        .value();

    const colWidths = _
        .chain(gridData)
        .map(d => d.layout.colCount)
        .value();

    return {
        layout: {
            maxCellRows,
            colWidths
        },
        data: gridData
    };
}
