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
import {initialiseData, invokeFunction} from "../../../common";
import {entity} from "../../../common/services/enums/entity";
import {getEnumName} from "../../../common/services/enums";
import template from './related-entity-editor.html';


const bindings = {
    allowedRelationships: '<',
    currentRelationships: '<',
    parentEntityRef: '<',
    targetEntityKind: '<',

    onAdd: '<',
    onRemove: '<'
};


const initialState = {
    allowedRelationships: [],
    dropdownEntries: [],
    currentRelationship: {},
    currentRelationships: [],
    parentEntityRef: null,
    targetEntityKind: null,
    targetEntityDisplayName: null,

    onAdd: () => console.log("default onAdd handler for related-entity-editor"),
    onRemove: () => console.log("default onAdd handler for related-entity-editor")
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        if(changes.targetEntityKind) {
            vm.targetEntityDisplayName = _.toLower(getEnumName(entity, vm.targetEntityKind)) + "s";
        }

        vm.dropdownEntries = _.map(vm.allowedRelationships, r => ({ code: r.value, name: r.name}) );
    };

    vm.onEntitySelect = (entity) => {
        vm.currentRelationship.entity = entity;
    };

    vm.isCurrentRelationshipValid = () => {
        return vm.currentRelationship
            && vm.currentRelationship.entity
            && vm.currentRelationship.relationship;
    };

    vm.onRelationshipAdd = () => {
        const currentRelationship = vm.currentRelationship;
        return invokeFunction(vm.onAdd, currentRelationship)
            .then(() => vm.currentRelationship = {});
    };

    vm.onRelationshipEdit = (value, comments, ctx) => {
        vm.currentRelationship = {
            entity: ctx.entity,
            relationship: value
        };
        return vm.onRemove(ctx)
            .then(() => vm.onRelationshipAdd());
    };
}


controller.$inject = [
];


const component = {
    bindings,
    template,
    controller
};


export default component;
