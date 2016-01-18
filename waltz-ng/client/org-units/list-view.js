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

import { buildHierarchies } from '../common';
import { talliesById } from '../common/tally-utils';


function controller(orgUnits, tallies, svgStore, $state) {


    const vm = this;

    const rootUnits = buildHierarchies(orgUnits);

    const toFlatNodes = (units, depth) => {
        return _.chain(units)
            .sortBy('name')
            .map(unit => {
                const node = {
                    unit: {
                        name: unit.name,
                        description: unit.description,
                        id: unit.id
                    },
                    style: {
                        'margin-left': (depth * 20) + 'px',
                        'padding-bottom': Math.max(10 - (depth * 2), 2) + 'px'
                    },
                    depth
                };
                return _.flatten(_.union([node], toFlatNodes(unit.children, depth + 1)));
            })
            .flatten()
            .value();
    };

    vm.flatUnits = toFlatNodes(rootUnits, 0);
    vm.talliesById = talliesById(tallies);

    svgStore.findByKind('ORG_UNIT').then(xs => vm.diagrams = xs);

    vm.blockProcessor = block => {
        block.parent.onclick = () => $state.go('main.org-units.unit', { id: block.value });
        angular.element(block.parent).addClass('clickable');
    };
}

controller.$inject = ['orgUnits', 'tallies', 'SvgDiagramStore', '$state'];


export default {
    template: require('./list-view.html'),
    controller,
    controllerAs: 'ctrl'
};
