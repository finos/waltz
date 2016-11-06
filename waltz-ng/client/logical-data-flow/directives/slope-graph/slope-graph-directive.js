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


const bindings = {
    entityRef: '<',
    flows: '<',
    decorators: '<',
    tweakers: '<'
};


const template = "<div><div class='viz'></div></div>";


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


function controller($element,
                    $window) {

    const vizElem = $element[0].querySelector('.viz');
    const svg = init(vizElem);

    const vm = this;

    const debouncedRender = _.debounce(() => {
        if (!( vizElem
            && vm.entityRef
            && vm.flows
            && vm.tweakers)) return;

        const { incoming = [], outgoing = [] } = _.groupBy(
            vm.flows,
            f => f.source.id === vm.entityRef.id
                ? 'outgoing'
                : 'incoming');

        const types = [];
        const sources = _.chain(incoming).map("source").uniqBy('id').value();
        const targets = _.chain(outgoing).map("target").uniqBy('id').value();

        const data = { incoming, outgoing, types, sources, targets};
        const vizDimensions = calculateDimensions(vizElem, types, sources, targets);

        render(
            svg,
            { ...dimensions, viz: vizDimensions},
            data,
            vm.tweakers);

    }, 100);

    angular
        .element($window)
        .on('resize', () => debouncedRender());

    vm.$onChanges = () => debouncedRender();
}


controller.$inject = [
    '$element',
    '$window'
];


const component = {
    bindings,
    template,
    controller
};


export default component;
