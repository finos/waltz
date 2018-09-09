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


export function rowLayout(data = []) {
    const gridData = _.map(data, d => gridLayout(d));

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
