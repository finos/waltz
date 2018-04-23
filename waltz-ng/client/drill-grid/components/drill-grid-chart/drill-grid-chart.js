

/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import template from "./drill-grid-chart.html";
import {initialiseData} from "../../../common";
import {ragColorScale} from "../../../common/colors";

import _ from "lodash";
import {event, select} from "d3-selection";
import {scaleBand, scaleLinear} from "d3-scale";
import {truncateText} from "../../../common/d3-utils";
import {truncate} from "../../../common/string-utils";
import {ascending} from "d3-array";
import {flattenChildren, getParents} from "../../../common/hierarchy-utils";


const bindings = {
    drillGrid: '<'
};


const initialState = {
};


const margin = {top: 100, right: 10, bottom: 20, left: 10};
const totalWidth = 1024;
const width = totalWidth - margin.left - margin.right;

const blocks = 32;
const blockHeight = 20;
const blockWidth = width / blocks;

const blockText = {
    dy: blockHeight * -0.3,
    dx: 3
};
const colWidth = 24;
const rowGroupPadding = 4;
const minHeight = 350;

const blockScaleX = scaleLinear()
    .domain([0,32])
    .range([0, width]);


const styles = {
    region: 'wdgc-region',
    rowGroupHover: 'wdgc-rowGroupHover',
    colHeader: 'wdgc-colHeader',
    colHover: 'wdgc-colHover',
    appMapping: 'wdgc-appMapping',
    rowGroup: 'wdgc-rowGroup',
    appRow: 'wdgc-appRow',
    history: 'wdgc-history',
    descendable: 'wdgc-descendable',
    tooltip: 'wdgc-tooltip',
    popup: 'wdgc-popup',
    usage: 'wdgc-usage',
    rowGroupBackground: 'wdgc-rowGroupBackground'
};


const regionInfo = [
    {
        name: 'xHeader',
        x: 5,
        y: 0,
        w: blocks - 5
    }, {
        name: 'yHeader',
        x: 1,
        y: 1,
        w: 4
    }, {
        name: 'rowGroups',
        x: 2,
        y: 1,
        w: blocks - 1
    }, {
        name: 'appFocus',
        x: 2,
        y: -1,
        w: 3
    }
];


// -- UTIL --

function calcTotalRequiredHeight(drillGrid) {
    const top = blockHeight * 2;
    const groupsHeight = _
        .chain(drillGrid.rowGroups)
        .map(g => g.rows.length)
        .sum()
        .value() * blockHeight;

    const groupsPadding = drillGrid.rowGroups.length * rowGroupPadding;

    const total = top + groupsHeight + groupsPadding + margin.top + margin.bottom;
    return _.max([total, minHeight]);
}


function applyBlockTextAttrs(selection) {
    return selection
        .attr('dy', blockText.dy)
        .attr('dx', blockText.dx);
}


/**
 * Each row group may have a variable number of applications (rows)
 * within it.  This function will return a map, keyed by the
 * groups 'domain.id', which gives the 'y' offsets for each group.
 *
 * @param rowGroups
 * @returns { <groupDomainId>: { yOffset, ... }
 */
function calculateRowGroupOffsets(rowGroups = []) {
    return _.reduce(
        rowGroups,
        (acc, rg) => {
            const rowCount = rg.rows.length;
            const start = acc.total;
            const height = (rowCount * blockHeight);

            acc[rg.group.id] = { start, height };

            acc.total = start + height + rowGroupPadding;
            return acc;
        },
        { total: 0 });
}


const highlightColumn = (colId, flag, svg) => {
    svg.selectAll(`.${styles.appMapping}`)
        .filter(d => d.colId === colId)
        .classed(styles.colHover, flag);

    svg.selectAll(`.${styles.colHeader}`)
        .filter(d => d.id === colId)
        .classed(styles.colHover, flag);
};


// -- DRAW FNs --


