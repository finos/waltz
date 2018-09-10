import _ from "lodash";
import {CELL_DIMENSIONS, ROW_CELL_DIMENSIONS} from "./roadmap-diagram-dimensions";
import {drawNodeGrid, gridLayout} from "./roadmap-diagram-node-grid-utils";


export const ROW_STYLES = {
    row: "wrd-row",
    rowCell: "wrd-row-cell"
};


export function drawRow(selection, ratingColorScheme, colWidths = []) {
    const rows = selection
        .selectAll(`.${ROW_STYLES.rowCell}`)
        .data(d => d.data);

    rows.exit()
        .remove();

    const newRows = rows
        .enter()
        .append("g")
        .classed(ROW_STYLES.rowCell, true);

    rows.merge(newRows)
        .attr("transform", (d,i) => {
            const colOffset = _.sum(_.take(colWidths, i));
            const dx = (colOffset || i * 3) * CELL_DIMENSIONS.width + (i * ROW_CELL_DIMENSIONS.padding);
            return `translate(${dx} 0)`
        })
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
