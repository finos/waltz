/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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

import template from "./related-app-groups-section.html";

import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import {sameRef} from "../../../common/entity-utils";
import {isGroupOwner} from "../../app-group-utils";
import {getEditRoleForEntityKind} from "../../../common/role-utils";
import {mkRel} from "../../../common/relationship-utils";


const bindings = {
    parentEntityRef: "<"
};

const modes = {
    edit: "edit",
    view: "view"
};

const initialState = {
    currentlySelectedGroup: null,
    relationships: [],
    editable: false,
    mode: modes.view
};


function canEdit(serviceBroker, UserService, entityRef) {
    switch(entityRef.kind){
        case "APP_GROUP":
            return isGroupOwner(serviceBroker, entityRef);
        case "CHANGE_INITIATIVE":
            return Promise.resolve(true);
        case "ROADMAP":
            const role = getEditRoleForEntityKind("ROADMAP");
            return UserService
                .whoami()
                .then(user => UserService.hasRole(user, role));
        default:
            return Promise.resolve(false);
    }
}


function loadRelationshipData($q, serviceBroker, entityRef) {
    const relationshipPromise = serviceBroker
        .loadViewData(
            CORE_API.EntityRelationshipStore.findForEntity,
            [entityRef, "ANY"],
            { force: true })
        .then(r => r.data);

    const groupPromise = serviceBroker
        .loadViewData(
            CORE_API.AppGroupStore.findRelatedByEntityRef,
            [ entityRef ],
            { force: true })
        .then(r => _.reject(r.data, relatedGroup => sameRef(relatedGroup, entityRef)));

    return $q
        .all([relationshipPromise, groupPromise])
        .then(([relationships, groups]) => {
            const groupsById = _.keyBy(groups, "id");
            return _
                .chain(relationships)
                .map(rel => {
                    return sameRef(entityRef, rel.a, { skipChecks: true })
                        ? {counterpartRef: rel.b, side: "TARGET", relationship: rel}
                        : {counterpartRef: rel.a, side: "SOURCE", relationship: rel};
                })
                .filter(rel => rel.counterpartRef.kind === "APP_GROUP")
                .map(rel => Object.assign({}, rel, { counterpart: groupsById[rel.counterpartRef.id] }))
                .filter(rel => rel.counterpart !== null)
                .uniqBy(rel => rel.counterpart.id)
                .orderBy("counterpart.name")
                .value();
        });
}


function canGroupBeProposed(relationships = [], proposedGroup, selfRef) {
    const proposalId = proposedGroup.id;
    const alreadyLinked = _.some(relationships, r => r.counterpart.id === proposalId);

    return ! alreadyLinked && ! sameRef(proposedGroup, selfRef);
}


function controller($q, notification, serviceBroker, UserService) {
    const vm = initialiseData(this, initialState);

    const reload = () => loadRelationshipData($q, serviceBroker, vm.parentEntityRef)
        .then(r => vm.relationships = r);

    vm.$onInit = () => {
        reload();
        canEdit(serviceBroker, UserService, vm.parentEntityRef)
            .then(r => vm.editable = r);

    };


    vm.groupSelectionFilter = (proposedGroup) => canGroupBeProposed(
        vm.relationships,
        proposedGroup,
        vm.parentEntityRef);


    vm.onRemove = (rel) => {
        return serviceBroker
            .execute(CORE_API.EntityRelationshipStore.remove, [ rel ])
            .then(() => {
                notification.info("Relationship removed");
                reload();
            })
            .catch(e => notification.error("Failed to remove! " + e.message))
    };


    vm.onAdd = () => {
        if (vm.currentlySelectedGroup === null) {
            return;
        }

        const newRel = mkRel(vm.parentEntityRef, "RELATES_TO", vm.currentlySelectedGroup);

        return serviceBroker
            .execute(CORE_API.EntityRelationshipStore.create, [ newRel ])
            .then(() => {
                notification.info("Relationship created");
                vm.currentlySelectedGroup = null;
                reload();
            })
            .catch(e => notification.error("Failed to add! " + e.message))
    };


    vm.onGroupSelected = (g) => vm.currentlySelectedGroup = g;
    vm.onEdit = () => vm.mode = modes.edit;
    vm.onCancel = () => vm.mode = modes.view;
}


controller.$inject = [
    "$q",
    "Notification",
    "ServiceBroker",
    "UserService"
];


const component = {
    controller,
    bindings,
    template
};


const id = "waltzRelatedAppGroupsSection";


export default {
    id,
    component
}