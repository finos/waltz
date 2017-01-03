/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
        .findByGroup('CAPABILITY')
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
