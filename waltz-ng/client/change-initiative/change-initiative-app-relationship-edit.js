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
import {groupRelationships, enrichRelationships} from "./change-initiative-utils";
import * as _ from "lodash";

const initialState = {
    parentEntityRef: null,
    currentRelationships: [],
    allowedRelationships: [{
        value: 'DEPRECATES',
        name: 'Deprecates'
    }, {
        value: 'SUPPORTS',
        name: 'Supports'
    }, {
        value: 'PARTICIPATES_IN',
        name: 'Participates In'
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
                    changeInitiativeStore,
                    notification) {

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

    const loadRelationships = () => {
        return changeInitiativeStore
            .findRelatedForId(id)
            .then(rels => loadRelatedEntities(id, rels))
            .then(curRels => vm.currentRelationships = curRels);
    };


    vm.$onInit = () => {
        changeInitiativeStore.getById(id)
            .then(ci => vm.entityRef.name = ci.name);
        loadRelationships();
    };


    vm.onAdd = (entityRel) => {

        changeInitiativeStore.changeRelationship(id, mkChangeCommand('ADD', entityRel.entity, entityRel.relationship))
            .then(result => {
                if(result) {
                    notification.success("Relationship added successfully");
                } else {
                    notification.warning("Failed to add relationship")
                }
                loadRelationships();
            });
    };

    vm.onRemove = (entityRel) => {

        changeInitiativeStore.changeRelationship(id, mkChangeCommand('REMOVE', entityRel.entity, entityRel.relationship))
            .then(result => {
                if(result) {
                    notification.success("Relationship removed successfully");
                } else {
                    notification.warning("Failed to remove relationship")
                }
                loadRelationships();
            });
    };
}


controller.$inject = [
    '$stateParams',
    'ApplicationStore',
    'ChangeInitiativeStore',
    'Notification'
];


const page = {
    template: require('./change-initiative-app-relationship-edit.html'),
    controller,
    controllerAs: 'ctrl'
};


export default page;

