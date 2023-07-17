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
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import template from "./measurable-category-list.html";
import {lastViewedMeasurableCategoryKey} from "../../../user";
import roles from "../../../user/system-roles";
import {dynamicSections} from "../../../dynamic-section/dynamic-section-definitions";
import {toEntityRef} from "../../../common/entity-utils";

const initialState = {
    category: null,
    visibility: {
        editButton: false
    },
    peopleSection: dynamicSections.involvedPeopleSection,
    taxonomyChangesSection: dynamicSections.taxonomyChangesSection
};


function controller($q,
                    $state,
                    $stateParams,
                    serviceBroker,
                    userService) {

    const vm = initialiseData(this, initialState);
    const categoryId = $stateParams.id;
    vm.categoryRef = {
        id: categoryId,
        kind: "MEASURABLE_CATEGORY"
    };


    vm.$onInit = () => {

        // last category is used when initialising the route in route.js
        serviceBroker
            .execute(
                CORE_API.UserPreferenceStore.saveForUser,
                [{key: lastViewedMeasurableCategoryKey, value: categoryId}]);

        const measurablePromise = serviceBroker
            .loadAppData(CORE_API.MeasurableStore.findAll)
            .then(r => {
                vm.measurablesByCategory = _.groupBy(r.data, m => m.categoryId);
                return vm.measurablesByCategory[categoryId];
            });

        serviceBroker
            .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
            .then(r => {
                vm.categories = r.data;
                vm.category = _.find(vm.categories, { id: $stateParams.id });
                vm.categoryRef = toEntityRef(vm.category);
                if (vm.category.editable) {
                    userService
                        .whoami()
                        .then(user => vm.visibility.editButton = userService
                            .hasRole(
                                user,
                                roles.TAXONOMY_EDITOR.key));
                }
            });


        const countPromise = serviceBroker
            .loadViewData(
                CORE_API.MeasurableRatingStore.countByMeasurableCategory,
                [categoryId])
            .then(r => r.data);

        serviceBroker
            .loadAppData(
                CORE_API.SvgDiagramStore.findByGroups,
                [ `NAVAID.MEASURABLE.${categoryId}` ])
            .then(r => vm.diagrams = r.data);


        $q.all([measurablePromise, countPromise])
            .then(([measurables, counts]) => {
                const countsById = _.keyBy(counts, "id");
                const getCount = (id) => _.get(countsById, [id, "count"],  0);

                vm.measurables = _.map(measurables, m => Object.assign(
                    {},
                    m,
                    { directCount: getCount(m.id) }));

                vm.measurablesByExtId = _.keyBy(measurables, "externalId");

                vm.blockProcessor = b => {
                    const extId = b.value;
                    const measurable = vm.measurablesByExtId[extId];
                    if (measurable) {
                        b.block.onclick = () => $state.go("main.measurable.view", { id: measurable.id });
                        angular
                            .element(b.block)
                            .addClass("clickable");
                    } else {
                        console.log(`MeasurableList: Could not find measurable with external id: ${extId}`, b)
                    }
                };
            });
    };

}


controller.$inject = [
    "$q",
    "$state",
    "$stateParams",
    "ServiceBroker",
    "UserService"
];


export default {
    template,
    controller,
    controllerAs: "$ctrl"
};