function init(svg, scaleX) {

    const chart = svg
        .append('g')
        .attr('transform', `translate(${margin.left}, ${margin.top})`);

    chart.selectAll(`.${styles.region}`)
        .data(regionInfo, d => d.name)
        .enter()
        .append('g')
        .classed(styles.region, true)
        .attr('transform', d => `translate(${scaleX(d.x)}, ${blockHeight * d.y})`)
        .each(function (d) {
            select(this).classed(d.name, true)
        });

    return chart;
}


function showCellTooltip(d, dialogs) {
    const html = `
        <div class="small">
            <div>
                ${d.col.name} / ${d.row.name}
            </div>
            <div>${d.app.name}</div>
            <div class="small">
                ${truncate(d.app.description, 128)}
            </div>
            <br>
            <div class="small text-muted">
                Click cell for more information
            </div>
        </div>
    `;

    return showDialog(dialogs, 'tooltip', html);
}


function showCellDetail(d, dialogs, drillGrid) {
    hideDialog(d, dialogs, 'tooltip');

    const colName = d.col.name;
    const rowName = d.row.name;

    const toTreeHtml = (nodes, optionName) => {
        const sortedNodes = _.sortBy(nodes, 'name');
        const items = _.map(sortedNodes, n => {
            return _.isEmpty(n.children)
                ? `<li>${n.name}</li>`
                : `<li><a class="clickable wdgc-navigation" data-wdgc-option-key="${optionName}" data-wdgc-option-value="${n.id}">${n.name}</a></li>` + toTreeHtml(n.children, optionName)
        });
        return nodes.length > 0
            ? `<ul> ${_.join(items, '')} </ul>`
            : '';
    };

    const toTreeView = (tree, optionName) => {
        const needScroll = flattenChildren(tree).length > 10;
        return `<div class="${needScroll ? 'waltz-scroll-region-150' : ''} small">
            ${toTreeHtml([tree], optionName)}
        </div>`;
    };


    const html = `
       <div class="small">
            <div>
                ${d.col.name} / ${d.row.name}
            </div>
            <div>
                <a class="wdgc-app-link clickable" 
                   data-wdgc-app-id="${d.app.id}">
                    ${d.app.name}
                </a>
            </div>
            <div class="small">
                ${truncate(d.app.description, 128)}
            </div>
            <br>
            
            <div>
                Children of <strong>${colName}</strong>:
                ${toTreeView(d.col, 'xId')}
                <br>
            </div>
  
            <div>
                Children of ${rowName}:
                ${toTreeView(d.row, 'yId')}
                <br>
            </div>
            <div class="clickable closer">
                <a class="clickable">
                  <span class="clearFocus">✕ </span>
                  <span>Dismiss</span>
                </a>
            </div>
        </div>
    `;

    return showDialog(dialogs, 'popup', html, true, drillGrid);
}


function showGroupTooltip(d, dialogs) {
    const parentList = _.map(
        getParents(d.group),
        p => `<li>${p.name}</li>`);

    const parentHtml = _.isEmpty(parentList)
        ? ''
        : `<hr>
           <div class="small text-muted">Parents</div>
           <ul class="small text-muted">
               ${_.join(parentList, '')}
           </ul>
          `;

    const html = `
        <div class="small">
            <div>
                ${d.group.name}
            </div>
            <div class="small">
                ${truncate(d.group.description, 128)}
            </div>
            ${parentHtml}
        </div>
    `;

    return showDialog(dialogs, 'tooltip', html);
}


function showAppTooltip(d, dialogs) {
    const html = `
        <div class="small">
            <div class="strong">${d.app.name}</div>
            <div class="small">
                ${truncate(d.app.description, 128)}
            </div>
            <br>
            <div class="small text-muted">
                Click on the application to toggle focus
            </div>
        </div>
    `;

    return showDialog(dialogs, 'tooltip', html);
}


