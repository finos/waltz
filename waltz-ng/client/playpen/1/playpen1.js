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


import template from "./playpen1.html";
import {initialiseData} from "../../common";
import InvolvementPicker from "../../common/svelte/entity-pickers/InvolvementPicker.svelte";
import EntityPicker from "../../report-grid/components/svelte/pickers/EntityPicker.svelte";
import EntitySelector from "../../report-grid/components/svelte/column-definition-edit-panel/EntitySelector.svelte";
import TestPage
    from "../../report-grid/components/svelte/column-definition-edit-panel/ColumnDefinitionEditPanel.svelte";
import {mkSelectionOptions} from "../../common/selector-utils";
import TaxonomyNavAidBuilder from "./TaxonomyNavAidBuilder.svelte";
import {CORE_API} from "../../common/services/core-api-utils";
import _ from "lodash";

const initData = {
    TaxonomyNavAidBuilder,
    taxonomy: []
};


function controller($q,
                    serviceBroker) {

    const vm = initialiseData(this, initData);

    vm.$onInit = () => {

        serviceBroker
            .loadViewData(
                CORE_API.DataTypeStore.findAll,
                //CORE_API.MeasurableStore.findAll,
                [])
            .then(r => {
                console.log(r);
                vm.taxonomy = r.data; // _.filter(r.data, m => m.categoryId === 12);
                vm.linkStem = "data-types"; // data-types or measurable
            });

    }

}

controller.$inject = ["$q", "ServiceBroker", "UserService"];

const view = {
    template,
    controller,
    controllerAs: "$ctrl",
    bindToController: true,
    scope: {}
};

export default view;