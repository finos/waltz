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
import {mkSelectionOptionsWithJoiningEntity} from "../../../common/selector-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import {indexRatingSchemes} from "../../../ratings/rating-utils";
import MeasurableRatingsViewGrid from "../view-grid/MeasurableRatingsViewGrid.svelte";

import template from "./measurable-ratings-browser-tree-panel.html";
import {lastViewedMeasurableCategoryKey} from "../../../user";
import {
    selectedCategory,
    selectedMeasurable,
    showPrimaryOnly,
    showUnmapped
} from "../view-grid/measurable-rating-view-store";
import UnmappedMeasurablesViewGrid from "../view-grid/UnmappedMeasurablesViewGrid.svelte";

/**
 * @name waltz-measurable-ratings-browser-tree-panel
 *
 * @description
 * This component shows a tabbed view (one tab per measurable category) of measurable rating trees.
 * Clicking on tree items shows a detail list on the right.
 *
 */


const bindings = {
    filters: "<",
    parentEntityRef: "<",
    onMeasurableCategorySelect: "<?",
};


const initialState = {
    applications: [],
    measurables: [],
    measurableCategories: [],
    measurableRatingsDetail: null,
    ratingTallies: [],
    detail: null,
    visibility: {
        loading: false,
        ratingDetail: false,
    },
    selectedMeasurable: null,
    onLoadDetail: () => log("onLoadDetail"),
    onMeasurableCategorySelect: () => log("onMeasurableCategorySelect"),
    showMore: false,
    showPrimaryOnly: false,
    showUnmapped: false,
    MeasurableRatingsViewGrid,
    UnmappedMeasurablesViewGrid
};


function log() {
    console.log("wmrbs::", arguments);
}


function loadMeasurableRatingTallies(serviceBroker, params, holder) {
    return serviceBroker
        .loadViewData(CORE_API.MeasurableRatingStore.statsByAppSelector, [params])
        .then(r => holder.ratingTallies = r.data);
}


function loadMeasurables(serviceBroker, selector, holder) {
    return serviceBroker
        .loadViewData(CORE_API.MeasurableStore.findMeasurablesBySelector, [selector])
        .then(r => {
            holder.measurables = r.data;
            holder.measurablesById = _.keyBy(r.data, "id");
        });
}


function loadMeasurableCategories(serviceBroker, holder) {
    return serviceBroker
        .loadAppData(CORE_API.MeasurableCategoryStore.findAll, [])
        .then(r => holder.measurableCategories = r.data);
}


function loadLastViewedCategory(serviceBroker, holder) {
    return serviceBroker
        .loadAppData(CORE_API.UserPreferenceStore.findAllForUser, [], {force: true})
        .then(r => {
            const lastViewed = _.find(r.data, d => d.key === lastViewedMeasurableCategoryKey);
            return holder.lastViewedCategoryId = lastViewed
                ? Number(lastViewed.value)
                : null;
        });
}


function loadApps(serviceBroker, selector, holder) {
    return serviceBroker
        .loadViewData(CORE_API.ApplicationStore.findBySelector, [selector])
        .then(r => holder.applications = r.data);
}


function loadRatingSchemes(serviceBroker, holder) {
    return serviceBroker
        .loadAppData(CORE_API.RatingSchemeStore.findAll)
        .then(r => holder.ratingSchemesById = indexRatingSchemes(r.data));
}


function controller($q, $scope, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const clearDetail = () => {
        vm.measurableRatingsDetail = null;
        vm.tableData = null;
    };

    const loadBaseData = () => {
        return $q
            .all([
                loadMeasurableCategories(serviceBroker, vm),
                loadMeasurables(serviceBroker, vm.selectionOptions, vm),
                loadRatingSchemes(serviceBroker, vm),
                loadApps(serviceBroker, vm.selectionOptions, vm),
                loadLastViewedCategory(serviceBroker, vm)
            ])
            .then(loadRatings);
    };


    const loadRatings = () => {
        clearDetail();

        const statsParams = {
            options: vm.selectionOptions,
            showPrimaryOnly: vm.showPrimaryOnly
        };

        const promise = loadMeasurableRatingTallies(serviceBroker, statsParams, vm);

        if (vm.visibility.ratingDetail) {
            promise.then(() => vm.onSelect(vm.selectedMeasurable));
        }

        return promise;
    };


    function setupSelector() {
        vm.selectionOptions = mkSelectionOptionsWithJoiningEntity(
            vm.parentEntityRef,
            undefined,
            undefined,
            vm.filters
        );
    }

    vm.$onInit = () => {
        setupSelector();
        loadBaseData();
    };


    vm.$onChanges = (changes) => {
        setupSelector();
        if (vm.parentEntityRef && changes.filters) {
            loadRatings();
        }
    };

    vm.onSelectUnmapped = () => {
        showUnmapped.set(true);
        vm.selectedMeasurable = {
            name: "Unmapped Applications",
            description: "Display applications which do not have any associated measurable rating for this category."
        };
    };

    showUnmapped.subscribe(d => {
        $scope.$applyAsync(() => {
            vm.showUnmapped = d;
            vm.visibility.ratingDetail = true;
        })
    })

    vm.onSelect = (measurable) => {
        selectedMeasurable.set(measurable);
        showUnmapped.set(false);
    };

    selectedMeasurable.subscribe(measurable => {
        $scope.$applyAsync(() => {
            if (!_.isEmpty(measurable)) {
                vm.selectedMeasurable = measurable;
                vm.visibility.ratingDetail = true;
            }
        })
    })

    vm.onCategorySelect = (c) => {
        vm.visibility.ratingDetail = false;
        clearDetail();
        vm.activeCategory = c;
        selectedCategory.set(c);
        showUnmapped.set(false);
        vm.onMeasurableCategorySelect(c);
    };

    vm.toggleShow = () => vm.showMore = !vm.showMore;

    vm.onTogglePrimaryOnly = (primaryOnly) => {
        showPrimaryOnly.set(primaryOnly);
    };

    showPrimaryOnly.subscribe(showPrimaryOnly => {
        $scope.$applyAsync(() => {
            vm.showPrimaryOnly = showPrimaryOnly;
            loadRatings();
        })
    })
}



controller.$inject = [
    "$q",
    "$scope",
    "ServiceBroker"
]

const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzMeasurableRatingsBrowserTreePanel"
};