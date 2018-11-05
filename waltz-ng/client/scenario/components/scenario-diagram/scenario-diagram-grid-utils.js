import _ from "lodash";
import {CELL_DIMENSIONS, ROW_CELL_DIMENSIONS, ROW_DIMENSIONS} from "./scenario-diagram-dimensions";
import {drawRow, ROW_STYLES, rowLayout} from "./scenario-diagram-row-utils";
import {toCumulativeCounts} from "../../../common/list-utils";
import {defaultOptions} from "./scenario-diagram-utils";
import {d3ContextMenu} from "../../../common/d3-context-menu";


export const GRID_STYLES = {
    columnDivider: "wrd-column-divider",
    rowDivider: "wrd-row-divider",
    nodeGridBackground: "wrd-node-grid-background"
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
        .call(drawBackgroundCells, options, dataWithLayout)
        .call(drawRow, options, dataWithLayout.layout.colWidths);
}


function drawBackgroundCells(selection, options, dataWithLayout) {
    const dataProvider = (d, rowIdx) => _
        .map(dataWithLayout.columnHeaders,
            (col, colIdx) => ({
                id: `${rowIdx}.${colIdx}`,
                colOffset: dataWithLayout.layout.colOffsets[colIdx],
                colWidth: dataWithLayout.layout.colWidths[colIdx],
                column: col.data,
                row: dataWithLayout.rowHeaders[rowIdx].data,
                rowHeight: d.layout.maxCellRows
            }));

    const backgroundCells = selection
        .selectAll(`.${GRID_STYLES.nodeGridBackground}`)
        .data(dataProvider, d => d.id);

    const contextMenu = _.get(options, ["handlers", "contextMenus", "nodeGrid"], null);

    const newBackgroundCells = backgroundCells
        .enter()
        .append("rect")
        .classed(GRID_STYLES.nodeGridBackground, true)
        .attr("fill", "#fafafa")
        .attr("stroke", "#eee")
        .attr("stroke-width", 1.5)
        .on("click", options.handlers.onNodeGridClick)
        .on("contextmenu", contextMenu ?  d3ContextMenu(contextMenu) : null);

    newBackgroundCells
        .merge(backgroundCells)
        .attr("x", (d,i) => (d.colOffset * CELL_DIMENSIONS.width) + (i * ROW_CELL_DIMENSIONS.padding))
        .attr("width", (d, i) => d.colWidth * CELL_DIMENSIONS.width + CELL_DIMENSIONS.padding)
        .attr("height", (d, i) => d.rowHeight * CELL_DIMENSIONS.height + CELL_DIMENSIONS.padding);

    backgroundCells.exit()
        .remove();
}
