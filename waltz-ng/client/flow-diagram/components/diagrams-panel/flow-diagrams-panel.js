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
};


const template = require('./flow-diagrams-panel.html');


const initialState = {
    flowDiagrams: [],
    flowDiagramEntities: [],
    diagrams: [],
    visibility: {
        diagram: false
    }
};



function controller(flowDiagramStateService) {
    const vm = initialiseData(this, initialState);

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
        vm.visibility.diagram = true;
        vm.selectedDiagram = diagram;
        flowDiagramStateService.reset()
        flowDiagramStateService
            .load(diagram.id)
            .then(() => console.log('loaded'))
    };

    vm.onDiagramDismiss = () => {
        vm.visibility.diagram = false;
        vm.selectedDiagram = null;
        flowDiagramStateService.reset();
    };

    vm.contextMenus = {};

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