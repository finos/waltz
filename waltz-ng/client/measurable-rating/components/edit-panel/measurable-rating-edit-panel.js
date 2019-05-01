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
import {kindToViewState} from "../../../common/link-utils";
import {mkTabs} from "../../measurable-rating-utils";
import {indexRatingSchemes, mkRatingsKeyHandler} from "../../../ratings/rating-utils";

import template from "./measurable-rating-edit-panel.html";


const bindings = {
    allocations: "<",
    parentEntityRef: "<",
    startingCategoryId: "<?"
};


function determineSaveFn(selected, store) {
    return _.isEmpty(selected.rating)
        ? store.create
        : store.update;
}


const initialState = {
    selected: null,
    allocations: [],
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
                    measurableRatingStore,
                    notification,
                    serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadData = (force) => {
        // -- LOAD ---

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
                    vm.onTabChange(vm.startingCategoryId);
                } else if (!vm.visibility.tab) {
                    const startingTab = _.get(vm.tabs, [0, "category", "id"], null);
                    if (startingTab) { vm.onTabChange(startingTab); }
                }
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
            showAllCategories);

        vm.hasHiddenTabs = vm.categories.length !== vm.tabs.length;
    };


    const getDescription = () => _.get(
        vm.selected,
        ["rating", "description"]);


    const getRating = () => _.get(
        vm.selected,
        ["rating", "rating"]);


    const doSave = (rating, description) => {
        const saveFn = determineSaveFn(vm.selected, measurableRatingStore);

        const savePromise = saveFn(
            vm.parentEntityRef,
            vm.selected.measurable.id,
            rating,
            description);

        return savePromise
            .then(rs => vm.ratings = rs)
            .then(() => recalcTabs())
            .then(() => {
                vm.saveInProgress = false;
                const newRating = { rating, description };
                vm.selected = Object.assign({}, vm.selected, { rating: newRating });
            });
    };


    const doRemove = () => {

        if (! vm.selected.rating) return $q.reject();

        vm.saveInProgress = true;

        return measurableRatingStore
            .remove(vm.parentEntityRef, vm.selected.measurable.id)
            .then(rs => {
                vm.saveInProgress = false;
                vm.ratings = rs;
                vm.tabs = mkTabs(vm.categories, vm.ratingSchemesById, vm.measurables, vm.ratings);
                vm.selected.rating = null;
            });
    };


    const deselectMeasurable = () => {
        vm.saveInProgress = false;
        vm.selected = Object.assign({}, vm.selected, { measurable: null });
        vm.visibility = Object.assign({}, vm.visibility, {schemeOverview: true, ratingPicker: false});
    };


    const selectMeasurable = (measurable, rating) => {
        const category = _.find(vm.categories, ({ id: measurable.categoryId }));
        vm.selected = Object.assign({}, vm.selected, { rating, measurable, category });
        vm.visibility = Object.assign({}, vm.visibility, {schemeOverview: false, ratingPicker: true});
    };


    // -- BOOT --

    vm.$onInit = () => {
        loadData(true);
    };


    // -- INTERACT ---

    vm.backUrl = $state
        .href(
            kindToViewState(vm.parentEntityRef.kind),
            { id: vm.parentEntityRef.id });


    vm.onMeasurableSelect = (measurable, rating) => {
        selectMeasurable(measurable, rating);
    };

    vm.onRatingSelect = r => {
        if (! vm.selected.measurable) return; // nothing selected
        if (! vm.selected.measurable.concrete) return; // not concrete
        if (r === getRating()) return; // rating not changed

        return r === "X"
            ? doRemove()
                .then(() => notification.success("Removed"))
            : doSave(r, getDescription())
                .then(() => notification.success("Saved"));
    };

    vm.onSaveComment = (comment) => {
        return doSave(getRating(), comment)
            .then(() => notification.success("Saved Comment"))
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

    vm.onTabChange = (categoryId) => {
        deselectMeasurable();
        vm.visibility.tab = categoryId;

        const category = vm.categoriesById[categoryId];
        const ratingScheme = vm.ratingSchemesById[category.ratingSchemeId];
        vm.selected = {
            category,
            ratingScheme,
        };
        vm.onKeypress = mkRatingsKeyHandler(
            ratingScheme.ratings,
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
    "MeasurableRatingStore",
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
