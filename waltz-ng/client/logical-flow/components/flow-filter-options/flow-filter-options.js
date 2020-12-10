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
import {buildHierarchies} from "../../../common/hierarchy-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import template from "./flow-filter-options.html";
import {getSelectedTagsFromPreferences, saveTagFilterPreferences} from "../../logical-flow-utils";
import {groupLogicalFlowFilterExcludedTagIdsKey} from "../../../user/services/user-preference-service";

const bindings = {
    onChange: "<",
    usedTypes: "<", // [ dataTypeId... ],
    usedTags: "<" // [ {name, id, tagUsages}... ]
};


const initialState = {
    selectedType: "ALL",
    selectedScope: "ALL",
    selectedTags: [],
    visibility: {
        tree: false
    },
    onChange: () => console.log("No change handler registered for flow-filter-options-overlay::onChange")
};


function buildHierarchyWithUsageEnrichment(vm) {
    const usedTypeIds = _.map(vm.usedTypes, "id");
    const enrichedDataTypes = _.map(
        vm.allDataTypes,
        dt => Object.assign({}, dt, {isUsed: _.includes(usedTypeIds, dt.id)}));
    const hierarchy = buildHierarchies(enrichedDataTypes, false);
    return hierarchy;
}


function controller(userPreferenceService, serviceBroker) {
    const vm = _.defaults(this, initialState);

    function loadDataTypes() {
        return serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => vm.allDataTypes = r.data);
    }

    vm.onShowAllTypes = () => {
        vm.selectedType = "ALL";
        vm.visibility.tree = false;
        vm.notifyChanges();
    };

    vm.onShowTree = () => {
        vm.visibility.tree = true;
    };

    vm.onSelectType = (id) => {
        vm.selectedType = id;
        vm.notifyChanges()
    };

    vm.onTagsChange = () => {
        vm.notifyChanges();

        saveTagFilterPreferences(
            vm.usedTags,
            vm.selectedTags,
            groupLogicalFlowFilterExcludedTagIdsKey,
            userPreferenceService);
    };

    vm.showAllTags = () => {
        vm.selectedTags = vm.usedTags;
        vm.notifyChanges();

        saveTagFilterPreferences(
            vm.usedTags,
            vm.selectedTags,
            groupLogicalFlowFilterExcludedTagIdsKey,
            userPreferenceService);
    };

    vm.$onChanges = () => {
        if (!_.isEmpty(vm.usedTypes)) {
            loadDataTypes()
                .then(() => {
                    const hierarchy = buildHierarchyWithUsageEnrichment(vm);
                    vm.hierarchy = hierarchy;
                    vm.notifyChanges();
                });
        }

        if (!_.isEmpty(vm.usedTags)) {
            getSelectedTagsFromPreferences(
                vm.usedTags,
                groupLogicalFlowFilterExcludedTagIdsKey,
                userPreferenceService)
                .then(selectedTags => {
                    vm.selectedTags = selectedTags;
                    vm.notifyChanges();
                });
        }
    };

    vm.notifyChanges = () => {
        const options = {
            typeIds: [vm.selectedType || "ALL"],
            scope: vm.selectedScope || "ALL",
            selectedTags: vm.selectedTags || []
        };
        vm.onChange(options);
    };
    // -- BOOT ---

    vm.notifyChanges();
}


controller.$inject = [
    "UserPreferenceService",
    "ServiceBroker"
];


const component = {
    controller,
    bindings,
    template
};


export default component;