/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import {perhaps} from './index';
import {select} from 'd3-selection';
import _ from 'lodash';


/**
 * Adapted from:
 *  http://brendansudol.com/writing/responsive-d3
 * @param svg
 * @returns {function()} to deregister the resize listener
 */
export function responsivefy(svg, type = 'both') {
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

        if (type === 'both') {
            svg.attr("height", Math.round(targetWidth / aspect));
        }
    };

    // add viewBox and preserveAspectRatio properties,
    // and call resize so that svg resizes on inital page load
    svg.attr("viewBox", `0 0 ${width} ${height}`);
    if (type === 'both') {
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


export function lineWithArrowPath(selection) {
    return selection.attr("d", (d) =>
        mkLineWithArrowPath(
            d.source.x,
            d.source.y,
            d.target.x,
            d.target.y));
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



export function wrapText(selection, width) {
    selection.each(function(d) {
        const textElem = select(this);
        const words = textElem
            .text()
            .split(/\s+/)
            .reverse();
        let line = [];
        let lineNumber = 0;
        const lineHeight = 1.1;
        const y = textElem.attr('y');
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