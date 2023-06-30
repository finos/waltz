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

import template from "./entity-involvement-editor.html";
import {displayError} from "../../../common/error-utils";

const bindings = {
    allowedInvolvements: "<",
    currentInvolvements: "<",
    parentEntityRef: "<",
    targetEntityKind: "<",
    onAdd: "<",
    onRemove: "<"
};


const initialState = {
    allowedInvolvements: [],
    currentInvolvement: {
        involvement: null,
        entity: null
    },
    currentInvolvements: [],
    parentEntityRef: null,
    targetEntityKind: null,
    targetEntityDisplayName: null,
    onAdd: () => console.log("default onAdd handler for entity-involvement-editor"),
    onRemove: () => console.log("default onRemove handler for entity-involvement-editor")
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        if(changes.targetEntityKind) {
            vm.targetEntityDisplayName = _.toLower(getEnumName(entity, vm.targetEntityKind)) + "s";
        }
    };

    vm.onEntitySelect = (entity) => {
        vm.currentInvolvement.entity = entity;
    };

    vm.isCurrentInvolvementValid = () => {
        return vm.currentInvolvement
            && vm.currentInvolvement.entity
            && vm.currentInvolvement.involvement;
    };

    vm.onInvolvementAdd = () => {
        const currentInvolvement = vm.currentInvolvement;
        invokeFunction(vm.onAdd, currentInvolvement)
        vm.currentInvolvement = {
            involvement: null,
            entity: null
        };
    };

}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;
