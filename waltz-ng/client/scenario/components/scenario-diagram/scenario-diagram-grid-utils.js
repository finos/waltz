import _ from "lodash";
import {CELL_DIMENSIONS, ROW_CELL_DIMENSIONS, ROW_DIMENSIONS} from "./scenario-diagram-dimensions";
import {drawRow, ROW_STYLES, rowLayout} from "./scenario-diagram-row-utils";
import {toCumulativeCounts} from "../../../common/list-utils";
import {defaultOptions} from "./scenario-diagram-utils";


export const GRID_STYLES = {
    columnDivider: "wrd-column-divider",
    rowDivider: "wrd-row-divider"
};


export function gridLayout(rowData = [],
                           columnHeaders = [],
                           rowHeaders = [],
                           options = defaultOptions) {
    const activeOptions = Object.assign({}, defaultOptions, options);

    const gridDataWithLayout = _.map(rowData, (row, i) => rowLayout(row, i, activeOptions));

    const rowHeights = _.map(gridDataWithLayout, d => d.layout.maxCellRows);

    const allColWidths = _.map(gridDataWithLayout, d => d.layout.colWidths);
    const transposed = _.unzip(allColWidths);
    const colWidths = _.map(transposed, _.max);

    const cumulativeColWidths = toCumulativeCounts(colWidths);
    const cumulativeRowHeights = toCumulativeCounts(rowHeights);
    const colOffsets = _.concat([0], cumulativeColWidths);
    const rowOffsets = _.concat([0], cumulativeRowHeights);

    const layout = {
        rowHeights,
        colWidths,
        colOffsets,
        rowOffsets,
        cumulativeColWidths,
        cumulativeRowHeights,
        totalHeight: _.sum(rowHeights),
        totalWidth: _.sum(colWidths),
        colCount: colWidths.length,
        rowCount: rowHeights.length
    };

    return {
        layout,
        gridData: gridDataWithLayout,
        columnHeaders,
        rowHeaders
    };
}


export function drawGrid(holder, dataWithLayout, options) {
    const rows = holder
        .selectAll(`.${ROW_STYLES.row}`)
        .data(dataWithLayout.gridData);

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
            const padding = i * ROW_DIMENSIONS.padding;
            const dy = rowOffset + padding;
            return `translate(0 ${ dy })`;
        })
        .call(drawBackgroundCells, dataWithLayout)
        .call(drawRow, options, dataWithLayout.layout.colWidths);
}


function drawBackgroundCells(selection, dataWithLayout) {
    const backgroundCells = selection
        .selectAll(".foo")
        .data((d, rowIdx) => _.map(
            dataWithLayout.columnHeaders,
            (c, colIdx) => ({
                id: `${rowIdx}.${colIdx}`,
                colOffset: dataWithLayout.layout.colOffsets[colIdx],
                colWidth: dataWithLayout.layout.colWidths[colIdx],
                rowHeight: d.layout.maxCellRows
            })), d => d.id);

    const newBackgroundCells = backgroundCells
        .enter()
        .append("rect")
        .classed("foo", true)
        .attr("fill", "#fafafa")
        .attr("stroke", "#eee")
        .attr("stroke-width", 1.5);

    newBackgroundCells
        .merge(backgroundCells)
        .attr("x", (d,i) => (d.colOffset * CELL_DIMENSIONS.width) + (i * ROW_CELL_DIMENSIONS.padding))
        .attr("width", (d, i) => d.colWidth * CELL_DIMENSIONS.width + CELL_DIMENSIONS.padding)
        .attr("height", (d, i) => d.rowHeight * CELL_DIMENSIONS.height + CELL_DIMENSIONS.padding);
}
