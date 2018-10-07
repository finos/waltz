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
import {CORE_API} from "../../../common/services/core-api-utils";
import {initialiseData} from "../../../common";

import template from "./measurable-rating-app-section.html";
import { determineStartingTab, mkTabs } from "../../measurable-rating-utils";


/**
 * @name waltz-measurable-rating-panel
 *
 * @description
 * This component render multiple <code>measurable-rating-panel</code> components
 * within a tab group.
 *
 * It is intended to be used to show measurables and ratings for a single entity (app or actor).
 */

const bindings = {
    parentEntityRef: "<",
};


const initialState = {
    categories: [],
    ratings: [],
    measurables: [],
    visibility: {
        editor: false,
        overlay: false,
        tab: null
    },
    byCategory: {}
};


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.viewMode = () => {
        loadData(true);
        vm.visibility.editor = false;
    };

    const loadData = (force = false) => {

        serviceBroker
            .loadViewData(
                CORE_API.RoadmapStore.findRoadmapsAndScenariosByRatedEntity,
                [ vm.parentEntityRef ])
            .then(r => vm.roadmapReferences = r.data);

        const ratingsPromise = serviceBroker
            .loadViewData(CORE_API.MeasurableRatingStore.findForEntityReference, [ vm.parentEntityRef ], { force })
            .then(r => vm.ratings = r.data);

        const ratingSchemesPromise = serviceBroker
            .loadAppData(CORE_API.RatingSchemeStore.findAll)
            .then(r => vm.ratingSchemesById = _.keyBy(r.data, "id"));

        const categoriesPromise = serviceBroker
            .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
            .then(r => vm.categories = r.data);

        const measurablesPromise = serviceBroker
            .loadViewData(CORE_API.MeasurableStore.findMeasurablesRelatedToPath, [vm.parentEntityRef], { force })
            .then(r => vm.measurables = r.data);

        $q.all([measurablesPromise, ratingSchemesPromise, ratingsPromise, categoriesPromise])
            .then(() => {
                vm.tabs = mkTabs(
                    vm.categories,
                    vm.ratingSchemesById,
                    vm.measurables,
                    vm.ratings,
                    false /*include empty */);
                const firstNonEmptyTab = determineStartingTab(vm.tabs);
                vm.visibility.tab = firstNonEmptyTab ? firstNonEmptyTab.category.id : null;
            });
    };

    vm.$onInit = () => loadData();

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


export default component;