function showDialog(dialogs, dialogName, html, pin = false, drillGrid) {
    const dialog = dialogs[dialogName];

    if (! dialogs.pinned || pin) {
        dialogs.active = dialog;
        dialogs.pinned = pin;

        const dialogElem = dialog
            .html(html)
            .style('display', 'block')
            .style('left', () => `${event.pageX}px`)
            .style('top', () => `${event.offsetY - 30}px`)
            .style('opacity', 0.9);

        dialogElem
            .selectAll('.closer')
            .on('click', (d) => hideDialog(d, dialogs, dialogName, true));

        dialogElem
            .selectAll('.wdgc-navigation')
            .on('click', function(d) {
                hideDialog(d, dialogs, dialogName, true);
                const linkElem = select(this);
                const key = linkElem.attr('data-wdgc-option-key');
                const val = linkElem.attr('data-wdgc-option-value');
                if (_.isEmpty(key) || _.isEmpty(val)) {
                    // nop
                } else {
                    drillGrid.refresh({ [key]: val });
                }
            });

        dialogElem
            .selectAll('.wdgc-app-link')
            .on('click', function(d) {
                hideDialog(d, dialogs, dialogName, true);
                const linkElem = select(this);
                const id = linkElem.attr('data-wdgc-app-id');
                if (! _.isEmpty(id)) {
                    // naughty, but otherwise location or $state would need to be passed everywhere
                    window.location.hash = `/application/${id}`;
                }
            });
    } else {
        return dialogs.active;
    }
}


function hideDialog(d, dialogs, dialogName = 'tooltip', force = false) {
    const dialog = dialogs[dialogName];

    if (! dialogs.pinned || force) {
        dialog
            .style('display', 'none');
        dialogs.active = null;
        dialogs.pinned = false;
    }
}


function drawAppMappings(selector, colScale, drillGrid, svg, dialogs) {

    const appMappings = selector
        .selectAll(`.${styles.appMapping}`)
        .data(d => _.filter(d.mappings, m => m.rating !== 'Z'), d => d.colId);

    appMappings.exit().remove();

    const newAppMappings = appMappings
        .enter()
        .filter(d => d.colType != 'NONE')
        .append('g')
        .classed(styles.appMapping, true)
        .on('mouseover.highlight', d => highlightColumn(d.colId, true, svg))
        .on('mouseout.highlight', d => highlightColumn(d.colId, false, svg))
        .on("mouseover.tooltip", d => showCellTooltip(d, dialogs))
        .on("mouseout.tooltip", d => hideDialog(d, dialogs, 'tooltip'))
        .on("click.detail", d => showCellDetail(d, dialogs, drillGrid));

    newAppMappings
        .append('rect')
        .attr('stroke', d => ragColorScale(d.rating))
        .attr('fill', d => ragColorScale(d.rating).brighter(2.5))
        .attr('rx', 1)
        .attr('y', 1)
        .attr('x', (colScale.bandwidth() / 2) * -1)
        .attr('width', colScale.bandwidth())
        .attr('height', blockHeight - 2);

    return appMappings
        .merge(newAppMappings)
        .attr('transform', d => `translate(${colScale(d.colId)} , 0)`);
}


const arrows = {
    DIRECT: ' ',
    HEIR: '↑', // ⇑
    ANCESTOR: '↓', // ⇓
    NONE: 'x',
    UNKNOWN: '?'
};


const typePriority = {
    DIRECT: 1,
    HEIR: 2,
    ANCESTOR: 3,
    UNKNOWN: 4,
    NONE: 5
};


function typeToArrow(type) {
    return arrows[type] || ' ';
}


function typeToPriority(type) {
    return typePriority[type] || 9;
}


