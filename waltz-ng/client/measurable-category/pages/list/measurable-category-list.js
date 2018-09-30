/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import _ from "lodash";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import template from "./measurable-category-list.html";
import {lastViewedMeasurableCategoryKey} from "../../../user/services/user-preference-service";


const initialState = {
    category: null,
};


function controller($q,
                    $state,
                    $stateParams,
                    serviceBroker,
                    userPreferenceService) {

    const vm = initialiseData(this, initialState);
    const categoryId = $stateParams.id;

    vm.$onInit = () => {

        userPreferenceService
            .savePreference(lastViewedMeasurableCategoryKey, categoryId);

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
            });

        const countPromise = serviceBroker
            .loadViewData(CORE_API.MeasurableRatingStore.countByMeasurableCategory, [categoryId])
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
    "UserPreferenceService"
];


export default {
    template,
    controller,
    controllerAs: "$ctrl"
};
