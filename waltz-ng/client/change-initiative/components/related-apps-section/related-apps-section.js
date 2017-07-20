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
import {initialiseData} from "../../../common/index";
import {mkLinkGridCell} from "../../../common/link-utils";
import {mapToDisplayNames} from "../../../applications/application-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import {relationshipKindNames} from "../../../common/services/display-names";
import {enrichRelationships, groupRelationships} from "../../change-initiative-utils";


import template from './related-apps-section.html';


const bindings = {
    parentEntityRef: '<'
};


const initialState = {
    appRelationships: [],
    entityRelationships: [],
    visibility: {
        editor: false
    },
    allowedRelationships: [{
        value: 'APPLICATION_NEW',
        name: 'Application - new'
    }, {
        value: 'APPLICATION_FUNCTIONAL_CHANGE',
        name: 'Application - functional change'
    }, {
        value: 'APPLICATION_DECOMMISSIONED',
        name: 'Application - decommissioned'
    }, {
        value: 'APPLICATION_NFR_CHANGE',
        name: 'Application - NFR change'
    }, {
        value: 'DATA_PUBLISHER',
        name: 'Data publisher'
    }, {
        value: 'DATA_CONSUMER',
        name: 'Data consumer'
    }]
};


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


function mkChangeCommand(operation, entityRef, relKind) {
    return {
        operation,
        entityReference: {
            id: entityRef.id,
            kind: 'APPLICATION'
        },
        relationship: relKind
    };
}


function controller(notification,
                    serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadRelatedEntities = (id, rels = [], force = false) => {
        const relationships = groupRelationships(id, rels);
        return serviceBroker
            .loadViewData(CORE_API.ApplicationStore.findBySelector, [{
                entityReference: vm.parentEntityRef,
                scope: 'EXACT'
            }], { force })
            .then(result =>  enrichRelationships(relationships.APPLICATION, result.data));
    };

    const loadRelationships = (force = false) => {
        return serviceBroker
            .loadViewData(CORE_API.ChangeInitiativeStore.findRelatedForId, [vm.parentEntityRef.id], { force })
            .then(result => loadRelatedEntities(vm.parentEntityRef.id, result.data, force))
            .then(curRels => vm.appRelationships = curRels)
            .then(() => vm.gridData = mkGridData(vm.appRelationships))
            .then(() => vm.entityRelationships = _.map(vm.appRelationships, r => Object.assign({}, {
                relationship: r.relationship,
                entity: {
                    id: r.entity.id,
                    name: r.entity.name,
                    kind: 'APPLICATION'
                }
            })));
    };

    vm.$onChanges= () => {
        if (vm.parentEntityRef) {
            loadRelationships(false);
        }
    };

    vm.columnDefs = columnDefs;

    vm.onInitialise = (cfg) => {
        vm.export = () => cfg.exportFn(`app-relationships.csv`);
    };

    vm.editMode = (editMode) => {
        vm.visibility.editor = editMode;
    };

    vm.onAdd = (entityRel) => {
        serviceBroker
            .execute(
                CORE_API.ChangeInitiativeStore.changeRelationship,
                [vm.parentEntityRef.id, mkChangeCommand('ADD', entityRel.entity, entityRel.relationship)])
            .then(result => {
                if(result.data) {
                    notification.success("Relationship added successfully");
                } else {
                    notification.warning("Failed to add relationship")
                }
                loadRelationships(true);
            });
    };

    vm.onRemove = (entityRel) => {
        serviceBroker
            .execute(
                CORE_API.ChangeInitiativeStore.changeRelationship,
                [vm.parentEntityRef.id, mkChangeCommand('REMOVE', entityRel.entity, entityRel.relationship)])
            .then(result => {
                if(result.data) {
                    notification.success("Relationship removed successfully");
                } else {
                    notification.warning("Failed to remove relationship")
                }
                loadRelationships(true);
            });
    };
}


controller.$inject = [
    'Notification',
    'ServiceBroker'
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzChangeInitiativeRelatedAppsSection'
};
