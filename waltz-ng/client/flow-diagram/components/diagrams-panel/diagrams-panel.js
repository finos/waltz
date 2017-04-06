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
    entityRef: '<',
    flowDiagrams: '<',
    flowDiagramEntities: '<',
};

const template = require('./diagrams-panel.html');


const initialState = {
    flowDiagrams: [],
    flowDiagramEntities: [],
    diagrams: [],
};


const dateCell = {
    field: 'lastUpdatedAt',
    displayName: 'Last Updated',
    enableFiltering: false,
    cellTemplate: '<div class="ui-grid-cell-contents">\n     ' +
                    '<waltz-from-now timestamp="COL_FIELD" days-only="true"></waltz-from-now>\n' +
                  '</div>',
    width: '15%'
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.diagramColumnDefs = [
        { field: 'name', displayName: 'Name', width: "20%" },
        { field: 'description', displayName: 'Description', width: "40%" },
        { field: 'lastUpdatedBy', displayName: 'Editor', width: "15%" },
        dateCell,
        { field: 'notable', displayName: 'Notable', width: "10%", sort: { direction: 'desc', priority: 0 }},

    ];

    vm.$onChanges = () => {
        if(vm.flowDiagrams && vm.flowDiagramEntities) {
            const flowEntitiesDiagramId = _.keyBy(vm.flowDiagramEntities, 'diagramId');
            vm.diagrams = _.map(vm.flowDiagrams, d => Object.assign(
                {},
                d,
                { notable: flowEntitiesDiagramId[d.id].isNotable || false }
            ));
        }

    };


}


controller.$inject = [
];


const component = {
    template,
    bindings,
    controller
};


export default component;