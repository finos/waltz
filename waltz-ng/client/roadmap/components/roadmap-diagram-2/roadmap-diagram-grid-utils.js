import _ from "lodash";
import {CELL_DIMENSIONS, ROW_CELL_DIMENSIONS, ROW_DIMENSIONS} from "./roadmap-diagram-dimensions";
import {drawNodeGrid, gridLayout} from "./roadmap-diagram-node-grid-utils";


export const GRID_STYLES = {
    columnDivider: "wrd-column-divider",
    rowDivider: "wrd-row-divider"
};



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
        .attr("stroke", "#eee");

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
    const gridData = _.map(data, d => gridLayout(d, options));

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
