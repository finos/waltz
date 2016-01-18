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
import _ from 'lodash';

import { noopTweaker } from '../common';

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
            .text(d => _.trunc(d.subject.name, 26))
            .call(this.tweaker.enter.bind(this.tweaker.enter));
    }
}