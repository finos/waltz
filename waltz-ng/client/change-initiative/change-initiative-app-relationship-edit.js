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
import _ from 'lodash';
import {CORE_API} from '../common/services/core-api-utils';

import {groupRelationships, enrichRelationships} from './change-initiative-utils';

import template from './change-initiative-app-relationship-edit.html';


const initialState = {
    parentEntityRef: null,
    currentRelationships: [],
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


function mkChangeCommand(operation, entityRef, relKind) {
    return {
        operation,
        entityReference: entityRef,
        relationship: relKind
    };
}


function controller($stateParams,
                    applicationStore,
                    notification,
                    serviceBroker) {

    const {id} = $stateParams;
    const vm = Object.assign(this, initialState);

    vm.entityRef = {
        kind: 'CHANGE_INITIATIVE',
        id: id
    };

    const loadRelatedEntities = (id, rels = []) => {
        const relationships = groupRelationships(id, rels);
         return applicationStore.findBySelector({
            entityReference: vm.entityRef,
            scope: 'EXACT'
        })
        .then(apps => {
            const enrichedRels = enrichRelationships(relationships.APPLICATION, apps);
            return _.map(enrichedRels, rel => ({
                entity: {
                    id: rel.entity.id,
                    kind: 'APPLICATION',
                    name: rel.entity.name
                },
                relationship: rel.relationship
            }));
        });
    };

    const loadRelationships = (force = false) => {
        serviceBroker
            .loadViewData(CORE_API.ChangeInitiativeStore.findRelatedForId, [id], { force })
            .then(result => loadRelatedEntities(id, result.data))
            .then(curRels => vm.currentRelationships = curRels);
    };


    vm.$onInit = () => {
        serviceBroker
            .loadViewData(CORE_API.ChangeInitiativeStore.getById, [id])
            .then(result => vm.entityRef.name = result.data.name);
        loadRelationships();
    };


    vm.onAdd = (entityRel) => {
        serviceBroker
            .execute(
                CORE_API.ChangeInitiativeStore.changeRelationship,
                [id, mkChangeCommand('ADD', entityRel.entity, entityRel.relationship)])
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
                [id, mkChangeCommand('REMOVE', entityRel.entity, entityRel.relationship)])
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
    '$stateParams',
    'ApplicationStore',
    'Notification',
    'ServiceBroker'
];


const page = {
    template,
    controller,
    controllerAs: 'ctrl'
};


export default page;

