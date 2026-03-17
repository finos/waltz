/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

import _ from "lodash";
import {color} from "d3-color";


function pickWorst(ratings = [], sortedByBadness = [ "DISCOURAGED", "SECONDARY", "PRIMARY", "NO_OPINION" ]) {
    const worst = _.find(sortedByBadness, x => _.includes(ratings, x));

    return worst || "NO_OPINION";
}


export default [
    () => {
        const buildGraphTweakers = (appIds = [], decorators = [], flowClassificationColors) => {

            const decoratorsByFlowId = _.groupBy(decorators, "dataFlowId");
            const calcRating = (d) => {
                const flowId = d.data.id;
                const flowDecorators = decoratorsByFlowId[flowId] || [];
                const ratings = _.map(flowDecorators, "rating");
                // note: we can use the domain() because when it is retrieved, it is already sorted by position & name
                // -> ref: flow-classification-utils.loadFlowClassificationRatings
                return pickWorst(ratings, flowClassificationColors.domain());
            };

            return {
                node : {
                    enter: (selection) => {
                        selection
                            .classed("wdfd-intra-node", d => _.includes(appIds, d.id))
                            .classed("wdfd-extra-node", d => ! _.includes(appIds, d.id))
                    },
                    update: _.identity,
                    exit: _.identity
                },
                link : {
                    update: (selection) => {
                        return selection
                            .attr("stroke", d => {
                                const rating = calcRating(d);
                                return flowClassificationColors(rating);
                            })
                            .attr("fill", d => {
                                const rating = calcRating(d);
                                return color(flowClassificationColors(rating)).brighter();
                            });
                    },
                    enter: (selection) => {
                        return selection
                            .attr("stroke-width", 1.5);

                    },
                    exit: _.identity
                }
            };
        };


        const enrichDataTypeCounts = (dataTypeCounts = [], displayNameService) => {
            return _
                .chain(dataTypeCounts)
                .map(dc => {
                    const enriched = {
                        dataType: {
                            id: dc.entityReference.id,
                            name: displayNameService.lookup("dataType", dc.entityReference.id)
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
