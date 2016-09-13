
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

import _ from "lodash";
import { authoritativeRatingColorScale } from '../../common/colors';
import { pickWorst } from '../../auth-sources/services/auth-sources-utils'


export default [
    () => {

        const enrich = (flows, flowEntities) => {
            const entitiesById = _.keyBy(flowEntities, 'id');

            return _.map(flows, f => ({
                ...f,
                sourceEntity: entitiesById[f.source.id],
                targetEntity: entitiesById[f.target.id]
            }));
        };

        const getDataTypes = (flows) => {
            return _.chain(flows)
                .map('dataType')
                .flatten()
                .uniq()
                .value();
        };

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
                            .on('click', app => app.fixed = true)
                            .on('dblclick', app => app.fixed = false)
                    },
                    update: _.identity,
                    exit: _.identity
                },
                link : {
                    enter: (selection) => {
                        selection
                            .attr('stroke', d => {
                                const rating = calcRating(d);
                                return authoritativeRatingColorScale(rating);
                            })
                            .attr('stroke-width', 1.5)
                            .attr('marker-end', d => {
                                const rating = calcRating(d);
                                return `url(#arrowhead-${rating})`;
                            })
                    },
                    update: _.identity,
                    exit: _.identity
                }
            };
        };


        return {
            enrich,
            getDataTypes,
            buildGraphTweakers,
        };
    }
];
