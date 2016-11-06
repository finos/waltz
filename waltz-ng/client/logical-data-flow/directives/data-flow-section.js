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

export default [
    'LogicalFlowUtilityService',
    (flowUtils) => ({
        restrict: 'E',
        replace: true,
        template: require('./data-flow-section.html'),
        scope: {
            flows: '=',
            selfId: '@'
        },
        link: (scope) => {
            scope.$watch('flows', () => {
                if (scope.flows) {
                    const [incoming, outgoing] = _.partition(scope.flows, f => f.source.id !== Number(scope.selfId));
                    scope.incoming = incoming;
                    scope.outgoing = outgoing;
                    scope.types = flowUtils.getDataTypes(scope.flows);
                }
            });
        }
    })
]
;
