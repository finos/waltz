/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
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