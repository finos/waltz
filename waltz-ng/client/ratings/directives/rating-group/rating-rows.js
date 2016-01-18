

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
import RatingRowLabels from './rating-row-labels';
import RatingCells from './rating-cells';


export default class extends WaltzVizComponent {

    constructor(dimensions, rowScale, tweakers) {
        super();
        this.className = 'rating-row';
        this.dimensions = dimensions;
        this.tweakers = tweakers;

        this.rowTweaker = tweakers.ratingRow || noopTweaker;

        this.rowHeight = (this.dimensions.ratingCell.height + this.dimensions.ratingCell.padding);

        this.ratingRowLabels = new RatingRowLabels(
            { width: dimensions.label.width, height: this.rowHeight },
            tweakers.subjectLabel
        );

        this.ratingCells = new RatingCells(
            this.dimensions.ratingCell.height,
            rowScale,
            tweakers.currentCell,
            tweakers.originalCell);
    }


    enter(selection) {
        const translator = (d, i) => `translate(0, ${(i + 1) * this.rowHeight })`;

        selection
            .append('g')
            .classed(this.className, true)
            .attr('transform', translator)
            .attr('data-subject', d => d.subject.code || d.subject.id)
            .call(this.ratingRowLabels.enter.bind(this.ratingRowLabels))
            .call(this.rowTweaker.enter);

    }


    update(selection) {
        this.ratingCells.apply(selection, d => d.ratings);
    }

    exit(selection) {
        selection
            .remove();
    }
}