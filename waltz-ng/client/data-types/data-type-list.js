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
import {buildHierarchies, initialiseData} from "../common";


const initialState = {
    dataTypes: [],
    trees: []
};


function controller($state,
                    dataTypes,
                    staticPanelStore,
                    svgStore,
                    lineageStore) {

    const vm = initialiseData(this, initialState);

    vm.trees = buildHierarchies(dataTypes);

    vm.nodeSelected = (node) => vm.selectedNode = node;

    svgStore
        .findByKind('DATA_TYPE')
        .then(xs => vm.diagrams = xs);

    staticPanelStore
        .findByGroup("HOME.DATA-TYPE")
        .then(panels => vm.panels = panels);

    vm.blockProcessor = b => {
        b.block.onclick = () => $state.go('main.data-type.code', { code: b.value });
        angular.element(b.block).addClass('clickable');
    };


    vm.flowTableInitialised = (api) => {
        vm.exportLineageReports = api.export;
    }


    lineageStore
        .findAllLineageReports()
        .then(lineageReports => vm.lineageReports = lineageReports);

}


controller.$inject = [
    '$state',
    'dataTypes',
    'StaticPanelStore',
    'SvgDiagramStore',
    'PhysicalFlowLineageStore'
];


const view = {
    template: require('./data-type-list.html'),
    controllerAs: 'ctrl',
    controller
};


export default view;