/**
 *   Adapted from: https://github.com/pkerpedjiev/d3-context-menu
 *
 *   The MIT License (MIT)
 *
 *   Copyright (C) 2014-present Patrick Gillespie and other contributors

 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:

 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.

 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
 *   - converted to ES6 and d3 v4
 *   - allowed actions to return a `result` object which is passed to the `onClose` callback handler
 */

import {event, select, selectAll} from "d3-selection";


export function d3ContextMenu(menu, opts) {
    let openCallback,
        closeCallback;

    if (typeof opts === "function") {
        openCallback = opts;
    } else {
        opts = opts || {};
        openCallback = opts.onOpen;
        closeCallback = opts.onClose;
    }

    // create the div element that will hold the context menu
    selectAll(".d3-context-menu")
        .data([1])
        .enter()
        .append("div")
        .attr("class", "d3-context-menu");

    // close menu
    select("body")
        .on("click.d3-context-menu", () => {
            select(".d3-context-menu")
                .style("display", "none");
            if (closeCallback) {
                closeCallback();
            }
        });

    // this gets executed when a contextmenu event occurs
    return function(data, index) {
        const elem = this;

        selectAll(".d3-context-menu")
            .html("");
        var list = selectAll(".d3-context-menu")
            .on("contextmenu", function(d) {
                select(".d3-context-menu").style("display", "none");
                event.preventDefault();
                event.stopPropagation();
            })
            .append("ul");

        list.selectAll("li")
            .data(typeof menu === "function" ? menu(data, elem, index) : menu)
            .enter()
            .append("li")
            .classed("is-divider", d => d.divider)
            .classed("is-disabled", d => d.disabled)
            .classed("is-header", d => !d.action)
            .classed("is-action", d => d.action != null)
            .html((d) => {
                if (d.divider) {
                    return "<hr>";
                }
                if (!d.title) {
                    console.error("No title attribute set. Check the spelling of your options.");
                }
                return (typeof d.title === "string") ? d.title : d.title(data);
            })
            .on("click", (d, i) => {
                if (d.disabled) return; // do nothing if disabled
                if (!d.action) return; // headers have no "action"
                const result = d.action(elem, data, index);
                select(".d3-context-menu")
                    .style("display", "none");

                if (closeCallback) {
                    closeCallback(result);
                }
            });

        // the openCallback allows an action to fire before the menu is displayed
        // an example usage would be closing a tooltip
        if (openCallback) {
            if (openCallback(data, index) === false) {
                return;
            }
        }

        // display context menu
        select(".d3-context-menu")
            .style("left", (event.pageX - 2) + "px")
            .style("top", (event.pageY - 2) + "px")
            .style("display", "block");

        event.preventDefault();
        event.stopPropagation();
    };
}