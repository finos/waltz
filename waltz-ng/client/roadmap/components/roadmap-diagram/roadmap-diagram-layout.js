/*
 * Waltz - Enterprise Architecture
 *  Copyright (C) 2016, 2017 Waltz open source project
 *  See README.md for more information
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import _ from "lodash";
import {ROADMAP_LAYOUT_OPTIONS} from "./roadmap-diagram-options";


function calcRowGroupLayout(rowGroupDefs = [], totalWidth) {
    const headingPadding = ROADMAP_LAYOUT_OPTIONS.column.label.height * ROADMAP_LAYOUT_OPTIONS.column.padding;
    let yPtr = ROADMAP_LAYOUT_OPTIONS.column.label.height + (0.5 * headingPadding);

    return _.map(
        rowGroupDefs,
        rg => {
            const numRows = rg.rows.length;
            const height = numRows
                ? numRows * ROADMAP_LAYOUT_OPTIONS.row.height
                : ROADMAP_LAYOUT_OPTIONS.rowGroup.emptyHeight;
            const layout = {
                x: 0,
                y: yPtr,
                width: totalWidth,
                height
            };
            const updatedGroup = Object.assign({}, rg, { layout });
            yPtr += height;
            return updatedGroup;
        });
}


function calcColLayouts(columnDefs = [], totalHeight) {
    return _.map(
        columnDefs,
        (c, idx) => {
            const oc = ROADMAP_LAYOUT_OPTIONS.column;
            const columnPadding = oc.width * oc.padding;
            const totalColumnWidth = oc.width + columnPadding;
            const layout = {
                x: (idx * totalColumnWidth) + (0.5 * columnPadding),
                y: 0,
                height: totalHeight,
                width: oc.width
            };
            return Object.assign({}, c, {layout});
        });
}


export function calcLayout(gridDef) {
    const totalWidth = gridDef.columns.length * (ROADMAP_LAYOUT_OPTIONS.column.width * (1 + ROADMAP_LAYOUT_OPTIONS.column.padding));
    const rowGroups = calcRowGroupLayout(gridDef.rowGroups, totalWidth);
    const totalHeight = _.sumBy(rowGroups, rg => rg.layout.height);
    const columns = calcColLayouts(gridDef.columns, totalHeight);
    return { columns, rowGroups };
}