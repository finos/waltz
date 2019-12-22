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

import template from "./related-app-groups-section.html";

import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import {sameRef} from "../../../common/entity-utils";
import {isGroupOwner} from "../../app-group-utils";
import {getEditRoleForEntityKind} from "../../../common/role-utils";
import {mkRel} from "../../../common/relationship-utils";
import {displayError} from "../../../common/error-utils";


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
            const role = getEditRoleForEntityKind("ROADMAP", "APP_GROUP");
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
                .filter(rel => rel.counterpart != null)
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
            .catch(e => displayError(notification, "Failed to remove", e))
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
            .catch(e => displayError(notification, "Failed to add", e))
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