import _ from "lodash";
import {CELL_DIMENSIONS} from "./roadmap-diagram-dimensions";
import {drawNodeGrid, gridLayout} from "./roadmap-diagram-node-grid-utils";


const STYLES = {
    rowCell: "wrd-row-cell"
};


export function drawRow(rowData, holder, ratingColorScheme) {
    const rows = holder
        .selectAll(`.${STYLES.rowCell}`)
        .data(rowData.data);

    rows.exit()
        .remove();

    const newRows = rows
        .enter()
        .append("g")
        .classed(STYLES.rowCell, true);

    const rowHeight = rowData.layout.maxCellRows * CELL_DIMENSIONS.height + CELL_DIMENSIONS.padding;

    newRows
        .filter((d, i) => i > 0)
        .append("line")
        .attr("y1", 0)
        .attr("y2", rowHeight)
        .attr("stroke", "red");

    rows.merge(newRows)
        .attr("transform", (d,i) => `translate(${400 * i} 0)`) // TODO:calc widths
        .call(drawNodeGrid, ratingColorScheme);
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

    const maxCellCols = _
        .chain(gridData)
        .map(d => d.layout.colCount)
        .max()
        .value();

    return {
        layout: {
            maxCellRows,
            maxCellCols
        },
        data: gridData
    };
}
