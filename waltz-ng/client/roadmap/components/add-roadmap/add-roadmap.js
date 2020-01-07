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

import {initialiseData, notEmpty} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {toEntityRef} from "../../../common/entity-utils";

import template from "./add-roadmap.html";


const bindings = {
    onAddRoadmap: "<",
    onCancel: "<"
};


const initialState = {};


function toCommand(model) {
    if (!validateModel(model)) return null;

    return {
        name: model.name,
        rowType: toEntityRef(model.rowType),
        columnType: toEntityRef(model.columnType),
        ratingSchemeId: model.ratingScheme.id
    };
}


function validateModel(model) {
    return notEmpty(model.name) &&
        notEmpty(model.columnType) &&
        notEmpty(model.rowType) &&
        notEmpty(model.ratingScheme);
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(
                CORE_API.MeasurableCategoryStore.findAll)
            .then(r => {
                vm.axisOptions = r.data;
            });

        serviceBroker
            .loadAppData(
                CORE_API.RatingSchemeStore.findAll)
            .then(r => {
                vm.ratingSchemes = r.data;
            });
    };

    vm.onFormChange = () => {
        vm.preventSubmit = !validateModel(vm.model);
    };

    vm.onAdd = () => {
        const command = toCommand(vm.model);
        return vm.onAddRoadmap(command);
    };
}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    controller,
    template,
    bindings
};


export default {
    id: "waltzAddRoadmap",
    component
};