function drawAppRows(selector, colScale, drillGrid, svg, dialogs) {
    const appRows = selector
        .selectAll(`.${styles.appRow}`)
        .data(d => d.rows, d => d.app.id);

    const newAppRows = appRows
        .enter()
        .append('g')
        .classed(styles.appRow, true);

    newAppRows
        .merge(appRows)
        .sort((a, b) => {
            const aPriority = typeToPriority(a.rowType);
            const bPriority = typeToPriority(b.rowType);

            return ascending(aPriority + a.app.name, bPriority + b.app.name);
        })
        .attr('transform', (d, i) => `translate(${blockWidth * 3}, ${i * blockHeight})`);

    newAppRows
        .append('text')
        .text(d => `${typeToArrow(d.rowType)} ${d.app.name}`)
        .attr('y', blockHeight)
        .on('click', (d) => {
            if (_.get(drillGrid, "options.focusApp.id") === d.app.id) {
                drillGrid.refresh({ focusApp: null });
            } else {
                drillGrid.refresh({ focusApp: d.app });
            }
        })
        .on("mouseover", d => showAppTooltip(d, dialogs))
        .on("mouseout", d => hideDialog(d, dialogs, 'tooltip'))
        .call(applyBlockTextAttrs)
        .call(truncateText, blockWidth * 4);

    appRows.exit().remove();

    appRows
        .merge(newAppRows)
        .call(drawAppMappings, colScale, drillGrid, svg, dialogs);

    return appRows;
}


function drawRowGroupLabel(selection, drillGrid, dialogs) {
    return selection
        .append('text')
        .attr('y', blockHeight)
        .text(d => d.group.name)
        .on('click.focus', d => drillGrid.refresh({ yId: d.group.id }))
        .on("mouseover", d => showGroupTooltip(d, dialogs))
        .on("mouseout", d => hideDialog(d, dialogs, 'tooltip'))
        .call(applyBlockTextAttrs)
        .call(truncateText, blockWidth * 3 - 10)
}


function drawRowGroups(drillGrid, svg, dialogs, colScale, rowWidth) {

    const groupOffsets = calculateRowGroupOffsets(drillGrid.rowGroups);

    const rowGroups = svg
        .select('.rowGroups')
        .selectAll(`.${styles.rowGroup}`)
        .data(drillGrid.rowGroups, d => d.group.id);

    rowGroups.exit().remove();

    const newRowGroups = rowGroups
        .enter()
        .append('g')
        .classed(styles.descendable, d => (d.group.children || []).length > 0)
        .classed(styles.rowGroup, true);

    newRowGroups
        .append('rect')
        .classed(styles.rowGroupBackground, true);

    newRowGroups
        .merge(rowGroups)
        .attr('transform', d => `translate(0, ${groupOffsets[d.group.id].start})`)
        .select('rect')
        .attr('width', rowWidth)
        .attr('height', d => groupOffsets[d.group.id].height)
        .attr('fill', (d,i) => i % 2 ? '#fafafa': '#f3f3f3');

    newRowGroups
        .on('mouseover.hover', function() { select(this).classed(styles.rowGroupHover, true)})
        .on('mouseout.hover', function() { select(this).classed(styles.rowGroupHover, false)})
        .call(drawRowGroupLabel, drillGrid, dialogs);

    rowGroups
        .merge(newRowGroups)
        .call(drawAppRows, colScale, drillGrid, svg, dialogs);
}


function drawColHeaders(drillGrid, svg, colScale) {
    const colsDomain = _.get(drillGrid, 'xAxis.current.domain',[]);

    const headers = svg
        .select(".xHeader")
        .selectAll(`.${styles.colHeader}`)
        .data(colsDomain, refToString);

    headers.exit().remove();

    const newHeaders = headers
        .enter()
        .append('g')
        .classed(styles.colHeader, true)
        .classed(styles.descendable, d => (d.children || []).length > 0)
        .on('mouseover', d => highlightColumn(d.id, true, svg))
        .on('mouseout', d => highlightColumn(d.id, false, svg));

    newHeaders
        .append('text')
        .text(d => d.name )
        .attr('y', 16)
        .call(truncateText, blockWidth * 4);

    return newHeaders
        .merge(headers)
        .attr('transform', d => `translate(${colScale(d.id)}, 0) rotate(315 0,14)`)
        .on('click.focus', d => drillGrid.refresh( { xId: d.id }));
}


