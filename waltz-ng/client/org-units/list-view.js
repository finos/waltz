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

import angular from "angular";
import {buildHierarchies} from "../common";
import {talliesById} from "../common/tally-utils";


function controller(orgUnits,
                    tallies,
                    svgStore,
                    $state) {

    const vm = this;

    const rootUnits = buildHierarchies(orgUnits);

    vm.talliesById = talliesById(tallies);
    vm.trees = rootUnits;

    svgStore
        .findByKind('ORG_UNIT')
        .then(xs => vm.diagrams = xs);

    vm.blockProcessor = b => {
        b.block.onclick = () =>
            $state.go('main.org-units.unit', { id: b.value });

        angular
            .element(b.block)
            .addClass('clickable');
    };
}

controller.$inject = [
    'orgUnits',
    'tallies',
    'SvgDiagramStore',
    '$state'
];


export default {
    template: require('./list-view.html'),
    controller,
    controllerAs: 'ctrl'
};
