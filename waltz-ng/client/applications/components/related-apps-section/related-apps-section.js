/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import _ from "lodash";
import {initialiseData} from "../../../common";
import {mkLinkGridCell, kindToBaseState} from "../../../common/link-utils";
import {mapToDisplayNames} from "../../application-utils";
import {relationshipKindNames} from "../../../common/services/display-names";


const bindings = {
    appRelationships: '<',
    editRole: '@',
    parentEntityRef: '<',
    sourceDataRatings: '<',
};


const initialState = {
    appRelationships: [],
    sourceDataRatings: []
};


const template = require('./related-apps-section.html');


const columnDefs = [
    mkLinkGridCell('Name', 'app.name', 'app.id', 'main.app.view'),
    { field: 'relationshipDisplay', name: 'Relationship'},
    { field: 'app.assetCode'},
    { field: 'app.kindDisplay', name: 'Kind'},
    { field: 'app.overallRatingDisplay', name: 'Overall Rating'},
    { field: 'app.businessCriticalityDisplay', name: 'Business Criticality'},
    { field: 'app.lifecyclePhaseDisplay', name: 'Lifecycle Phase'},
];


function mkGridData(appRelationships = []) {
    return _.map(appRelationships || [], ar => ({
                relationshipDisplay: relationshipKindNames[ar.relationship],
                app: Object.assign({}, ar.entity, mapToDisplayNames(ar.entity))
            }
        )
    );
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges= () => {
        vm.gridData = mkGridData(vm.appRelationships);
        if (vm.parentEntityRef) {
            vm.editRouteState = kindToBaseState(vm.parentEntityRef.kind) + '.app-relationship-edit';
        }
    };

    vm.columnDefs = columnDefs;

    vm.onInitialise = (cfg) => {
        vm.export = () => cfg.exportFn(`app-relationships.csv`);
    };
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default component;