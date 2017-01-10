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

import { noopTweaker } from '../common';
import WaltzVizComponent from '../viz-component';
import { capabilityColorScale } from '../../../common/colors';

export default class extends WaltzVizComponent {

    constructor(height, rowScale, currentTweaker = noopTweaker, revertTweaker = noopTweaker ) {
        super();
        this.height = height;
        this.rowScale = rowScale;
        this.className = 'rating-cell';
        this.currentTweaker = currentTweaker;
        this.revertTweaker = revertTweaker;
    }


    enter(selection) {

        const gs = selection
            .append('g')
            .classed(this.className, true);

        gs.append('rect')
            .classed('current-indicator', true)
            .attr({
                stroke: '#eee',
                fill: '#eee',
                height: 0
            })
            .call(this.currentTweaker.enter.bind(this.currentTweaker.enter));

        gs.filter(d => d.original !== undefined)
            .append('rect')
            .classed('original-indicator', true)
            .attr({
                stroke: d => capabilityColorScale(d.original),
                fill: d => capabilityColorScale(d.original).brighter(),
                height: this.height,
                width: 0
            })
            .call(this.revertTweaker.enter.bind(this.revertTweaker.enter));

    }


    update(selection) {
        selection
            .attr('transform', (d) => `translate(${this.rowScale(d.measurable)}, 0)`)
            .attr('data-measurable', d => d.measurable)
            .selectAll('.current-indicator')
            .attr({
                width: this.rowScale.rangeBand()
            })
            .transition()
            .duration(100)
            .attr({
                height: this.height,
                stroke: d => capabilityColorScale(d.current),
                fill: d => capabilityColorScale(d.current).brighter()
            });

        selection.selectAll('.original-indicator')
            .filter((d) => d.current !== d.original)
            .transition()
            .duration(150)
            .attr({
                width: this.rowScale.rangeBand() / 6
            });

        selection.selectAll('.original-indicator')
            .filter((d) => d.current === d.original)
            .transition()
            .duration(150)
            .attr({
                width: 0
            });
    }
}