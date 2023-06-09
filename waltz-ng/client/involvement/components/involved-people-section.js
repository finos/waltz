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
import {initialiseData} from "../../common";

import template from "./involved-people-section.html";
import {CORE_API} from "../../common/services/core-api-utils";
import {aggregatePeopleInvolvements} from "../involvement-utils";
import {determineUpwardsScopeForKind, mkSelectionOptions} from "../../common/selector-utils";
import {entityLifecycleStatus as EntityLifecycleStatus} from "../../common/services/enums/entity-lifecycle-status";


const bindings = {
    parentEntityRef: "<",
};

const columnDefs = [
    {
        field: "person.displayName",
        displayName: "Name",
        cellTemplate: `
                <div class="ui-grid-cell-contents">
                    <waltz-entity-link entity-ref="row.entity.personRef"
                                       tooltip-placement="right"
                                       icon-placement="none"></waltz-entity-link>
                    -
                    <a href="mailto:{{row.entity.person.email}}">
                        <waltz-icon name="envelope-o"></waltz-icon>
                    </a>
                </div>`
    },
    { field: "person.title", displayName: "Title" },
    { field: "person.officePhone", displayName: "Telephone" },
    {
        field: "rolesDisplayName",
        displayName: "Roles",
        sortingAlgorithm: (a, b) => {
            const aNames = _.join(_.map(a, "displayName"));
            const bNames = _.join(_.map(b, "displayName"));
            return aNames.localeCompare(bNames);
        },
        cellTemplate: `
                <div class="ui-grid-cell-contents">
                    <span ng-bind="COL_FIELD"
                          uib-popover-template="'wips/roles-popup.html'"
                          popover-trigger="mouseenter"
                          popover-append-to-body="true">
                    </span>
                </div>`
    }
];

const initialState = {
    allowedInvolvements: [],
    currentInvolvements: [],
    gridData: [],
    columnDefs,
    exportGrid: () => {},
    visibility: {
        editor: false
    }
};


function mkGridData(involvements = [], displayNameService, descriptionService) {
    return _.chain(involvements)
        .map(inv => {
            const roles = _.map(inv.involvements, pInv => ({
                provenance: pInv.provenance,
                displayName: displayNameService.lookup("involvementKind", pInv.kindId),
                description: descriptionService.lookup("involvementKind", pInv.kindId)
            }));

            const rolesDisplayName = _
                .chain(roles)
                .map("displayName")
                .join(", ")
                .value();

            return {
                person: inv.person,
                personRef: mkEntityRef(inv.person),
                roles,
                rolesDisplayName
            };
        })
        .sortBy("person.displayName")
        .value();
}


function mkEntityRef(person) {
    if (person) {
        return {
            id: person.id,
            name: person.displayName,
            kind: "PERSON",
            entityLifecycleStatus: person.isRemoved ? EntityLifecycleStatus.REMOVED.key : EntityLifecycleStatus.ACTIVE.key
        };
    }
    return person;
}


function mkCurrentInvolvements(involvements = []) {
    return _.flatMap(
        involvements,
        i => {
            const personEntityRef = mkEntityRef(i.person);
            return _.map(i.involvements, inv => ({
                entity: personEntityRef,
                involvement: +inv.kindId,
                isReadOnly: inv.isReadOnly
            }));
        });
}


function controller($q, displayNameService, descriptionService, serviceBroker, involvedSectionService, UserService) {

    const vm = initialiseData(this, initialState);

    const refresh = () => {
        const options = mkSelectionOptions(vm.parentEntityRef, determineUpwardsScopeForKind(vm.parentEntityRef.kind));
        const kindPromise = serviceBroker
            .loadAppData(CORE_API.InvolvementKindStore.findAll, [])
            .then(r => r.data);

        const involvementPromise = serviceBroker
            .loadViewData(
                CORE_API.InvolvementStore.findBySelector,
                [ options ],
                {force: true})
            .then(r => r.data);

        const peoplePromise = serviceBroker
            .loadViewData(
                CORE_API.InvolvementStore.findPeopleBySelector,
                [options],
                {force: true})
            .then(r => r.data);

        const userRolesPromise = UserService
            .whoami()
            .then(user => user.roles);

        return $q
            .all([involvementPromise, peoplePromise, kindPromise, userRolesPromise])
            .then(([involvements = [], people = [], involvementKinds = [], userRoles = []]) => {
                const aggInvolvements = aggregatePeopleInvolvements(involvements, people);
                vm.gridData = mkGridData(aggInvolvements, displayNameService, descriptionService);
                vm.currentInvolvements = mkCurrentInvolvements(aggInvolvements);
                vm.involvementKinds = involvementKinds;

                vm.allowedInvolvements = _
                    .chain(involvementKinds)
                    .filter(ik => ik.userSelectable)
                    .filter(ik => ik.subjectKind === vm.parentEntityRef.kind)
                    .filter(ik => _.isEmpty(ik.permittedRole) || _.includes(userRoles, ik.permittedRole))
                    .map(ik => ({ value: ik.id, name: ik.name }))
                    .value();
            });
    };


    vm.$onChanges = (changes) => {
        if (changes.parentEntityRef && vm.parentEntityRef) {
            refresh();
        }


    };


    vm.editMode = (editMode) => {
        vm.visibility.editor = editMode;
    };


    vm.onAdd = (entityInvolvement) => {
        return involvedSectionService
            .addInvolvement(vm.parentEntityRef, entityInvolvement)
            .then(refresh);
    };


    vm.onRemove = (entityInvolvement) => {
        return involvedSectionService
            .removeInvolvement(vm.parentEntityRef, entityInvolvement)
            .then(refresh);
    };
}


controller.$inject = [
    "$q",
    "DisplayNameService",
    "DescriptionService",
    "ServiceBroker",
    "InvolvedSectionService",
    "UserService"
];


const component = {
    bindings,
    template,
    controller
};

export default component;
