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

import template from "./related-org-unit-section.html";

import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import {sameRef} from "../../../common/entity-utils";
import {getEditRoleForEntityKind} from "../../../common/role-utils";
import {mkRel} from "../../../common/relationship-utils";


const bindings = {
    parentEntityRef: "<"
};


const modes = {
    view: "view",
    edit: "edit"
};


const initialState = {
    currentlySelected: null,
    relationships: [],
    editable: false,
    mode: modes.view
};


function canEdit(UserService, entityRef) {
    const kind = entityRef.kind;
    switch(kind){
        case "ROADMAP":
            const role = getEditRoleForEntityKind(kind);
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

    const orgUnitsPromise = serviceBroker
        .loadViewData(
            CORE_API.OrgUnitStore.findRelatedByEntityRef,
            [ entityRef ],
            { force: true })
        .then(r => _.reject(r.data, ou => sameRef(ou, entityRef, { skipChecks: true })));

    return $q
        .all([relationshipPromise, orgUnitsPromise])
        .then(([relationships, orgUnits]) => {
            const groupsById = _.keyBy(orgUnits, "id");
            return _
                .chain(relationships)
                .map(rel => {
                    return sameRef(entityRef, rel.a, { skipChecks: true })
                        ? {counterpartRef: rel.b, side: "TARGET", relationship: rel}
                        : {counterpartRef: rel.a, side: "SOURCE", relationship: rel};
                })
                .filter(rel => rel.counterpartRef.kind === "ORG_UNIT")
                .map(rel => Object.assign({}, rel, { counterpart: groupsById[rel.counterpartRef.id] }))
                .filter(rel => rel.counterpart !== null)
                .uniqBy(rel => rel.counterpart.id)
                .orderBy("counterpart.name")
                .value();
        });
}


function canOrgUnitBeProposed(relationships = [], proposedOrgUnit, selfRef) {
    const proposalId = proposedOrgUnit.id;
    const alreadyLinked = _.some(relationships, r => r.counterpart.id === proposalId);

    return ! alreadyLinked && ! sameRef(proposedOrgUnit, selfRef);
}


function controller($q, notification, serviceBroker, UserService) {
    const vm = initialiseData(this, initialState);

    const reload = () => loadRelationshipData($q, serviceBroker, vm.parentEntityRef)
        .then(r => vm.relationships = r);

    vm.$onInit = () => {
        reload();
        canEdit(UserService, vm.parentEntityRef)
            .then(r => vm.editable = r);
    };

    vm.selectionFilter = (proposed) => canOrgUnitBeProposed(
        vm.relationships,
        proposed,
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
        if (vm.currentlySelected === null) {
            return;
        }

        const newRel = mkRel(vm.parentEntityRef, "RELATES_TO", vm.currentlySelected);

        return serviceBroker
            .execute(CORE_API.EntityRelationshipStore.create, [ newRel ])
            .then(() => {
                notification.info("Relationship created");
                vm.currentlySelected = null;
                reload();
            })
            .catch(e => notification.error("Failed to add! " + e.message))
    };


    vm.onSelected = (g) => vm.currentlySelected = g;
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


const id = "waltzRelatedOrgUnitSection";


export default {
    id,
    component
}