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
import {entityLifecycleStatus as EntityLifecycleStatus} from "../../common/services/enums/entity-lifecycle-status";
import {isHierarchicalKind} from "../../common/selector-utils";


const bindings = {
    parentEntityRef: "<",
};

const entityColDef = {
    displayName: "Entity",
    field: "involvement",
    cellTemplate: `
        <div class="ui-grid-cell-contents">
            <waltz-icon fixed-width="true"
                        title="{{row.entity.directionTooltip}}"
                        name="{{row.entity.directionIcon}}">
            </waltz-icon>
            <waltz-entity-link entity-ref="row.entity.involvement.entityReference"
                               tooltip-placement="right"
                               icon-placement="none">
            </waltz-entity-link>
        </div>`
};

const personColDef = {
    field: "person.displayName",
    displayName: "Name",
    cellTemplate: `
        <div class="ui-grid-cell-contents">
            <waltz-entity-link entity-ref="row.entity.person"
                               tooltip-placement="right"
                               icon-placement="none">
            </waltz-entity-link>
            -
            <a href="mailto:{{row.entity.person.email}}">
                <waltz-icon name="envelope-o"></waltz-icon>
            </a>
        </div>`
};


function mkColumnDefs(isHierarchical = false) {
    return _.compact([
        isHierarchical ? entityColDef : null,
        personColDef,
        {field: "person.title", displayName: "Title"},
        {field: "person.officePhone", displayName: "Telephone"},
        {field: "involvementKind.name", displayName: "Role"}
    ]);
}


const initialState = {
    allowedInvolvements: [],
    currentInvolvements: [],
    rawGridData: [],
    gridData: [],
    columnDefs: [],
    visibility: {
        editor: false
    },
    isHierarchical: false,
    showDirectOnly: false,
    showRemovedPeople: false
};


function mkGridData(raw) {
    return _
        .chain(raw)
        .flatMap(
            (xs, key) => _.map(
                xs,
                x => {
                    const direction = toDirection(key);
                    const directionIcon = toDirectionIcon(direction);
                    const directionTooltip = toDirectionTooltip(direction);

                    const person = Object.assign(
                        {},
                        x.person,
                        {
                            entityLifecycleStatus: x.person.isRemoved ? EntityLifecycleStatus.REMOVED.key : EntityLifecycleStatus.ACTIVE.key
                        });

                    const additionalProps = {
                        direction,
                        directionIcon,
                        directionTooltip,
                        person
                    };

                    return Object.assign({}, x, additionalProps);
                }))
        .filter()
        .value();
}


function toDirection(key) {
    switch (key) {
        case "descendents":
            return "DESCENDENT";
        case "ancestors":
            return "ANCESTOR";
        default:
            return "EXACT";
    }
}


function toDirectionIcon(dir) {
    switch (dir) {
        case "DESCENDENT":
            return "arrow-turn-down";
        case "ANCESTOR":
            return "arrow-turn-up";
        default:
            return "equals";
    }
}

function toDirectionTooltip(dir) {
    switch (dir) {
        case "DESCENDENT":
            return "Inherited from a descendant entity at a lower level";
        case "ANCESTOR":
            return "Inherited from a ancestor entity at a higher level";
        default:
            return "Directly linked to this entity";
    }
}

function controller($q, displayNameService, descriptionService, serviceBroker, involvedSectionService) {

    const vm = initialiseData(this, initialState);


    const applyFilters = () => {
        vm.gridData = _
            .chain(vm.rawGridData)
            .filter(d => vm.showRemovedPeople
                ? true
                : !d.person.isRemoved)
            .filter(d => vm.showDirectOnly
                ? d.direction === "EXACT"
                : true)
            .value();
    };

    const refresh = () => {
        serviceBroker
            .loadAppData(
                CORE_API.InvolvementViewService.findAllInvolvementsForEntityByDirection,
                [vm.parentEntityRef],
                {force: true})
            .then(r => {
                vm.rawGridData = mkGridData(r.data);
                vm.currentInvolvements = _
                    .chain(vm.rawGridData)
                    .filter(d => d.direction === "EXACT")
                    .value();
                applyFilters();
                console.log({raw: vm.rawGridData, curr: vm.currentInvolvements})
            });
    };


    vm.$onChanges = (changes) => {
        if (changes.parentEntityRef && vm.parentEntityRef) {
            vm.isHierarchical = isHierarchicalKind(vm.parentEntityRef.kind);
            vm.columnDefs = mkColumnDefs(vm.isHierarchical);
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


    vm.onRemove = (involvementDetail) => {
        return involvedSectionService
            .removeInvolvement(vm.parentEntityRef, involvementDetail)
            .then(refresh);
    };

    vm.onToggleScope = () => {
        vm.showDirectOnly = !vm.showDirectOnly;
        applyFilters();
    };

    vm.onHiddenPeople= ()=>{
        vm.showRemovedPeople = !vm.showRemovedPeople;
        applyFilters();
    };
}


controller.$inject = [
    "$q",
    "DisplayNameService",
    "DescriptionService",
    "ServiceBroker",
    "InvolvedSectionService"
];


const component = {
    bindings,
    template,
    controller
};

export default component;
