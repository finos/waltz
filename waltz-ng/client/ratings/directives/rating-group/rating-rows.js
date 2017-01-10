

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