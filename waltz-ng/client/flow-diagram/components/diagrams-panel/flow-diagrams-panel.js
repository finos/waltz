/*
 *
 *  * Waltz - Enterprise Architecture
 *  * Copyright (C) 2017  Khartec Ltd.
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Lesser General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import _ from "lodash";
import {initialiseData} from "../../../common";


const bindings = {
    flowDiagrams: '<',
    flowDiagramEntities: '<',
    createDiagramCommands: '<',
    reload: '<'
};


const template = require('./flow-diagrams-panel.html');


const initialState = {
    flowDiagrams: [],
    flowDiagramEntities: [],
    diagrams: [],
    createDiagramCommands: () => ([]),
    reload: () => console.log('wfdp:reload - default (do nothing) handler'),
    visibility: {
        diagram: false,
        editor: false
    }
};



function controller(flowDiagramStateService) {
    const vm = initialiseData(this, initialState);

    const showReadOnlyDiagram = () => {
        vm.visibility.diagram = true;
        vm.visibility.editor = false;
    };

    const showEditableDiagram = () => {
        vm.visibility.diagram = false;
        vm.visibility.editor = true;
    };

    const hideDiagram = () => {
        vm.visibility.diagram = false;
        vm.visibility.editor = false;
    };

    vm.$onChanges = () => {
        if(vm.flowDiagrams && vm.flowDiagramEntities) {
            const flowEntitiesDiagramId = _.keyBy(vm.flowDiagramEntities, 'diagramId');
            vm.diagrams = _.map(
                vm.flowDiagrams,
                d => Object.assign(
                    {},
                    d,
                    { notable: flowEntitiesDiagramId[d.id].isNotable || false }));
        }
    };

    vm.onDiagramSelect = (diagram) => {
        showReadOnlyDiagram();
        vm.selectedDiagram = diagram;
        flowDiagramStateService.reset()
        flowDiagramStateService
            .load(diagram.id)
            .then(() => console.log('loaded'))
    };

    vm.onDiagramDismiss = () => {
        hideDiagram();
        vm.selectedDiagram = null;
        flowDiagramStateService.reset();
    };

    vm.contextMenus = {};

    vm.createDiagram = () => {
        flowDiagramStateService
            .reset();

        showEditableDiagram();

        flowDiagramStateService.processCommands(vm.createDiagramCommands() || []);
        setTimeout(() => flowDiagramStateService.processCommands([]), 0);
    };

    vm.editDiagram = () => {
        showEditableDiagram();
        flowDiagramStateService.processCommands([
            { command: 'SET_TITLE', payload: vm.selectedDiagram.name }
        ]);
    };


    vm.dismissDiagramEditor = () => {
        hideDiagram();
        vm.selectedDiagram = null;
        flowDiagramStateService
            .reset();
        vm.reload();
    };

}


controller.$inject = [
    'FlowDiagramStateService'
];


const component = {
    template,
    bindings,
    controller
};


export default component;