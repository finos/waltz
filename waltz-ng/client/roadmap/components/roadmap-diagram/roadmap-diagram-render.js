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
import {calcLayout} from "./roadmap-diagram-layout";
import {ROADMAP_LAYOUT_OPTIONS} from "./roadmap-diagram-options";


const STYLES = {
    columns: "wrd-col",
    columnLabel: "wrd-column-label",
    rowGroup: "wrd-row-group"
};


function drawRowGroups(svgGroups, gridDefWithLayout) {
    const rowGroupSelector = svgGroups
        .rowGroups
        .selectAll(`.${STYLES.rowGroup}`)
        .data(gridDefWithLayout.rowGroups, (d) => d.id);

    const newGroups = rowGroupSelector
        .enter()
        .append("g")
        .classed(STYLES.rowGroup, true);

    newGroups
        .append('rect');

    newGroups
        .append('text')
        .attr('dy', ROADMAP_LAYOUT_OPTIONS.rowGroup.label.dy)
        .attr('dx', ROADMAP_LAYOUT_OPTIONS.rowGroup.label.dx)
        .text(d => d.datum.name);

    rowGroupSelector
        .merge(newGroups)
        .attr('transform', d => `translate(${d.layout.x}, ${d.layout.y})`)
        .select('rect')
        .attr("height", (d) => d.layout.height)
        .attr("width", (d) => d.layout.width);
}


function drawColumnLabels(container, columns = []) {

    const mkTransform = (d) => {
        const colLabelOpts = ROADMAP_LAYOUT_OPTIONS.column.label;
        const dx = d.layout.x + colLabelOpts.dx;
        const dy = colLabelOpts.height;
        const angle = colLabelOpts.angle;
        return `translate(${ dx } ${ dy }) rotate(${ angle })`;
    };

    const colSelector = container
        .attr('transform', `translate(${ROADMAP_LAYOUT_OPTIONS.column.dx} 0)`)
        .selectAll(`.${STYLES.columnLabel}`)
        .data(columns, d => d.id);

    colSelector
        .enter()
        .append("text")
        .classed(STYLES.columnLabel, true)
        .text(d => d.datum.name)
        .attr("transform", d => mkTransform(d));
}


function drawColumns(svgGroups, gridDefWithLayout) {

    drawColumnLabels(svgGroups.colLabels, gridDefWithLayout.columns);

    const colSelector = svgGroups
        .colMain
        .attr('transform', `translate(${ ROADMAP_LAYOUT_OPTIONS.column.dx } ${ ROADMAP_LAYOUT_OPTIONS.column.label.height })`)
        .selectAll(`.${ STYLES.columns }`)
        .data(gridDefWithLayout.columns, d => d.id);

    const newCols = colSelector
        .enter()
        .append("g")
        .classed(STYLES.columns, true);

    newCols
        .append("rect");

    newCols
        .merge(colSelector)
        .attr("transform", d => `translate(${ d.layout.x } ${ d.layout.y })`)
        .select("rect")
        .attr("height", d => d.layout.height)
        .attr("width", d => d.layout.width);
}


export function draw(svgGroups, gridDef) {
    // console.log("draw", { svgGroups, gridDef});
    const gridDefWithLayout = calcLayout(gridDef);
    console.log("draw", { gridDefWithLayout });
    drawRowGroups(svgGroups, gridDefWithLayout);
    drawColumns(svgGroups, gridDefWithLayout);
}
