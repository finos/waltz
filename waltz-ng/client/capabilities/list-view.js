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


function controller(appCapabilityStore,
                    capabilities,
                    staticPanelStore,
                    svgStore,
                    $state) {
    const vm = this;

    vm.capabilityHierarchy = buildHierarchies(capabilities);

    appCapabilityStore
        .countByCapabilityId()
        .then(tallies => vm.tallies = talliesById(tallies));

    staticPanelStore
        .findByGroup("HOME.CAPABILITY")
        .then(panels => vm.panels = panels);

    svgStore
        .findByKind('CAPABILITY')
        .then(xs => vm.diagrams = xs);

    vm.blockProcessor = b => {
        b.block.onclick = () => $state.go('main.capability.view', { id: b.value });
        angular.element(b.block).addClass('clickable');
    };
}

controller.$inject = [
    'AppCapabilityStore',
    'capabilities',
    'StaticPanelStore',
    'SvgDiagramStore',
    '$state'];


export default {
    template: require('./list-view.html'),
    controller,
    controllerAs: 'ctrl'
};
