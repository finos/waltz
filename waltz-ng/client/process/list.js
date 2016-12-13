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

import {termSearch} from "../common";


const initialState = {
    processes: [],
    filteredProcesses: [],
    processQuery: ''
};


function controller($scope,
                    $state,
                    processStore,
                    sourceDataRatingStore,
                    staticPanelStore,
                    svgStore) {
    const vm = Object.assign(this, initialState);

    processStore
        .findAll()
        .then(ps => vm.processes = ps);

    sourceDataRatingStore
        .findAll()
        .then(sdrs => vm.sourceDataRatings = sdrs);

    svgStore
        .findByKind('PROCESS')
        .then(xs => vm.diagrams = xs);

    staticPanelStore
        .findByGroup("HOME.PROCESS")
        .then(panels => vm.panels = panels);

    vm.blockProcessor = b => {
        b.block.onclick = () => $state.go('main.process.view', { id: b.value });
        angular.element(b.block).addClass('clickable');
    };

    $scope.$watchGroup(
        ['ctrl.processQuery', 'ctrl.processes'],
        ([q, ps]) => vm.filteredProcesses = termSearch(ps, q)
    );

}


controller.$inject = [
    '$scope',
    '$state',
    'ProcessStore',
    'SourceDataRatingStore',
    'StaticPanelStore',
    'SvgDiagramStore'
];


const view = {
    template: require('./list.html'),
    controller,
    controllerAs: 'ctrl'
};


export default view;