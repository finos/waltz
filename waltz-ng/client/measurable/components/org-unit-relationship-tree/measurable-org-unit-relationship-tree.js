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

import template from "./measurable-org-unit-relationship-tree.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {
    buildHierarchies,
    doSearch,
    prepareSearchNodes,
    reduceToSelectedNodesOnly
} from "../../../common/hierarchy-utils";
import _ from "lodash";


const bindings ={
    parentEntityRef: "<",
    selectedCategory: "<"
};


const initialState = {
    linkToState: "main.measurable.view",
    searchNodes: [],
    searchTerms: "",
};


function controller($q, serviceBroker) {

    const vm = initialiseData(this, initialState);

    function loadData() {

        const allMeasurablesPromise = serviceBroker
            .loadAppData(CORE_API.MeasurableStore.findAll)
            .then(r => r.data);

        const relatedMeasurablesPromise = serviceBroker
            .loadViewData(
                CORE_API.MeasurableStore.findByOrgUnitId,
                [vm.parentEntityRef.id])
            .then(r => r.data);

        const orgUnitPromise = serviceBroker
            .loadAppData(CORE_API.OrgUnitStore.findAll)
            .then(r => r.data);

        $q
            .all([allMeasurablesPromise, relatedMeasurablesPromise, orgUnitPromise])
            .then(([allMeasurables, relatedMeasurables, orgUnits]) => {

                const relatedMeasurableIds = _.map(relatedMeasurables, d => d.id);
                const orgUnitsById = _.keyBy(orgUnits, d => d.id);

                vm.enrichedNodes = _.chain(allMeasurables)
                    .filter(m => vm.selectedCategory.id === m.categoryId)
                    .map(m => Object.assign({}, m, {
                        direct: _.includes(relatedMeasurableIds, m.id),
                        orgUnit: _.get(orgUnitsById, m.organisationalUnitId, null) }))
                    .value();

                vm.directNodes = reduceToSelectedNodesOnly(vm.enrichedNodes, relatedMeasurableIds);

                return vm.hierarchy = buildHierarchies(vm.directNodes, false)

            })
            .then(() => vm.searchNodes = prepareSearchNodes(vm.directNodes));
    }

    vm.$onInit = () => {
        loadData();
    };

    vm.$onChanges = (c) => {
        if(c.selectedCategory){
            loadData();
        }
    };

    vm.searchTermsChanged = (termStr = "") => {
        if (termStr === "") {
            vm.hierarchy = buildHierarchies(vm.directNodes, false);
            vm.expandedNodes = [];
        } else {
            const matchedNodes = doSearch(termStr, vm.searchNodes);
            vm.hierarchy = buildHierarchies(matchedNodes, false);
            vm.expandedNodes = matchedNodes;
        }
    };

    vm.clearSearch = () => {
        vm.searchTermsChanged("");
        vm.searchTerms = "";
    };

}


controller.$inject = [
    "$q",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzMeasurableOrgUnitRelationshipTree"
};
