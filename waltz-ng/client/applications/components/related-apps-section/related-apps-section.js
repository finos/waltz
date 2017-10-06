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
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {sameRef} from "../../../common/entity-utils";

import template from './related-apps-section.html';


const bindings = {
    editRole: '@?',
    parentEntityRef: '<',
};


const initialState = {
    columnDefs: [
        mkLinkGridCell('Name', 'app.name', 'app.id', 'main.app.view'),
        { field: 'relationshipDisplay', name: 'Relationship'},
        { field: 'app.assetCode'},
        { field: 'app.kindDisplay', name: 'Kind'},
        { field: 'app.overallRatingDisplay', name: 'Overall Rating'},
        { field: 'app.businessCriticalityDisplay', name: 'Business Criticality'},
        { field: 'app.lifecyclePhaseDisplay', name: 'Lifecycle Phase'},
    ],
    appRelationships: [],
    sourceDataRatings: []
};


function mkGridData(relations = [], apps = []) {
    const appsById = _.keyBy(apps, 'id');

    return _.map(relations, r => ({
        relationshipDisplay: relationshipKindNames[r.relationship],
        app: appsById[r.entity.id]
    }));
}


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);


    function loadData() {
        const relationsPromise = serviceBroker
            .loadViewData(
                CORE_API.ChangeInitiativeStore.findRelatedForId,
                [ vm.parentEntityRef.id ])
            .then(r => _
                .chain(r.data)
                .flatMap(rel => ([
                    {entity: rel.a, relationship: rel.relationship},
                    {entity: rel.b, relationship: rel.relationship}
                ]))
                .filter(rel => rel.entity.kind === 'APPLICATION')
                .reject(rel => sameRef(rel.entity, vm.parentEntityRef))
                .value());

        const appsPromise = serviceBroker.loadViewData(
            CORE_API.ApplicationStore.findBySelector,
            [ mkSelectionOptions(vm.parentEntityRef) ])
            .then(r => _.map(r.data, a => Object.assign({}, a, mapToDisplayNames(a))));

        $q.all([appsPromise, relationsPromise])
            .then(([apps, relations]) => {
                vm.gridData = mkGridData(relations, apps);
            });
    }

    vm.$onChanges= (c) => {
        if (vm.parentEntityRef) {
            vm.editRouteState = kindToBaseState(vm.parentEntityRef.kind) + '.app-relationship-edit';
            loadData();
        }
    };

    vm.onInitialise = (cfg) => {
        vm.export = () => cfg.exportFn(`app-relationships.csv`);
    };
}


controller.$inject = [
    '$q',
    'ServiceBroker'
];


const component = {
    template,
    bindings,
    controller
};

const id = 'waltzRelatedAppsSection';


export default {
    id,
    component
};