function drawHistory(drillGrid, svg) {
    const xHistoryDatum = _.get(drillGrid, 'xAxis.current.active');
    const yHistoryDatum = _.get(drillGrid, 'yAxis.current.active');
    const appHistoryDatum = drillGrid.options.focusApp;

    const appEntry = appHistoryDatum
        ? {
            id: 'APPLICATION',
            name: appHistoryDatum.name,
            action: () => drillGrid.refresh({ focusApp: null })
        }
        : null;

    const xEntry = xHistoryDatum
        ? {
            id: 'XAXIS',
            name: xHistoryDatum.name,
            action: () => drillGrid.refresh({ xId: xHistoryDatum.parentId })
        }
        : null;

    const yEntry = yHistoryDatum
        ? {
            id: 'YAXIS',
            name: yHistoryDatum.name,
            action: () => drillGrid.refresh({ yId: yHistoryDatum.parentId })
        }
        : null;

    const historyEntries = _.compact([
        appEntry,
        xEntry,
        yEntry
    ]);

    svg.select(`.${styles.usage}`)
        .style('display', historyEntries.length > 0 ? 'none' : 'inline-block');

    const historyElems = svg
        .select('.appFocus')
        .selectAll('text')
        .data(historyEntries, d => d.id + d.name);

    historyElems.exit().remove();

    const newHistoryElems = historyElems
        .enter()
        .append('text')
        .on('click', (d) => d.action());

    historyElems
        .merge(newHistoryElems)
        .attr('dy', (d,i) => i * 12);

    newHistoryElems
        .append('tspan')
        .classed('clearFocus', true)
        .text('✕ ');

    newHistoryElems
        .append('tspan')
        .text(d => d.name)
        .call(truncateText, blockWidth * 4);
}


function refToString(ref) {
    if (_.isString(ref)) return ref;
    return `${ref.kind}/${ref.id}`;
}


function draw(drillGrid, svg, dialogs, blockScaleX) {
    if (! svg) return;

    const height = calcTotalRequiredHeight(drillGrid);

    svg.attr('viewBox', `0 0 1024 ${height}`);
    svg.attr('height', height);

    const colIds = _.flow(
        d => _.get(d, 'xAxis.current.domain', []),
        d => _.map(d, 'id')
    )(drillGrid);

    const colsWidth = colWidth * colIds.length || 1;  // 'or 1' to prevent colScale from blowing up
    const colsStartX = blockScaleX(5);

    // this scale works within the xHeader group
    const colScale = scaleBand()
        .domain(colIds)
        .range([colsStartX, colsStartX + colsWidth])
        .paddingInner([0.1])
        .paddingOuter([0.3])
        .align([0.5]);

    // full rowWidth takes account of group and app names (blockScale 8)
    const rowWidth = colsWidth + blockScaleX(8);

    drawColHeaders(drillGrid, svg, colScale);
    drawRowGroups(drillGrid, svg, dialogs, colScale, rowWidth);
    drawHistory(drillGrid, svg);
}


// --- NG ---

function controller($element) {
    const vm = initialiseData(this, initialState);

    let svg = null;
    const dialogs = {
        tooltip: null,
        popup: null,
        active: null
    };

    vm.$onInit = () => {
        const rootElem = $element[0];
        svg = select(rootElem)
            .select('svg');

        dialogs.tooltip = select(rootElem)
            .select(`.${styles.tooltip}`);

        dialogs.popup = select(rootElem)
            .select(`.${styles.popup}`);

        init(svg, blockScaleX);
    };

    vm.$onChanges = (c) => {
        if (c.drillGrid && vm.drillGrid) {
            vm.drillGrid.addListener(() => draw(vm.drillGrid, svg, dialogs, blockScaleX));
        }
    };
}


controller.$inject = ['$element'];


const component = {
    controller,
    bindings,
    template
};


export default {
    id: 'waltzDrillGridChart',
    component
}