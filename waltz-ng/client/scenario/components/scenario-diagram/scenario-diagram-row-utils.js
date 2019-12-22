/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

import _ from "lodash";
import {CELL_DIMENSIONS, ROW_CELL_DIMENSIONS} from "./scenario-diagram-dimensions";
import {drawNodeGrid, nodeGridLayout} from "./scenario-diagram-node-grid-utils";
import {defaultOptions} from "./scenario-diagram-utils";


export const ROW_STYLES = {
    row: "wrd-row",
    rowCell: "wrd-row-cell"
};


export function drawRow(selection, options, colWidths = []) {
    const dataProvider = d => d.data;

    const rows = selection
        .selectAll(`g.${ROW_STYLES.rowCell}`)
        .data(
            dataProvider,
            d => d.id);

    rows.exit()
        .remove();

    const newRows = rows
        .enter()
        .append("g")
        .classed(ROW_STYLES.rowCell, true);

    newRows
        .merge(rows)
        .attr("transform", (d, i) => {
            const colOffset = _.sum(_.take(colWidths, i));
            const dx = (colOffset * CELL_DIMENSIONS.width) + (i * ROW_CELL_DIMENSIONS.padding);
            return `translate(${dx} 0)`;
        })
        .call(drawNodeGrid, options);
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
 * @param rowIdx - row index, used to create ids
 * @param options
 * @returns {{layout: {maxCellRows: *, maxCellCols: *}, data: *}}
 */
export function rowLayout(data = [], rowIdx, options = defaultOptions) {
    const gridData = _.map(data, (d, i) => nodeGridLayout(d, { row: rowIdx, col: i }, options));

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
        type: "rowLayout",
        id: rowIdx,
        layout: {
            maxCellRows,
            colWidths
        },
        data: gridData
    };
}
