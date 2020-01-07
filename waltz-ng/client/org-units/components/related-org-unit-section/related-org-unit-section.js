/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */


import _ from "lodash";

import template from "./related-org-unit-section.html";

import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import {sameRef} from "../../../common/entity-utils";
import {getEditRoleForEntityKind} from "../../../common/role-utils";
import {mkRel} from "../../../common/relationship-utils";
import {displayError} from "../../../common/error-utils";


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
            const role = getEditRoleForEntityKind(kind, "ORG_UNIT");
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
                .filter(rel => !_.isNil(rel.counterpart))
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
            .catch(e => displayError(notification, "Failed to remove! ", e.message))
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
            .catch(e => displayError(notification,"Failed to add", e));
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