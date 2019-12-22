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

import template from "./tag-physical-flow-view.html";
import {initialiseData} from "../../common";
import {CORE_API} from "../../common/services/core-api-utils";
import {columnDef} from "../../physical-flow/physical-flow-table-utilities";
import {dynamicSections} from "../../dynamic-section/dynamic-section-definitions";

const initialState = {
    bookmarksSection: dynamicSections.bookmarksSection
};

function controller($stateParams, serviceBroker) {
    const vm = initialiseData(this, initialState);
    const id = $stateParams.id;

    vm.entityReference = {id, kind: "TAG"};

    vm.$onInit = () => {
        serviceBroker
            .loadViewData(
                CORE_API.TagStore.getTagById,
                [id])
            .then(r => vm.tag = r.data);
    };

    vm.physicalFlowColumnDefs = [
        columnDef.name,
        columnDef.source,
        columnDef.target,
        columnDef.extId,
        columnDef.observation,
        columnDef.frequency,
        columnDef.description
    ];
}

controller.$inject = ["$stateParams", "ServiceBroker"];


export default {
    template,
    controller,
    controllerAs: "ctrl",
    bindToController: true,
    scope: {}
};

