
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


export default [
    () => ({
        restrict: 'E',
        replace: true,
        template: require('./org-unit-flow-viz.html'),
        scope: {
            entities: '=',
            flows: '='
        },
        link: (scope) => {

            const context = {};

            scope.$watchGroup(['entities', 'flows'], () => {

                if (scope.entities && scope.flows ) {
                    context.allEntities = scope.entities;
                    context.allFlows = scope.flows;
                    context.entitiesById = _.keyBy(scope.entities, 'id');
                    context.availableTypes = _.chain(context.allFlows)
                        .map('dataType')
                        .uniq()
                        .value();

                    scope.data = {
                        entities: context.allEntities,
                        flows: context.allFlows
                    };

                    scope.types = {
                        available: context.availableTypes,
                        selected: 'ALL'
                    };

                    scope.tweakers = {
                        node: {
                            enter: (selection) =>
                                selection
                                    .classed('wdfd-intra-node', d => !d.isNeighbour)
                                    .classed('wdfd-extra-node', d => d.isNeighbour)
                                    .on('click', app => app.fixed = true)
                                    .on('dblclick', app => app.fixed = false)
                        }
                    };
                }

            });


            const filterFlows = (type) => {
                const flows = _.filter(
                    context.allFlows,
                        f => type === 'ALL' ? true : f.dataType === type);

                const entities = _.chain(flows)
                    .map(f => ([f.source.id, f.target.id]))
                    .flatten()
                    .uniq()
                    .map(id => context.entitiesById[id])
                    .value();

                scope.data = {
                    flows,
                    entities
                };

            };

            scope.show = (type) => {
                scope.types.selected = type;
                filterFlows(type);
            };

        }
    })
];
