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
import {drawUnit, NODE_STYLES, updateUnit} from "./scenario-diagram-static-node-utils";
import {CELL_DIMENSIONS} from "./scenario-diagram-dimensions";
import {defaultOptions} from "./scenario-diagram-utils";
import {d3ContextMenu} from "../../../common/d3-context-menu";

const EMPTY_CELL_WIDTH = 0.5;
const EMPTY_CELL_HEIGHT = 0.5;


export function drawNodeGrid(selection, options) {
    const dataProvider = d => d.data;

    const cells = selection
        .selectAll(`g.${NODE_STYLES.nodeCell}`)
        .data(
            dataProvider,
            d => d.id);

    const contextMenu = _.get(options, ["handlers", "contextMenus", "node"], null);

    const newCells = cells
        .enter()
        .append("g")
        .classed(NODE_STYLES.nodeCell, true)
        .call(drawUnit, options)
        .on("contextmenu", contextMenu ? d3ContextMenu(contextMenu) : null);

    cells
        .exit()
        .remove();

    newCells
        .merge(cells)
        .attr("transform", d => {
            const dy = CELL_DIMENSIONS.padding + (CELL_DIMENSIONS.height * d.layout.row);
            const dx = CELL_DIMENSIONS.padding + (CELL_DIMENSIONS.width * d.layout.col);
            return `translate(${dx} ${dy})`;
        })
        .call(updateUnit, options);
}


/**
 * Given an _array_ of data will return an _object_ similar to:
 *
 * ```
 * {
 *     data: [ {
 *         ...datum,   // original data
 *         layout: { col: x, row: y }  // position within grid (zero offset)
 *     }],
 *     layout: {
 *       colCount: x,  // max number of cols (<= options.cols)
 *       rowCount: y   // max number rows
 *     }
 * }
 * ```
 *
 * @param data
 * @param coords - `{row: n, col: n}` used to generate unique id
 * @param options
 * @returns {{data: Array, layout: {colCount: number, rowCount: number}}}
 **/
export function nodeGridLayout(data = [], coords, options = defaultOptions) {
    // head is safe because the whole data array deals with the same domain coordinates
    const columnDomainId = _.get(_.head(data), ["domainCoordinates", "column", "id"]);
    const colWidth = _.get(options, ["colWidths", columnDomainId], 1);

    const dataWithLayout = _
        .chain(data)
        .orderBy(options.sortFn)
        .map((d, idx) => {
            const layout = {
                col: idx % colWidth,
                row: Math.floor(idx / colWidth)
            };
            return Object.assign({}, d, { layout });
        })
        .value();

    const layout = {
        colCount: Math.min(colWidth, data.length) || EMPTY_CELL_WIDTH,
        rowCount: Math.ceil(data.length / colWidth) || EMPTY_CELL_HEIGHT
    };

    return {
        id: `${coords.col},${coords.row}`,
        kind: "nodeGridLayout",
        data: dataWithLayout,
        layout
    };
}



