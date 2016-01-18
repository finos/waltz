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
import angular from 'angular';
import { init, render } from './slope-graph-diagram';


const dimensions = {
    margin: { top: 80, left: 120, right: 120, bottom: 20 },
    label: { width: 200, height: 10},
    viz: {}
};


function calculateDimensions(vizElem, types, sources, targets) {
    const maxRows = _.max([sources.length, targets.length, types.length]);
    const width = vizElem.offsetWidth;
    const height = dimensions.margin.top + dimensions.margin.bottom + (maxRows * 22);

    return { width, height };
}


function link(scope, elem) {
    scope.vizElem = elem[0].querySelector('.viz');
    scope.svg = init(scope.vizElem);
}

function controller(scope, $window) {

    const debouncedRender = _.debounce(() => {
        if (!( scope.vizElem
            && scope.incoming
            && scope.outgoing
            && scope.types
            && scope.sources
            && scope.targets
            && scope.tweakers)) return;

        const { incoming, outgoing, types, sources, targets, tweakers } = scope;
        const data = { incoming, outgoing, types, sources, targets};
        const vizDimensions = calculateDimensions(scope.vizElem, types, sources, targets);

        render(scope.svg, { ...dimensions, viz: vizDimensions}, data, tweakers);
    }, 100);


    angular.element($window).on('resize', () => debouncedRender());
    scope.$watchCollection('incoming', () => debouncedRender());
    scope.$watchCollection('outgoing', () => debouncedRender());
    scope.$watchCollection('sources', () => debouncedRender());
    scope.$watchCollection('targets', () => debouncedRender());
    scope.$watchCollection('types', () => debouncedRender());

}


controller.$inject = ['$scope', '$window'];

export default () => ({
    restrict: 'E',
    replace: true,
    scope: {
        incoming: '=',
        sources: '=',
        outgoing: '=',
        targets: '=',
        types: '=',
        tweakers: '='
    },
    template: '<div><div class="viz"></div></div>',
    link,
    controller
});
