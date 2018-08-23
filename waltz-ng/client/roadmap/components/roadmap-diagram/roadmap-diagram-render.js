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
    columnCell: "wrd-col-cell",
    columnLabel: "wrd-column-label",
    rowGroup: "wrd-row-group",
    rowGroupCell: "wrd-row-group-cell"
};


function drawRowGroups(svgGroups, gridDefWithLayout) {
    svgGroups
        .rowGroups
        .attr('transform', `translate(0 ${ROADMAP_LAYOUT_OPTIONS.column.height})`)
        .attr("clip-path", "url(#row-groups-clip)");

    const rowGroupSelector = svgGroups
        .rowGroupHeaders
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


function drawColumns(svgGroups, columns = []) {

    const mkTransform = (d) => {
        const colLabelOpts = ROADMAP_LAYOUT_OPTIONS.column.label;
        const dx = d.layout.x + colLabelOpts.dx;
        const dy = colLabelOpts.height;
        const angle = colLabelOpts.angle;
        return `translate(${ dx } ${ dy }) rotate(${ angle })`;
    };

    svgGroups
        .columns
        .attr('transform', `translate(${ROADMAP_LAYOUT_OPTIONS.column.dx} 0)`)
        .attr("clip-path", "url(#columns-clip)");


    const colSelector = svgGroups
        .columnHeaders
        .selectAll(`.${STYLES.columnLabel}`)
        .data(columns, d => d.id);

    colSelector
        .enter()
        .append("text")
        .classed(STYLES.columnLabel, true)
        .text(d => d.datum.name)
        .attr("transform", d => mkTransform(d));
}


function drawGridContent(svgGroups, gridDefWithLayout) {
    svgGroups
        .grid
        .attr('transform', `translate(${ ROADMAP_LAYOUT_OPTIONS.column.dx } ${ ROADMAP_LAYOUT_OPTIONS.column.height })`)
        .attr("clip-path", "url(#grid-clip)");

    const colSelector = svgGroups
        .gridContent
        .selectAll(`.${ STYLES.columnCell }`)
        .data(gridDefWithLayout.columns, d => d.id);

    colSelector
        .enter()
        .append("g")
        .classed(STYLES.columnCell, true)
        .attr('transform', d => `translate(${d.layout.x} 0)`)
        .selectAll(`.${STYLES.rowGroupCell}`)
        .data(gridDefWithLayout.rowGroups)
        .enter()
        .append('g')
        .classed(STYLES.rowGroupCell, true)
        .attr('transform', d => `translate(0, ${d.layout.y})`)
        .append('rect')
        .attr('width', ROADMAP_LAYOUT_OPTIONS.column.width)
        .attr('height', d => d.layout.height)
        .attr('fill', 'red')
        .attr('stroke', 'black');


}


export function draw(svgGroups, gridDef) {
    // console.log("draw", { svgGroups, gridDef});
    const gridDefWithLayout = calcLayout(gridDef);
    console.log("draw", { gridDefWithLayout });
    drawGridContent(svgGroups, gridDefWithLayout);
    drawRowGroups(svgGroups, gridDefWithLayout);
    drawColumns(svgGroups, gridDefWithLayout.columns);
}
