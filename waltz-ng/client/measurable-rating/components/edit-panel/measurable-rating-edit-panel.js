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
import {CORE_API} from "../../../common/services/core-api-utils";
import {initialiseData} from "../../../common";
import {kindToViewState} from "../../../common/link-utils";
import {mkTabs} from "../../measurable-rating-utils";
import {indexRatingSchemes, mkRatingsKeyHandler} from "../../../ratings/rating-utils";

import template from "./measurable-rating-edit-panel.html";
import {displayError} from "../../../common/error-utils";


const bindings = {
    allocations: "<",
    allocationSchemes: "<",
    parentEntityRef: "<",
    startingCategoryId: "<?"
};


const initialState = {
    selected: null,
    allocations: [],
    allocationSchemes: [],
    measurables: [],
    categories: [],
    ratings: [],
    ratingSchemesById: {},
    ratingItemsBySchemeIdByCode: {},
    tabs: [],
    saveInProgress: false,
    startingCategoryId: null,
    unusedCategories: [],
    visibility: {
        ratingPicker: false,
        instructions: true,
        schemeOverview: false,
        showAllCategories: false,
        tab: null
    }
};


function controller($q,
                    $state,
                    notification,
                    serviceBroker) {
    const vm = initialiseData(this, initialState);


    const loadData = (force) => {
        const ratingSchemePromise = serviceBroker
            .loadAppData(CORE_API.RatingSchemeStore.findAll)
            .then(r => {
                vm.ratingSchemesById = _.keyBy(r.data, "id");
                vm.ratingItemsBySchemeIdByCode = indexRatingSchemes(r.data || []);
            });

        const categoryPromise = serviceBroker
            .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
            .then(r => {
                vm.categories = r.data;
                vm.categoriesById = _.keyBy(r.data, "id");
            });

        const measurablesPromise = serviceBroker
            .loadAppData(CORE_API.MeasurableStore.findAll)
            .then(r => vm.measurables = r.data);

        const ratingsPromise = serviceBroker
            .loadViewData(CORE_API.MeasurableRatingStore.findForEntityReference, [ vm.parentEntityRef ], { force })
            .then(r => vm.ratings = r.data);

        $q.all([ratingsPromise, measurablesPromise, categoryPromise, ratingSchemePromise])
            .then(() => {
                recalcTabs();
                if (vm.startingCategoryId) {
                    vm.activeTab = _.find(vm.tabs, t => t.category.id === vm.startingCategoryId)
                } else if (!vm.activeTab) {
                    vm.activeTab = vm.tabs[0];
                }
                vm.onTabChange();
            });

    };

    const recalcTabs = function () {
        const hasNoRatings = vm.ratings.length === 0;
        const showAllCategories = hasNoRatings || vm.visibility.showAllCategories;
        vm.tabs = mkTabs(
            vm.categories,
            vm.ratingSchemesById,
            vm.measurables,
            vm.ratings,
            vm.allocationSchemes,
            vm.allocations,
            showAllCategories);

        vm.hasHiddenTabs = vm.categories.length !== vm.tabs.length;
        if (vm.activeTab) {
            vm.activeTab = _.find(vm.tabs, t => t.category.id == vm.activeTab.category.id);
        }
    };


    const getDescription = () => _.get(
        vm.selected,
        ["rating", "description"]);


    const getRating = () => _.get(
        vm.selected,
        ["rating", "rating"]);


    const doSave = (rating, description) => {

        return serviceBroker
            .execute(
                CORE_API.MeasurableRatingStore.save,
                [vm.parentEntityRef, vm.selected.measurable.id, rating, description])
            .then(r => { vm.ratings = r.data })
            .then(() => recalcTabs())
            .then(() => {
                vm.saveInProgress = false;
                const newRating = { rating, description };
                vm.selected = Object.assign({}, vm.selected, { rating: newRating });
            })
            .catch(e => displayError(notification, "Could not save rating", e))
    };


    const doRemove = () => {

        if (! vm.selected.rating) return $q.reject();

        vm.saveInProgress = true;

        return serviceBroker
            .execute(
                CORE_API.MeasurableRatingStore.remove,
                [vm.parentEntityRef, vm.selected.measurable.id])
            .then(r => {
                vm.saveInProgress = false;
                vm.ratings = r.data;
                vm.selected.rating = null;
                recalcTabs();
            });
    };


    const deselectMeasurable = () => {
        vm.saveInProgress = false;
        vm.selected = Object.assign({}, vm.selected, { measurable: null });
        vm.visibility = Object.assign({}, vm.visibility, {schemeOverview: true, ratingPicker: false});
    };


    const selectMeasurable = (node) => {
        const { measurable, rating, allocations } = node;
        const category = _.find(vm.categories, ({ id: measurable.categoryId }));
        const ratingScheme = vm.ratingSchemesById[category.ratingSchemeId];
        const hasWarnings = !_.isEmpty(allocations);

        vm.selected = Object.assign({}, node, { category, hasWarnings, ratingScheme });
        vm.visibility = Object.assign({}, vm.visibility, {schemeOverview: false, ratingPicker: true});
    };


    // -- BOOT --

    vm.$onInit = () => {
        loadData(true);

        vm.backUrl = $state
            .href(
                kindToViewState(vm.parentEntityRef.kind),
                { id: vm.parentEntityRef.id });

        vm.allocationsByMeasurableId = _.groupBy(vm.allocations, a => a.measurableId);
        vm.allocationSchemesById = _.keyBy(vm.allocationSchemes, s => s.id);
    };


    // -- INTERACT ---

    vm.onMeasurableSelect = (node) => {
        selectMeasurable(node);
    };

    vm.onRatingSelect = r => {
        if (! vm.selected.measurable) return; // nothing selected
        if (! vm.selected.measurable.concrete) return; // not concrete
        if (r === getRating()) return; // rating not changed

        return r === "X"
            ? doRemove()
                .then(() => notification.success(`Removed: ${vm.selected.measurable.name}`))
            : doSave(r, getDescription())
                .then(() => notification.success(`Saved: ${vm.selected.measurable.name}`));
    };

    vm.onSaveComment = (comment) => {
        return doSave(getRating(), comment)
            .then(() => notification.success(`Saved Comment for: ${vm.selected.measurable.name}`))
    };

    vm.doCancel = () => {
        deselectMeasurable();
    };

    vm.onRemoveAll = (categoryId) => {
        if (confirm("Do you really want to remove all ratings in this category ?")) {
            serviceBroker
                .execute(
                    CORE_API.MeasurableRatingStore.removeByCategory,
                    [vm.parentEntityRef, categoryId])
                .then(r => {
                    notification.info("Removed all ratings for category");
                    vm.ratings = r.data;
                    recalcTabs();
                })
                .catch(e => {
                    const message = "Error removing all ratings for category: " + e.message;
                    console.log(message, { e });
                    notification.error(message);
                });
        }
    };

    vm.onTabChange = () => {
        deselectMeasurable();
        vm.onKeypress = mkRatingsKeyHandler(
            vm.activeTab.ratingScheme.ratings,
            vm.onRatingSelect,
            vm.doCancel);
    };

    vm.onShowAllTabs = () => {
        vm.visibility.showAllCategories = true;
        recalcTabs();
    };

}


controller.$inject = [
    "$q",
    "$state",
    "Notification",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzMeasurableRatingEditPanel"
};
