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

import {perhaps} from "./index";
import {select} from "d3-selection";
import _ from "lodash";


/**
 * Adapted from:
 *  http://brendansudol.com/writing/responsive-d3
 * @param svg
 * @returns {function()} to deregister the resize listener
 */
export function responsivefy(svg, type = "both") {
    // get container + svg aspect ratio
    const container = select(svg.node().parentNode);
    const id = "resize." + container.attr("id");
    const width = parseInt(svg.style("width"));
    const height = parseInt(svg.style("height"));
    const aspect = width / height;

    const resize = () => {
        const targetWidth = parseInt(container.style("width"));
        if (_.isNaN(targetWidth)) return;
        svg.attr("width", targetWidth);

        if (type === "both") {
            svg.attr("height", Math.round(targetWidth / aspect));
        }
    };

    // add viewBox and preserveAspectRatio properties,
    // and call resize so that svg resizes on inital page load
    svg.attr("viewBox", `0 0 ${width} ${height}`);
    if (type === "both") {
        svg.attr("perserveAspectRatio", "xMinYMid");
    }
    svg.call(resize);

    select(window)
        .on(id, resize);

    const destroyListenerFn = () =>
        select(window)
            .on(id, null);

    return destroyListenerFn;
}


export function lineWithArrowPath(selection, arrowLoc = 0.2) {
    return selection.attr("d", (d) =>
        mkLineWithArrowPath(
            d.source.x,
            d.source.y,
            d.target.x,
            d.target.y,
            arrowLoc));
}


/**
 *
 * @param x1
 * @param y1
 * @param x2
 * @param y2
 * @param arrowLoc  (0.1 = nr target, 1 = center, 1.9 = nr source)
 * @returns {string}
 */
export function mkLineWithArrowPath(x1, y1, x2, y2, arrowLoc = 0.2) {
    const dx = x2 - x1;
    const dy = y2 - y1;
    const theta = Math.atan2(dy, dx); // (rotate marker)
    const d90 = Math.PI / 2;
    const l = Math.sqrt(dx*dx + dy*dy) * arrowLoc - 7;
    const dtxs = x2 - l/2 * Math.cos(theta);  // val is how far 'back'
    const dtys = y2 - l/2 * Math.sin(theta);
    return `M${x1},${y1} 
            l${dx} ${dy}
            M${dtxs},${dtys}
            l${(3.5 * Math.cos(d90 - theta) - 10 * Math.cos(theta))}
                ,${(-3.5 * Math.sin(d90 - theta) - 10 * Math.sin(theta))}
            L${(dtxs - 3.5 * Math.cos(d90 - theta) - 10 * Math.cos(theta))}
                ,${(dtys + 3.5 * Math.sin(d90 - theta) - 10 * Math.sin(theta))}
            z`;
}

/**
 * @param x1
 * @param y1
 * @param x2
 * @param y2
 * @param flatness (higher = flatter, defaults to 3)
 * @returns {string}
 */
export function mkCurvedLine(x1, y1, x2, y2, flatness = 3) {
    const dx = x2 - x1;
    const dy = y2 - y1;
    const dr = Math.sqrt(dx * dx + dy * dy);

    return `M${x1} ${y1} 
            A${dr * flatness},${dr * flatness} 0 0,1 ${x2},${y2}`;
}


export function wrapText(selection, width) {
    selection.each(function() {
        const textElem = select(this);
        const words = textElem
            .text()
            .split(/\s+/)
            .reverse();
        let line = [];
        let lineNumber = 0;
        const lineHeight = 1.1;
        const y = textElem.attr("y");
        const dy = 0.1;
        let word;

        let tspan = textElem
            .text(null)
            .append("tspan")
            .attr("x", 0)
            .attr("y", y);

        while (word = words.pop()) {
            line.push(word);
            tspan.text(line.join(" "));
            const computedLength = perhaps(() => tspan.node().getComputedTextLength(), 150);
            if (computedLength > width) {
                lineNumber++;
                line.pop();
                tspan.text(line.join(" "));
                line = [word];
                tspan = textElem
                    .append("tspan")
                    .attr("x", 0)
                    // .attr("y", y)
                    .attr("y", lineNumber * lineHeight + dy + "em")
                    .text(word);
            }
        }
    });
}


export function truncateText(selection, maxWidth = 130) {
    return selection
        .each(function() {
            const self = select(this);
            setTimeout(() => {
                let textLength = self.node().getComputedTextLength();
                let text = self.text();

                let iterations = 0;
                while (textLength > (maxWidth) && text.length > 0) {
                    iterations++;
                    text = text.slice(0, -1);
                    self.text(text + "...");
                    textLength = self.node().getComputedTextLength();
                }
            }, 10)
        });
}

/**
 * Example definitions:
 * <pre>
 * [
 *  {
 *    name: "holder",
 *    children: [
 *        { name: "columns", children: [ {name : 'columnHeadings'}, { name: 'columnsBody' } ]},
 *        { name: "rowGroups", children: [ {name : 'rowGroupHeadings'}, { name: 'rowGroupsBody' } ]},
 *    ]
 *  }
 * ]
 * </pre>
 * @param container - where to render elements
 * @param definitions - what to render
 */
export function createGroupElements(container, definitions = []) {
    const register = {};

    const createGroupElem = (container, definition) => {
        const g = container
            .append("g")
            .classed(definition.name, true);

        _.forEach(definition.attrs, (v,k) => g.attr(k, v));

        register[definition.name] = g;
        _.forEach(definition.children || [], c => createGroupElem(g, c));
    };

    _.forEach(definitions, d => createGroupElem(container, d));
    return register;
}


/**
 * Given an array of points (absolute coords: {x: 0, y: 0}) will return a path, moving  ('M') to the first then
 * drawing lines ('L') to subsequent points, finishing with a close shape ('Z')
 * @param pts
 * @returns {string}
 */
export function toPath(pts) {
    const toPathCombiner = (acc, p, i) => acc + `${i == 0 ? "M" : "L"}${p.x} ${p.y} `;
    return _.reduce(pts, toPathCombiner, "") + "Z"
}
