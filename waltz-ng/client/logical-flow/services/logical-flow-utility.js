
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

import _ from "lodash";
import {authoritativeRatingColorScale} from "../../common/colors";


function pickWorst(ratings = []) {
    const sortedByBadness = [ 'DISCOURAGED', 'SECONDARY', 'PRIMARY', 'NO_OPINION' ];
    const worst = _.find(sortedByBadness, x => _.includes(ratings, x));

    return worst || 'NO_OPINION';
}


export default [
    () => {

        const buildGraphTweakers = (appIds = [], decorators = []) => {

            const decoratorsByFlowId = _.groupBy(decorators, 'dataFlowId');
            const calcRating = (d) => {
                const flowId = d.data.id;
                const flowDecorators = decoratorsByFlowId[flowId] || [];
                const ratings = _.map(flowDecorators, 'rating');
                return pickWorst(ratings);
            };

            return {
                node : {
                    enter: (selection) => {
                        selection
                            .classed('wdfd-intra-node', d => _.includes(appIds, d.id))
                            .classed('wdfd-extra-node', d => ! _.includes(appIds, d.id))
                            .on('dblclick.unfix', d => { d.fx = null; d.fy = null; })
                    },
                    update: _.identity,
                    exit: _.identity
                },
                link : {
                    update: (selection) => {
                        return selection
                            .attr('stroke', d => {
                                const rating = calcRating(d);
                                return authoritativeRatingColorScale(rating);
                            })
                            .attr('fill', d => {
                                const rating = calcRating(d);
                                return authoritativeRatingColorScale(rating).brighter();
                            });
                            //
                            // .attr('marker-end', d => {
                            //     const rating = calcRating(d);
                            //     return `url(#arrowhead-${rating})`;
                            // })
                    },
                    enter: (selection) => {
                        return selection
                            .attr('stroke-width', 1.5);

                    },
                    exit: _.identity
                }
            };
        };


        const enrichDataTypeCounts = (dataTypeCounts = [], displayNameService) => {
            return _.chain(dataTypeCounts)
                .map(dc => {
                    const enriched = {
                        dataType: {
                            id: dc.entityReference.id,
                            name: displayNameService.lookup('dataType', dc.entityReference.id)
                        },
                        inbound: 0,
                        outbound: 0,
                        intra: 0,
                        total: 0
                    };
                    _.forEach(dc.tallies, t => {
                        enriched[_.lowerCase(t.id)] = t.count;
                        enriched.total += t.count;
                    });
                    return enriched;
                })
                .value();
        };


        return {
            buildGraphTweakers,
            enrichDataTypeCounts
        };
    }
];
