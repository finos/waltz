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

import {selectAll, select, event} from 'd3-selection';

/**
 *   Adapted from: https://github.com/pkerpedjiev/d3-context-menu
 *
 *   - converted to ES6 and d3 v4
 *   - allowed actions to return a `result` object which is passed to the `onClose` callback handler
 */

export function d3ContextMenu(menu, opts) {
    let openCallback,
        closeCallback;

    if (typeof opts === 'function') {
        openCallback = opts;
    } else {
        opts = opts || {};
        openCallback = opts.onOpen;
        closeCallback = opts.onClose;
    }

    // create the div element that will hold the context menu
    selectAll('.d3-context-menu')
        .data([1])
        .enter()
        .append('div')
        .attr('class', 'd3-context-menu');

    // close menu
    select('body')
        .on('click.d3-context-menu', () => {
            select('.d3-context-menu')
                .style('display', 'none');
            if (closeCallback) {
                closeCallback();
            }
        });

    // this gets executed when a contextmenu event occurs
    return function(data, index) {
        const elem = this;

        selectAll('.d3-context-menu')
            .html('');
        var list = selectAll('.d3-context-menu')
            .on('contextmenu', function(d) {
                select('.d3-context-menu').style('display', 'none');
                event.preventDefault();
                event.stopPropagation();
            })
            .append('ul');

        list.selectAll('li')
            .data(typeof menu === 'function' ? menu(data) : menu)
            .enter()
            .append('li')
            .classed('is-divider', d => d.divider)
            .classed('is-disabled', d => d.disabled)
            .classed('is-header', d => !d.action)
            .classed('is-action', d => d.action != null)
            .html((d) => {
                if (d.divider) {
                    return '<hr>';
                }
                if (!d.title) {
                    console.error('No title attribute set. Check the spelling of your options.');
                }
                return (typeof d.title === 'string') ? d.title : d.title(data);
            })
            .on('click', (d, i) => {
                if (d.disabled) return; // do nothing if disabled
                if (!d.action) return; // headers have no "action"
                const result = d.action(elem, data, index);
                select('.d3-context-menu')
                    .style('display', 'none');

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
        select('.d3-context-menu')
            .style('left', (event.pageX - 2) + 'px')
            .style('top', (event.pageY - 2) + 'px')
            .style('display', 'block');

        event.preventDefault();
        event.stopPropagation();
    };
}