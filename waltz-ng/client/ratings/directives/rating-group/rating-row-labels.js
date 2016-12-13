/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import _ from "lodash";
import {noopTweaker} from "../common";

export default class RatingRowLabels {

    constructor(dimensions, tweaker = noopTweaker) {
        this.tweaker = tweaker;
        this.dimensions = dimensions;
        this.className = 'rating-row-label';
    }

    enter(selection) {
        const { width, height } = this.dimensions;

        selection
            .append('text')
            .classed(this.className, true)
            .classed('no-text-select', true)
            .attr('transform', `translate(${width}, ${height / 1.6})`)
            .attr('text-anchor', 'end')
            .text(d => _.truncate(d.subject.name, { length: 26 }))
            .call(this.tweaker.enter.bind(this.tweaker.enter));
    }
}